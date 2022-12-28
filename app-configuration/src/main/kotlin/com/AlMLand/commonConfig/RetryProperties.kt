package com.AlMLand.commonConfig

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "retry-configuration")
class RetryProperties(
    var initialIntervalMs: Long,
    var maxIntervalMs: Long,
    var multiplier: Double,
    var maxAttempts: Int,
    var sleepTimeMs: Long
)