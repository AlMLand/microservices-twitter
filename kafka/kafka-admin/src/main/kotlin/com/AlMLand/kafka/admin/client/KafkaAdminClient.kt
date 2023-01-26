package com.AlMLand.kafka.admin.client

import com.AlMLand.kafka.admin.exception.KafkaClientException
import com.AlMLand.kafka.kafkaAdmin.KafkaProperties
import com.AlMLand.retry.RetryProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
    private val retryProperties: RetryProperties,
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
        value = [Throwable::class],
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
        } catch (t: Throwable) {
            throw KafkaClientException(RETRY_LIMIT_TOPIC_CREATION, t)
        }
        checkTopicsCreated()
    }

    fun checkTopicsCreated() {
        var topics = getTopics()
        for (name in kafkaProperties.topicNamesToCreate) {
            runBlocking {
                var (retryCount, sleepTime) = Pair(1, retryProperties.sleepTimeMs)
                while (!isTopicCreated(topics, name)) {
                    retryCount = checkRetry(retryCount, sleepTime, RETRY_LIMIT_TOPIC_CREATION)
                    sleepTime = sleepTime.times(retryProperties.multiplier)
                    topics = getTopics()
                }
            }
        }
    }

    @Retryable(
        value = [Throwable::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0)
    )
    private fun getTopics(): Collection<TopicListing>? =
        try {
            logger.info("Reading kafka topic ${kafkaProperties.topicNamesToCreate}")
            kafkaAdmin.listTopics().listings().get()?.run {
                forEach { logger.debug("Topic with id ${it.topicId()}, name ${it.name()}") }
                this
            }
        } catch (t: Throwable) {
            throw KafkaClientException(RETRY_LIMIT_TOPICS, t)
        }

    private suspend fun isTopicCreated(topics: Collection<TopicListing>?, topicName: String): Boolean =
        topics?.any { it.name() == topicName } ?: false

    private fun notAlreadyExists(topicName: String) = getTopics()?.none { it.name() == topicName } ?: true

    fun checkSchemaRegistry() {
        var (retryCount, sleepTime) = Pair(1, retryProperties.sleepTimeMs)
        runBlocking {
            while (!isSchemaRegistrySuccessful()) {
                retryCount = checkRetry(retryCount, sleepTime, RETRY_LIMIT_SCHEMA_REGISTRY)
                sleepTime = sleepTime.times(retryProperties.multiplier)
            }
        }
    }

    private suspend fun checkRetry(retryCount: Int, sleepTime: Long, errorMessage: String): Int {
        checkMaxRetry(retryCount, retryProperties.maxAttempts, errorMessage)
        delay(sleepTime)
        return retryCount.inc()
    }

    private fun checkMaxRetry(retryCount: Int, maxRetry: Int, errorMessage: String) {
        if (retryCount > maxRetry) throw KafkaClientException(errorMessage)
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