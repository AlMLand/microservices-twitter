package com.AlMLand.elasticsearch.consumer

import com.AlMLand.kafka.admin.client.KafkaAdminClient
import com.AlMLand.kafka.avro.model.TwitterAvroModel
import com.AlMLand.kafka.kafkaAdmin.KafkaProperties
import org.apache.avro.specific.SpecificRecordBase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.io.Serializable

sealed interface KafkaConsumer<K : Serializable, V : SpecificRecordBase> {
    fun receive(messages: List<V>, keys: List<Int>, partitions: List<Int>, offsets: List<Long>)

    @Service
    class TwitterKafkaConsumer(
        private val kafkaProperties: KafkaProperties,
        private val kafkaAdminClient: KafkaAdminClient,
        private val kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry
    ) : KafkaConsumer<Long, TwitterAvroModel> {
        companion object {
            private val logger = LoggerFactory.getLogger(this::class.java)
        }

        @KafkaListener(id = "twitterTopicListener", topics = ["\${kafka-configuration.topic-name}"])
        override fun receive(
            @Payload messages: List<TwitterAvroModel>,
            @Header(KafkaHeaders.RECEIVED_KEY) keys: List<Int>,
            @Header(KafkaHeaders.RECEIVED_PARTITION) partitions: List<Int>,
            @Header(KafkaHeaders.OFFSET) offsets: List<Long>
        ) {
            logger.info(
                """
                {} number of message received with keys {}, partitions {} and offsets {}
            """.trimIndent(),
                messages.size,
                keys,
                partitions,
                offsets
            )
        }
    }
}