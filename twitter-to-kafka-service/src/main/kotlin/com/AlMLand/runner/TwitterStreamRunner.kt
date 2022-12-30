package com.AlMLand.runner

import com.AlMLand.listener.TwitterStatusListener
import com.AlMLand.twitterToKafkaService.TwitterProperties
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component
import twitter4j.FilterQuery
import twitter4j.TwitterStream

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