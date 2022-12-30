package com.AlMLand.twitterToKafkaService

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import twitter4j.TwitterStreamFactory

@Configuration
class AppConfiguration {
    @Bean
    fun twitterStream() = TwitterStreamFactory().instance
}
