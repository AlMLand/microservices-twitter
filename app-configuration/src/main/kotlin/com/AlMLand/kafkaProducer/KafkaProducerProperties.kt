package com.AlMLand.kafkaProducer

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@SuppressWarnings("LongParameterList")
@Configuration
@ConfigurationProperties(prefix = "kafka-producer")
class KafkaProducerProperties(
    var keySerializerClass: String,
    var valueSerializerClass: String,
    var compressionType: String,
    var ack: String,
    var batchSize: Int,
    var batchSizeBoostFactor: Int,
    var linger: Int,
    var requestTimeout: Int,
    var retryCount: Int
)