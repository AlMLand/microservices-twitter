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
        getLimit().let { limit ->
            var count = 0
            while (count < limit) {
                try {
                    twitterFeignClient.getTweet(twitterProperties.twitterBearerToken).let {
                        objectMapper.writeValueAsString(it)
                    }.let {
                        commonTweetService.setTweetToTwitterStatusListener(it)
                        count++
                    }
                } catch (tsce: TwitterServerConnectException) {
                    logger.error("Exception occur by connect to twitter server", tsce)
                }
            }
        }
    }

    private fun getLimit(): Int =
        twitterProperties.twitterTweetsLimit.let {
            if (it == 0) Int.MAX_VALUE else it
        }

    fun setupRules(rules: Map<String, String>) {
        val bearerToken = twitterProperties.twitterBearerToken
        formatRulesToString(twitterFeignClient.getRules(bearerToken)).let {
            if (!it.isNullOrBlank()) {
                twitterFeignClient.deleteRules(bearerToken, String.format(BODY_DELETE_TEMPLATE, it))
            }
        }
        createRules(bearerToken, rules)
    }

    private fun createRules(bearerToken: String, rules: Map<String, String>) =
        with(StringBuilder()) {
            rules.forEach { (key, value) -> append("{\"value\": \"$key\", \"tag\": \"$value\"},") }
            twitterFeignClient.createRules(
                bearerToken,
                String.format(BODY_ADD_TEMPLATE, toString().substring(0, length - 1))
            )
        }

    private fun formatRulesToString(answerFromTwitter: LinkedHashMap<String, Any>): String? =
        (answerFromTwitter["data"] as? ArrayList<LinkedHashMap<String, Any>>)
            ?.map { it["id"] }
            ?.joinToString(prefix = "\"", postfix = "\"", separator = "\",\"")

}