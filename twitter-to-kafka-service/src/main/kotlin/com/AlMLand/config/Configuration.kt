package com.AlMLand.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import twitter4j.TwitterStreamFactory

@Configuration
@ConfigurationProperties(prefix = "twitter-to-kafka-service")
open class Configuration(
    var appStart: String = "",
    var infoMessage: String = "",
    var twitterKeywords: List<String> = listOf(),
    var twitterV2BaseUrl: String = "",
    var twitterV2RulesBaseUrl: String = "",
    var twitterV2BearerToken: String = ""
) {
    @Bean
    open fun twitterStream() = TwitterStreamFactory().instance
}
