package com.AlMLand.commonConfig

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "retry-configuration")
class RetryProperties(
    var initialIntervalMs: Long = 0,
    var maxIntervalMs: Long = 0,
    var multiplier: Long = 0,
    var maxAttempts: Int = 0,
    var sleepTimeMs: Long = 0
)