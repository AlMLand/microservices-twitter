package com.AlMLand.elasticsearch.consumer

import com.AlMLand.kafka.admin.client.KafkaAdminClient
import com.AlMLand.kafka.avro.model.TwitterAvroModel
import com.AlMLand.kafka.kafkaAdmin.KafkaProperties
import org.apache.avro.specific.SpecificRecordBase
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.io.Serializable

sealed interface KafkaConsumer<K : Serializable, V : SpecificRecordBase> {
    fun receive(messages: List<V>, keys: List<K>, partitions: List<Int>, offsets: List<Long>)

    @Service
    class TwitterKafkaConsumer(
        private val kafkaProperties: KafkaProperties,
        private val kafkaAdminClient: KafkaAdminClient,
        private val kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry
    ) : KafkaConsumer<Long, TwitterAvroModel> {
        companion object {
            private val logger = LoggerFactory.getLogger(this::class.java)
            private const val TOPIC_LISTENER_ID = "twitter-topic-listener"
        }

        @KafkaListener(id = TOPIC_LISTENER_ID, topics = ["\${kafka-configuration.topic-name}"])
        override fun receive(
            @Payload messages: List<TwitterAvroModel>,
            @Header(KafkaHeaders.RECEIVED_KEY) keys: List<Long>,
            @Header(KafkaHeaders.RECEIVED_PARTITION) partitions: List<Int>,
            @Header(KafkaHeaders.OFFSET) offsets: List<Long>
        ) {
            logger.info(
                """
                ${messages.size} number of message received with keys $keys, partitions $partitions and offsets $offsets
            """.trimIndent()
            )
        }

        @EventListener
        fun startListening(event: ApplicationStartedEvent) =
            kafkaAdminClient.checkTopicsCreated().let {
                kafkaListenerEndpointRegistry
                    .getListenerContainer(TOPIC_LISTENER_ID)?.start().also {
                        logger.info(
                            "Topics with names ${kafkaProperties.topicNamesToCreate} are ready for operations."
                        )
                    }
                    ?: error("The listener container with id: $TOPIC_LISTENER_ID is not found")
            }
    }
}