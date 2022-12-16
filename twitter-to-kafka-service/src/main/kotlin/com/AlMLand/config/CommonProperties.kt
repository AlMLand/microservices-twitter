package com.AlMLand.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "twitter-to-kafka-service.common")
class CommonProperties(
    var appStart: String = "",
    var infoMessage: String = ""
)
