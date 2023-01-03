package com.AlMLand.listener

import com.AlMLand.kafka.avro.model.TwitterAvroModel
import com.AlMLand.kafka.producer.service.KafkaProducer
import com.AlMLand.kafkaAdmin.KafkaProperties
import com.AlMLand.transformation.TwitterStatusToAvroTransformer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import twitter4j.Status
import twitter4j.StatusAdapter

@Component
class TwitterStatusListener(
    private val kafkaProperties: KafkaProperties,
    private val kafkaProducer: KafkaProducer<Long, TwitterAvroModel>,
    private val avroTransformer: TwitterStatusToAvroTransformer
) : StatusAdapter() {
    companion object {
        private val logger = LoggerFactory.getLogger(TwitterStatusListener::class.java)
    }

    override fun onStatus(status: Status) {
        logger.info(
            "Received status text {} sending to kafka topic {}",
            status?.text ?: "no text available",
            kafkaProperties.topicNamesToCreate.toTypedArray()
        )
        avroTransformer.getAvroModel(status).let {
            kafkaProducer.send(kafkaProperties.topicName, it.userId, it)
        }
    }
}