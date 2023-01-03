package com.AlMLand.feign.config

import com.AlMLand.feign.controller.TwitterFeignClient
import com.AlMLand.twitterToKafkaService.TwitterProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConditionalOnExpression(
    "\${twitter-to-kafka-service.enable-feign-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-v2-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-mock-tweets}"
)
@Configuration
class FeignConfiguration(private val twitterProperties: TwitterProperties) {

    @Bean
    fun twitterClient() = Feign.builder()
        .encoder(JacksonEncoder(getObjectMapper()))
        .decoder(JacksonDecoder(getObjectMapper()))
        .target(TwitterFeignClient::class.java, twitterProperties.twitterBaseUrl)

    private fun getObjectMapper() = ObjectMapper().registerModule(
        KotlinModule.Builder()
            .configure(KotlinFeature.NullToEmptyCollection, true)
            .configure(KotlinFeature.NullToEmptyMap, true)
            .configure(KotlinFeature.NullIsSameAsDefault, true)
            .configure(KotlinFeature.SingletonSupport, true)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build()
    )
        .findAndRegisterModules()
}