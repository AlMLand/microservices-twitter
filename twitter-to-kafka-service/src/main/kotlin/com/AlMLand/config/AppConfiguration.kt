package com.AlMLand.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import twitter4j.TwitterStreamFactory

@Configuration
open class AppConfiguration {
    @Bean
    open fun twitterStream() = TwitterStreamFactory().instance
}
