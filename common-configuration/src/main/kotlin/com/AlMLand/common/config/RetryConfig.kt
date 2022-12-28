package com.AlMLand.common.config

import com.AlMLand.commonConfig.RetryProperties
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
            retryProperties.multiplier,
            retryProperties.maxIntervalMs
        )
        .customPolicy(SimpleRetryPolicy(retryProperties.maxAttempts))
        .build()
}