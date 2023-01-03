package com.AlMLand.initialization

import com.AlMLand.kafka.admin.client.KafkaAdminClient
import com.AlMLand.kafkaAdmin.KafkaProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

sealed interface StreamInitializer {
    fun init()

    @Component
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