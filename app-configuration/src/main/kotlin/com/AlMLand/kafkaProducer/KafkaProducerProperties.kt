package com.AlMLand.kafkaProducer

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@SuppressWarnings("LongParameterList")
@Configuration
@ConfigurationProperties(prefix = "kafka-producer")
class KafkaProducerProperties(
    var keySerializerClass: String = "",
    var valueSerializerClass: String = "",
    var compressionType: String = "",
    var ack: String = "",
    var batchSize: Int = 0,
    var batchSizeBoostFactor: Int = 0,
    var linger: Int = 0,
    var requestTimeout: Int = 0,
    var retryCount: Int = 0
)