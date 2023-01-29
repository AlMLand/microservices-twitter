package com.AlMLand.kafka.admin.client

import com.AlMLand.kafka.admin.exception.KafkaCreateTopicException
import com.AlMLand.kafka.admin.exception.KafkaFetchAllTopicsException
import com.AlMLand.kafka.admin.exception.KafkaSchemaRegistryException
import com.AlMLand.kafka.admin.exception.KafkaTopicReadyException
import com.AlMLand.kafka.kafkaAdmin.KafkaProperties
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.TopicListing
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException

@Component
class KafkaAdminClient(
    private val kafkaProperties: KafkaProperties,
    private val kafkaAdmin: Admin,
    private val webClient: WebClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Retryable(
        retryFor = [KafkaCreateTopicException::class],
        maxAttempts = 4,
        backoff = Backoff(delay = 2000, multiplier = 2.0),
        listeners = ["createTopicListener"]
    )
    fun createTopic() {
        try {
            kafkaProperties.topicNamesToCreate
                .notAlreadyExists()
                .mapToNewTopic()
                .let {
                    logger.info("Start with create a ${it.size} topics")
                    kafkaAdmin.createTopics(it).also { result ->
                        logger.info("Create topic, result: ${result.all().get() ?: "result is not available"}")
                    }
                }
        } catch (re: RuntimeException) {
            throw KafkaCreateTopicException("Can't create kafka topic(s)", re)
        }
        isTopicReady()
    }

    private fun List<String>.mapToNewTopic() =
        map {
            NewTopic(
                it.trim(),
                kafkaProperties.numberOfPartitions,
                kafkaProperties.replicationFactor
            )
        }

    private fun List<String>.notAlreadyExists() =
        allTopics()?.let {
            it.map { topicListing -> topicListing.name() }
                .let { availableNames ->
                    filter { newNames -> !availableNames.contains(newNames) }
                }
        } ?: this

    @Retryable(
        retryFor = [KafkaTopicReadyException::class],
        maxAttempts = 4,
        backoff = Backoff(delay = 2000, multiplier = 3.0),
        listeners = ["topicReadyListener"]
    )
    fun isTopicReady() {
        allTopics()?.let {
            it.map { topicListing -> topicListing.name() }.also { topicNames ->
                if (!topicNames.containsAll(kafkaProperties.topicNamesToCreate))
                    throw KafkaTopicReadyException("Kafka topics are not ready to use")
            }
        } ?: throw KafkaTopicReadyException("Kafka topics are not ready to use")
    }

    @Retryable(
        retryFor = [KafkaFetchAllTopicsException::class],
        maxAttempts = 4,
        backoff = Backoff(delay = 2000, multiplier = 2.0),
        listeners = ["allTopicsListener"]
    )
    private fun allTopics(): Collection<TopicListing>? =
        try {
            kafkaAdmin.listTopics().listings().get()?.run {
                forEach { logger.debug("Fetch topic with id ${it.topicId()}, name ${it.name()}") }
                this
            }
        } catch (re: RuntimeException) {
            throw KafkaFetchAllTopicsException("Can't fetch all topics from kafka", re)
        }

    @Retryable(
        retryFor = [KafkaSchemaRegistryException::class],
        maxAttempts = 4,
        backoff = Backoff(delay = 3000, multiplier = 3.0),
        listeners = ["checkSchemaRegistryListener"]
    )
    fun checkSchemaRegistry() {
        try {
            (webClient
                .get()
                .uri(kafkaProperties.schemaRegistryUrl)
                .retrieve()
                .toBodilessEntity()
                .block()
                ?.statusCode
                ?.is2xxSuccessful ?: false).also {
                if (!it) throw KafkaSchemaRegistryException("Schema registry is not started")
            }
        } catch (e: WebClientRequestException) {
            throw KafkaSchemaRegistryException("Schema registry is not started")
        }
    }

}