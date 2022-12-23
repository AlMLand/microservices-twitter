package com.AlMLand.runner

import com.AlMLand.common.CommonTweetService
import com.AlMLand.common.TwitterV2StreamHelper
import com.AlMLand.exception.StreamTweetsException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

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
        } catch (re: StreamTweetsException) {
            logger.error("Error occurred by streaming tweets")
            throw re
        }
    }
}