package com.AlMLand.runner

import com.AlMLand.common.CommonTweetService
import com.AlMLand.config.TwitterProperties
import com.AlMLand.feign.service.TwitterFeignService
import com.AlMLand.listener.TwitterStatusListener
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component
import twitter4j.FilterQuery
import twitter4j.TwitterStream

@FunctionalInterface
sealed interface StreamRunner {
    fun start()

    @Component
    @ConditionalOnExpression(
        "not \${twitter-to-kafka-service.enable-v2-tweets} " +
                "&& not \${twitter-to-kafka-service.enable-feign-tweets}" +
                "&& not \${twitter-to-kafka-service.enable-mock-tweets}"
    )
    class TwitterStreamRunner(
        private val twitterProperties: TwitterProperties,
        private val twitterStatusListener: TwitterStatusListener,
        private val twitterStream: TwitterStream
    ) : StreamRunner {
        companion object {
            private val logger = LoggerFactory.getLogger(TwitterStreamRunner::class.java)
        }

        override fun start() {
            twitterStream.addListener(twitterStatusListener)
            addFilter()
        }

        private fun addFilter() {
            val keywords = twitterProperties.twitterKeywords.toTypedArray()
            twitterStream.filter(FilterQuery(*keywords))
            logger.info("Starts filtering twitter stream for keywords {}", keywords.joinToString(", "))
        }

        @PreDestroy
        fun shutdown() {
            logger.info("Closing twitter stream")
            twitterStream.shutdown()
        }
    }

    @Component
    @ConditionalOnExpression(
        "\${twitter-to-kafka-service.enable-v2-tweets} " +
                "&& not \${twitter-to-kafka-service.enable-feign-tweets} " +
                "&& not \${twitter-to-kafka-service.enable-mock-tweets}"
    )
    class TwitterV2StreamRunner(
        private val commonTweetService: CommonTweetService,
        private val twitterV2StreamHelper: TwitterV2StreamHelper
    ) : StreamRunner {
        companion object {
            private val logger = LoggerFactory.getLogger(TwitterV2StreamRunner::class.java)
        }

        override fun start() {
            try {
                twitterV2StreamHelper.setupRules(commonTweetService.getRules())
                twitterV2StreamHelper.connectStream()
            } catch (re: RuntimeException) {
                logger.error("Error occurred by streaming tweets")
                throw re
            }
        }
    }

    @Component
    @ConditionalOnExpression(
        "\${twitter-to-kafka-service.enable-feign-tweets} " +
                "&& not \${twitter-to-kafka-service.enable-v2-tweets} " +
                "&& not \${twitter-to-kafka-service.enable-mock-tweets}"
    )
    class TwitterFeignStreamRunner(
        private val twitterFeignService: TwitterFeignService,
        private val commonTweetService: CommonTweetService
    ) : StreamRunner {
        override fun start() {
            twitterFeignService.setupRules(commonTweetService.getRules())
            twitterFeignService.getTweets()
        }
    }
}
