package com.AlMLand.kafka.admin.client.listeners

import org.slf4j.LoggerFactory
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.listener.RetryListenerSupport
import org.springframework.stereotype.Component

private const val LOGGER_NO_INFORMATION_MESSAGE = "no information is available"

@Component
class CreateTopicListener : RetryListenerSupport() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun <T : Any?, E : Throwable?> onSuccess(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        result: T
    ) {
        logger.info(
            """
            The attempt ${context?.retryCount ?: LOGGER_NO_INFORMATION_MESSAGE} of topics creation is successful.
        """.trimIndent()
        )
    }

    override fun <T : Any?, E : Throwable?> onError(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        throwable: Throwable?
    ) {
        logger.warn(
            """
            The attempt ${context?.retryCount ?: LOGGER_NO_INFORMATION_MESSAGE} of topics creation is failed with Exception: 
            ${context?.lastThrowable ?: LOGGER_NO_INFORMATION_MESSAGE}
        """.trimIndent()
        )
    }
}

@Component
class AllTopicsListener : RetryListenerSupport() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun <T : Any?, E : Throwable?> onSuccess(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        result: T
    ) {
        logger.info(
            """
            The attempt ${context?.retryCount ?: LOGGER_NO_INFORMATION_MESSAGE} of fetching all kafka topics is successful.
        """.trimIndent()
        )
    }

    override fun <T : Any?, E : Throwable?> onError(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        throwable: Throwable?
    ) {
        logger.warn(
            """
            The attempt ${context?.retryCount ?: LOGGER_NO_INFORMATION_MESSAGE} of fetching all kafka topics is failed with Exception: 
            ${context?.lastThrowable ?: LOGGER_NO_INFORMATION_MESSAGE}
        """.trimIndent()
        )
    }
}

@Component
class CheckSchemaRegistryListener : RetryListenerSupport() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun <T : Any?, E : Throwable?> onSuccess(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        result: T
    ) {
        logger.info(
            """
            The attempt ${context?.retryCount ?: LOGGER_NO_INFORMATION_MESSAGE} of reach the schema registry via an http request is successful.
        """.trimIndent()
        )
    }

    override fun <T : Any?, E : Throwable?> onError(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        throwable: Throwable?
    ) {
        logger.warn(
            """
            The attempt ${context?.retryCount ?: LOGGER_NO_INFORMATION_MESSAGE} of reach the schema registry via an http request is failed with Exception: 
            ${context?.lastThrowable ?: LOGGER_NO_INFORMATION_MESSAGE}
        """.trimIndent()
        )
    }
}