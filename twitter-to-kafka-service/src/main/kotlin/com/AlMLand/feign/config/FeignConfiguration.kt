package com.AlMLand.feign.config

import com.AlMLand.config.TwitterProperties
import com.AlMLand.feign.controller.TwitterClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignConfiguration(private val twitterProperties: TwitterProperties) {

    @Bean("twitterClient")
    fun twitterClient() = Feign.builder()
        .encoder(JacksonEncoder(getObjectMapper()))
        .decoder(JacksonDecoder(getObjectMapper()))
        .target(TwitterClient::class.java, twitterProperties.twitterV2BaseUrl)

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