package com.AlMLand.elasticsearch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

fun main(args: Array<String>) {
    runApplication<KafkaToElasticsearchServiceApplication>(*args)
}

@ComponentScan(basePackages = ["com.AlMLand.kafka", "com.AlMLand.elasticsearch"])
@SpringBootApplication
class KafkaToElasticsearchServiceApplication