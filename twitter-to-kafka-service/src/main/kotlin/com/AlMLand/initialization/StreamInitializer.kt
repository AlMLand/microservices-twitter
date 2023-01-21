package com.AlMLand.initialization

import com.AlMLand.kafka.admin.client.KafkaAdminClient
import com.AlMLand.kafka.kafkaAdmin.KafkaProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@FunctionalInterface
sealed interface StreamInitializer {
    fun init()

    @Component
    @ConditionalOnProperty("twitter-to-kafka-service.kafka-initialization-programmatically")
    class KafkaStreamInitializer(
        private val kafkaProperties: KafkaProperties,
        private val kafkaAdminClient: KafkaAdminClient
    ) : StreamInitializer {
        companion object {
            private val logger = LoggerFactory.getLogger(this::class.java)
        }

        override fun init() {
            with(kafkaAdminClient) {
                createTopic()
                checkSchemaRegistry()
                logger.info(
                    "Topics with names: {} is ready for operations",
                    kafkaProperties.topicNamesToCreate.toTypedArray()
                )
            }
        }
    }
}