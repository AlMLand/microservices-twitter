package com.AlMLand.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnExpression(
    "\${twitter-to-kafka-service.enable-mock-tweets}" +
            "&& not \${twitter-to-kafka-service.enable-v2-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-feign-tweets}"
)
@ConfigurationProperties(prefix = "twitter-to-kafka-service")
class MockProperties(
    var twitterKeywords: List<String> = listOf(),
    var mockMinTweetLength: Int = 0,
    var mockMaxTweetLength: Int = 0,
    var mockSleepMs: Long = 0
)