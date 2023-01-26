package com.AlMLand.kafka.admin.client

import com.AlMLand.kafka.admin.exception.KafkaCreateTopicException
import com.AlMLand.kafka.admin.exception.KafkaFetchAllTopicsException
import com.AlMLand.kafka.admin.exception.KafkaSchemaRegistryException
import com.AlMLand.kafka.kafkaAdmin.KafkaProperties
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.TopicListing
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KafkaAdminClient(
    private val kafkaProperties: KafkaProperties,
    private val kafkaAdmin: Admin,
    private val webClient: WebClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private const val RETRY_LIMIT_TOPIC_CREATION = "Reached max number of retry for creating kafka topic(s)"
        private const val RETRY_LIMIT_SCHEMA_REGISTRY = "Reached max number of retry for schema registry validate"
        private const val RETRY_LIMIT_TOPICS = "Can't fetch all topics from kafka"
    }

    @Retryable(
        value = [KafkaCreateTopicException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0)
    )
    fun createTopic() {
        try {
            kafkaProperties.topicNamesToCreate.filter { notAlreadyExists(it.trim()) }.map {
                NewTopic(
                    it.trim(),
                    kafkaProperties.numberOfPartitions,
                    kafkaProperties.replicationFactor
                )
            }.let {
                logger.info("Start with create a ${it.size} topics")
                kafkaAdmin.createTopics(it).also { result ->
                    logger.info("Create topic, result: ${result.all().get() ?: "the topic exists already"}")
                }
            }
        } catch (re: RuntimeException) {
            throw KafkaCreateTopicException(RETRY_LIMIT_TOPIC_CREATION, re)
        }
        checkTopicsCreated()
    }

    private fun notAlreadyExists(topicName: String) = allTopics()?.none { it.name() == topicName } ?: true

    @Retryable(
        value = [KafkaFetchAllTopicsException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 3.0)
    )
    fun checkTopicsCreated() {
        allTopics()?.let {
            it.map { topicListing -> topicListing.name() }.also { topicNames ->
                if (!topicNames.containsAll(kafkaProperties.topicNamesToCreate))
                    throw KafkaFetchAllTopicsException(RETRY_LIMIT_TOPICS)
            }
        } ?: throw KafkaFetchAllTopicsException(RETRY_LIMIT_TOPICS)
    }

    @Retryable(
        value = [KafkaFetchAllTopicsException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0)
    )
    private fun allTopics(): Collection<TopicListing>? =
        try {
            logger.info("Reading kafka topic ${kafkaProperties.topicNamesToCreate}")
            kafkaAdmin.listTopics().listings().get()?.run {
                forEach { logger.debug("Topic with id ${it.topicId()}, name ${it.name()}") }
                this
            }
        } catch (re: RuntimeException) {
            throw KafkaFetchAllTopicsException(RETRY_LIMIT_TOPICS, re)
        }

    @Retryable(
        value = [KafkaSchemaRegistryException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0)
    )
    fun checkSchemaRegistry() {
        if (!isSchemaRegistrySuccessful()) throw KafkaSchemaRegistryException(RETRY_LIMIT_SCHEMA_REGISTRY)
    }

    private fun isSchemaRegistrySuccessful(): Boolean =
        webClient
            .get()
            .uri(kafkaProperties.schemaRegistryUrl)
            .retrieve()
            .toBodilessEntity()
            .block()
            ?.statusCode
            ?.is2xxSuccessful ?: false
}