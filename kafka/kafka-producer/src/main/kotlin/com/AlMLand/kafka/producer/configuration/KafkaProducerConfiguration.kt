package com.AlMLand.kafka.producer.configuration

import com.AlMLand.kafkaAdmin.KafkaProperties
import com.AlMLand.kafkaProducer.KafkaProducerProperties
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import java.io.Serializable

@Configuration
class KafkaProducerConfiguration<K : Serializable, V : SpecificRecordBase>(
    private val kafka: KafkaProperties,
    private val kafkaProducer: KafkaProducerProperties
) {
    @Bean
    fun properties(): Map<String, Any> =
        with(kafkaProducer) {
            mapOf(
                BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
                kafka.schemaRegistryUrlKey to kafka.schemaRegistryUrl,
                KEY_SERIALIZER_CLASS_CONFIG to keySerializerClass,
                VALUE_SERIALIZER_CLASS_CONFIG to valueSerializerClass,
                BATCH_SIZE_CONFIG to batchSize.times(batchSizeBoostFactor),
                LINGER_MS_CONFIG to linger,
                COMPRESSION_TYPE_CONFIG to compressionType,
                ACKS_CONFIG to ack,
                REQUEST_TIMEOUT_MS_CONFIG to requestTimeout,
                RETRIES_CONFIG to retryCount
            )
        }

    @Bean
    fun producerFactory(): ProducerFactory<K, V> = DefaultKafkaProducerFactory(properties())

    @Bean
    fun kafkaTemplate(): KafkaTemplate<K, V> = KafkaTemplate(producerFactory())
}