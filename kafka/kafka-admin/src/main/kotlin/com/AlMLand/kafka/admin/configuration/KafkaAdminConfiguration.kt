package com.AlMLand.kafka.admin.configuration

import com.AlMLand.kafka.kafkaAdmin.KafkaProperties
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.Admin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@Configuration
class KafkaAdminConfiguration(private val kafkaProperties: KafkaProperties) {
    @Bean
    fun adminClient(): Admin =
        Admin.create(mapOf(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers))
}