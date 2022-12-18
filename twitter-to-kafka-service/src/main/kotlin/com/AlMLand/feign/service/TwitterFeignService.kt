package com.AlMLand.feign.service

import com.AlMLand.common.CommonTweetService
import com.AlMLand.config.TwitterProperties
import com.AlMLand.feign.controller.TwitterFeignClient
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(TwitterFeignService::class.java)

@Service
@ConditionalOnExpression(
    "\${twitter-to-kafka-service.enable-feign-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-v2-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-mock-tweets}"
)
class TwitterFeignService(
    private val twitterFeignClient: TwitterFeignClient,
    private val twitterProperties: TwitterProperties,
    private val commonTweetService: CommonTweetService,
    private val objectMapper: ObjectMapper
) {
    fun getTweets() {
        val limit = getLimit()
        var count = 0
        while (count < limit) {
            try {
                val tweet = twitterFeignClient.getTweet(twitterProperties.twitterV2BearerToken)
                val tweetAsJson = objectMapper.writeValueAsString(tweet)
                commonTweetService.setTweetToTwitterStatusListener(tweetAsJson)
                count++
            } catch (re: RuntimeException) {
                logger.error("Exception occur by connect to twitter server")
            }
        }
        logger.info("App ends ... {} tweets are read", count)
    }

    private fun getLimit(): Int {
        val limit = twitterProperties.twitterTweetsLimit
        return if (limit == 0) Int.MAX_VALUE else limit
    }

}