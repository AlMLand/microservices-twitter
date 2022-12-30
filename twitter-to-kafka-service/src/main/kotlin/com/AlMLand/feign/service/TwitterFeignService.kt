package com.AlMLand.feign.service

import com.AlMLand.common.CommonTweetService
import com.AlMLand.exception.TwitterServerConnectException
import com.AlMLand.feign.controller.TwitterFeignClient
import com.AlMLand.twitterToKafkaService.TwitterProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Service

@Service
@ConditionalOnExpression(
    "\${twitter-to-kafka-service.enable-feign-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-v2-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-mock-tweets}"
)
@SuppressWarnings("ImplicitDefaultLocale")
class TwitterFeignService(
    private val twitterFeignClient: TwitterFeignClient,
    private val twitterProperties: TwitterProperties,
    private val commonTweetService: CommonTweetService,
    private val objectMapper: ObjectMapper
) {
    companion object {
        private val logger = LoggerFactory.getLogger(TwitterFeignService::class.java)
        private const val BODY_ADD_TEMPLATE = "{\"add\": [%s]}"
        private const val BODY_DELETE_TEMPLATE = "{\"delete\":{\"ids\":[%s]}}"
    }

    fun getTweets() {
        val limit = getLimit()
        var count = 0
        while (count < limit) {
            try {
                val tweet = twitterFeignClient.getTweet(twitterProperties.twitterBearerToken)
                val tweetAsJson = objectMapper.writeValueAsString(tweet)
                commonTweetService.setTweetToTwitterStatusListener(tweetAsJson)
                count++
            } catch (tsce: TwitterServerConnectException) {
                logger.error("Exception occur by connect to twitter server", tsce)
            }
        }
    }

    private fun getLimit(): Int {
        val limit = twitterProperties.twitterTweetsLimit
        return if (limit == 0) Int.MAX_VALUE else limit
    }

    fun setupRules(rules: Map<String, String>) {
        val bearerToken = twitterProperties.twitterBearerToken
        val answerFromTwitter = twitterFeignClient.getRules(bearerToken)
        val existingRules = formatRulesToString(answerFromTwitter)
        if (!existingRules.isNullOrBlank()) {
            twitterFeignClient.deleteRules(bearerToken, String.format(BODY_DELETE_TEMPLATE, existingRules))
        }
        createRules(bearerToken, rules)
    }

    private fun createRules(bearerToken: String, rules: Map<String, String>) {
        val sb = StringBuilder()
        rules.forEach { (key, value) -> sb.append("{\"value\": \"$key\", \"tag\": \"$value\"},") }
        twitterFeignClient.createRules(
            bearerToken,
            String.format(BODY_ADD_TEMPLATE, sb.toString().substring(0, sb.length - 1))
        )
    }

    private fun formatRulesToString(answerFromTwitter: LinkedHashMap<String, Any>): String? =
        (answerFromTwitter["data"] as? ArrayList<LinkedHashMap<String, Any>>)
            ?.map { it["id"] }
            ?.joinToString(prefix = "\"", postfix = "\"", separator = "\",\"")

}