package com.AlMLand.twittertokafkaservice

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import twitter4j.TwitterStreamFactory

@Configuration
class AppConfiguration {
    @Bean
    fun twitterStream() = TwitterStreamFactory().instance
}
