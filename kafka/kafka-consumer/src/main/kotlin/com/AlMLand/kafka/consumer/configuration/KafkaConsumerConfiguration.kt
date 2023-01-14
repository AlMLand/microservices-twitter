package com.AlMLand.kafka.consumer.configuration

import com.AlMLand.kafka.kafkaAdmin.KafkaProperties
import com.AlMLand.kafka.kafkaConsumer.KafkaConsumerProperties
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import java.io.Serializable

@EnableKafka
@Configuration
class KafkaConsumerConfiguration<K : Serializable, V : SpecificRecordBase>(
    private val kafka: KafkaProperties,
    private val kafkaConsumer: KafkaConsumerProperties
) {
    @Bean
    fun properties(): Map<String, Any> =
        with(kafkaConsumer) {
            mapOf(
                BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
                KEY_DESERIALIZER_CLASS_CONFIG to keyDeserializer,
                VALUE_DESERIALIZER_CLASS_CONFIG to valueDeserializer,
                GROUP_ID_CONFIG to consumerGroupId,
                AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
                kafka.schemaRegistryUrlKey to kafka.schemaRegistryUrl,
                specificAvroReaderKey to specificAvroReader,
                SESSION_TIMEOUT_MS_CONFIG to sessionTimeout,
                HEARTBEAT_INTERVAL_MS_CONFIG to heartbeatInterval,
                MAX_POLL_INTERVAL_MS_CONFIG to maxPollInterval,
                MAX_PARTITION_FETCH_BYTES_CONFIG to (maxPartitionFetchBytesDefault * maxPartitionFetchBytesBoostFactor),
                MAX_POLL_RECORDS_CONFIG to maxPollRecords
            )
        }

    @Bean
    fun consumerFactory(): ConsumerFactory<K, V> = DefaultKafkaConsumerFactory(properties())

    @Bean
    fun kafkaListenerContainerFactory(): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<K, V>> =
        ConcurrentKafkaListenerContainerFactory<K, V>().apply {
            consumerFactory = consumerFactory()
            isBatchListener = kafkaConsumer.batchListener
            setConcurrency(kafkaConsumer.concurrencyLevel)
            setAutoStartup(kafkaConsumer.autoStartUp)
            containerProperties.pollTimeout = kafkaConsumer.pollTimeout
        }
}