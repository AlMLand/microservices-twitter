package com.AlMLand.kafka.kafkaConsumer

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@SuppressWarnings("LongParameterList")
@Configuration
@ConfigurationProperties(prefix = "kafka-consumer")
class KafkaConsumerProperties(
    var keyDeserializer: String = "",
    var valueDeserializer: String = "",
    var consumerGroupId: String = "",
    var autoOffsetReset: String = "",
    var specificAvroReaderKey: String = "",
    var specificAvroReader: String = "",
    var batchListener: Boolean = false,
    var autoStartUp: Boolean = false,
    var concurrencyLevel: Int = 0,
    var sessionTimeout: Int = 0,
    var heartbeatInterval: Int = 0,
    var maxPollInterval: Int = 0,
    var maxPollRecords: Int = 0,
    var maxPartitionFetchBytesDefault: Int = 0,
    var maxPartitionFetchBytesBoostFactor: Int = 0,
    var pollTimeout: Long = 0
)