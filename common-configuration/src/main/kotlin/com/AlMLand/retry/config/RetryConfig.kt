package com.AlMLand.retry.config

import com.AlMLand.retry.RetryProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.retry.support.RetryTemplateBuilder

@Configuration
class RetryConfig(private val retryProperties: RetryProperties) {
    @Bean
    fun retryTemplate(): RetryTemplate = RetryTemplateBuilder()
        .exponentialBackoff(
            retryProperties.initialIntervalMs,
            retryProperties.multiplier.toDouble(),
            retryProperties.maxIntervalMs
        )
        .customPolicy(SimpleRetryPolicy(retryProperties.maxAttempts))
        .build()
}