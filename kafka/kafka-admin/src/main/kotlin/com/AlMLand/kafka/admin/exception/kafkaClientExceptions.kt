package com.AlMLand.kafka.admin.exception

class KafkaSchemaRegistryException(message: String, throwable: Throwable? = null) : RuntimeException(message, throwable)

class KafkaCreateTopicException(message: String, throwable: Throwable? = null) : RuntimeException(message, throwable)

class KafkaFetchAllTopicsException(message: String, throwable: Throwable? = null) : RuntimeException(message, throwable)

class KafkaTopicReadyException(message: String, throwable: Throwable? = null) : RuntimeException(message, throwable)