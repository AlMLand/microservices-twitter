package com.AlMLand.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import twitter4j.TwitterStreamFactory

@Configuration
class AppConfiguration {
    @Bean
    fun twitterStream() = TwitterStreamFactory().instance
}
