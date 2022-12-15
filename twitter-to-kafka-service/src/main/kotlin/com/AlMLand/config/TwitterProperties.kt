package com.AlMLand.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "twitter-to-kafka-service")
open class TwitterProperties(
    var twitterKeywords: List<String> = listOf(),
    var twitterV2BaseUrl: String = "",
    var twitterV2RulesBaseUrl: String = "",
    var twitterV2BearerToken: String = ""
)