package com.AlMLand.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import twitter4j.TwitterStreamFactory

@Configuration
@ConfigurationProperties(prefix = "twitter-to-kafka-service.common")
open class CommonProperties(
    var appStart: String = "",
    var infoMessage: String = ""
) {
    @Bean
    open fun twitterStream() = TwitterStreamFactory().instance
}
