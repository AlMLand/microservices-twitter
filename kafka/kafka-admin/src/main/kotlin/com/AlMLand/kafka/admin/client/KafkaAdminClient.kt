package com.AlMLand.kafka.admin.client

import com.AlMLand.commonConfig.RetryProperties
import com.AlMLand.kafka.admin.exception.KafkaClientException
import com.AlMLand.kafkaAdmin.KafkaProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.CreateTopicsResult
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.TopicListing
import org.slf4j.LoggerFactory
import org.springframework.retry.RetryContext
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KafkaAdminClient(
    private val kafkaProperties: KafkaProperties,
    private val retryProperties: RetryProperties,
    private val kafkaAdmin: Admin,
    private val retryTemplate: RetryTemplate,
    private val webClient: WebClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun createTopic() {
        val result = try {
            retryTemplate.execute<CreateTopicsResult, Throwable> { executeCreateTopics(it) }
        } catch (t: Throwable) {
            throw KafkaClientException("Reached max number of retry for creating kafka topic(s)", t)
        }
        checkTopicsCreated()
    }

    fun checkTopicsCreated() {
        var topics = getTopics()
        for (name in kafkaProperties.topicNamesToCreate) {
            runBlocking {
                var (retryCount, sleepTime) = Pair(1, retryProperties.sleepTimeMs)
                while (!isTopicCreated(topics, name)) {
                    retryCount = checkRetry(retryCount, sleepTime)
                    sleepTime = increaseSleepTime(sleepTime)
                    topics = getTopics()
                }
            }
        }
    }

    private fun getTopics(): Collection<TopicListing>? =
        try {
            retryTemplate.execute<Collection<TopicListing>, Throwable> { executeGetTopics(it) }
        } catch (t: Throwable) {
            throw KafkaClientException("Reached max number of retry for creating kafka topic(s)", t)
        }

    private fun executeGetTopics(retryContext: RetryContext): Collection<TopicListing>? {
        logger.info(
            "Reading kafka topic {}, attempt {}",
            kafkaProperties.topicNamesToCreate.toTypedArray(),
            retryContext.retryCount
        )
        return kafkaAdmin.listTopics().listings().get()?.run {
            forEach { logger.debug("Topic with id {}, name {}", it.topicId(), it.name()) }
            this
        }
    }

    private fun isTopicCreated(topics: Collection<TopicListing>?, topicName: String): Boolean =
        topics?.any { it.name() == topicName } ?: false

    private fun executeCreateTopics(retryContext: RetryContext): CreateTopicsResult {
        val topicNames = kafkaProperties.topicNamesToCreate
        val newTopics =
            topicNames.map {
                NewTopic(
                    it.trim(),
                    kafkaProperties.numberOfPartitions,
                    kafkaProperties.replicationFactor
                )
            }
        logger.info("Start with create a {} topics , attempt {}", topicNames.size, retryContext.retryCount)
        return kafkaAdmin.createTopics(newTopics)
    }

    fun checkSchemaRegistry() {
        var (retryCount, sleepTime) = Pair(1, retryProperties.sleepTimeMs)
        runBlocking {
            while (!isSchemaRegistrySuccessful()) {
                retryCount = checkRetry(retryCount, sleepTime)
                sleepTime = increaseSleepTime(sleepTime)
            }
        }
    }

    private fun increaseSleepTime(sleepTime: Long): Long = sleepTime * retryProperties.multiplier

    private suspend fun checkRetry(retryCount: Int, sleepTime: Long): Int {
        checkMaxRetry(retryCount, retryProperties.maxAttempts)
        delay(sleepTime)
        return retryCount + 1
    }

    private fun checkMaxRetry(retryCount: Int, maxRetry: Int) {
        if (retryCount > maxRetry) throw KafkaClientException("Reached max number of retry for creating kafka topic(s)")
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