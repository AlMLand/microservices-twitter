package com.AlMLand.kafkaAdmin

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@SuppressWarnings("LongParameterList")
@Configuration
@ConfigurationProperties(prefix = "kafka-configuration")
class KafkaProperties(
    var bootstrapServers: String,
    var schemaRegistryUrlKey: String,
    var schemaRegistryUrl: String,
    var topicName: String,
    var topicNamesToCreate: List<String>,
    var numberOfPartitions: Int,
    var replicationFactor: Short
)