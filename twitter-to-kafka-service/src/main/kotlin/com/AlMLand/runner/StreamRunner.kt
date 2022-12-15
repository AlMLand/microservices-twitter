package com.AlMLand.runner

import com.AlMLand.config.Configuration
import com.AlMLand.listener.TwitterStatusListener
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import twitter4j.FilterQuery
import twitter4j.TwitterStream

@FunctionalInterface
sealed interface StreamRunner {
    fun start()

    @Component
    @ConditionalOnProperty(
        name = ["twitter-to-kafka-service.enable-v2-tweets"],
        havingValue = "false"
    )
    open class TwitterStreamRunner(
        private val configuration: Configuration,
        private val twitterStatusListener: TwitterStatusListener,
        private val twitterStream: TwitterStream
    ) : StreamRunner {
        private val logger = LoggerFactory.getLogger(StreamRunner.TwitterStreamRunner::class.java)

        override fun start() {
            twitterStream.addListener(twitterStatusListener)
            addFilter()
        }

        private fun addFilter() {
            val keywords = configuration.twitterKeywords.toTypedArray()
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
    @ConditionalOnExpression("\${twitter-to-kafka-service.enable-v2-tweets} && not \${twitter-to-kafka-service.enable-mock-tweets}")
    open class TwitterV2StreamRunner(
        private val configuration: Configuration,
        private val twitterV2StreamHelper: TwitterV2StreamHelper
    ) : StreamRunner {
        private val logger = LoggerFactory.getLogger(StreamRunner.TwitterV2StreamRunner::class.java)

        override fun start() {
            val bearerToken: String? = configuration.twitterV2BearerToken
            bearerToken?.let {
                try {
                    twitterV2StreamHelper.setupRules(bearerToken, getRules())
                    twitterV2StreamHelper.connectStream(bearerToken)
                } catch (re: RuntimeException) {
                    logger.error("Error occurred by streaming tweets")
                    throw re
                }
            }
                ?: throw IllegalArgumentException("There was a problem getting with bearer token")
        }

        private fun getRules(): Map<String, String> {
            val keywords = configuration.twitterKeywords
            val rules = keywords.associateWith { "Keyword: $it" }
            logger.info("Created filter for twitter stream for keywords {}", keywords)
            return rules
        }
    }

}