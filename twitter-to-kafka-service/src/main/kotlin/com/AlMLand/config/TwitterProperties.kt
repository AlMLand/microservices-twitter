package com.AlMLand.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "twitter-to-kafka-service")
class TwitterProperties(
    var twitterKeywords: List<String> = listOf(),
    var twitterBaseUrl: String = "",
    var twitterTweetUrl: String = "",
    var twitterRulesUrl: String = "",
    var twitterBearerToken: String = "",
    var twitterTweetsLimit: Int = 0
)