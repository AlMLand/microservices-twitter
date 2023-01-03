package com.AlMLand.kafka.producer.service

import com.AlMLand.kafka.avro.model.TwitterAvroModel
import org.apache.avro.specific.SpecificRecordBase
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.io.Serializable

sealed interface KafkaProducer<K : Serializable, V : SpecificRecordBase> {
    fun send(topicName: String, key: K, value: V)

    @Service
    class TwitterKafkaProducer(
        private val kafkaTemplate: KafkaTemplate<Long, TwitterAvroModel>
    ) : KafkaProducer<Long, TwitterAvroModel> {

        companion object {
            private val logger = LoggerFactory.getLogger(this::class.java)
        }

        override fun send(topicName: String, key: Long, message: TwitterAvroModel) {
            logger.info("Sending message: '{}' to topic: '{}'", message, topicName)
            // und returned the answer(ack from kafka server)
            kafkaTemplate.send(topicName, key, message).run {
                if (isCompletedExceptionally)
                    logger.error("Error while sending message: '{}' to topic: '{}'", message, topicName)
                else {
                    get().recordMetadata.run {
                        logger.debug(
                            "Received new metadata [ topic: {}, partition: {}, offset: {}, timestamp: {}, at time: {} ]",
                            topic(), partition(), offset(), timestamp(), System.nanoTime()
                        )
                    }
                }
            }
        }
    }
}