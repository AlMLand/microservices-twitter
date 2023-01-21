package com.AlMLand

import com.AlMLand.initialization.StreamInitializer
import com.AlMLand.runner.StreamRunner
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.Environment

fun main(args: Array<String>) {
    runApplication<TwitterToKafkaServiceApplication>(*args)
}

@SpringBootApplication
@ComponentScan("com.AlMLand")
class TwitterToKafkaServiceApplication(
    private var environment: Environment,
    private val streamRunner: StreamRunner,
    @Value("\${twitter-to-kafka-service.kafka-initialization-programmatically}")
    private val isProgrammaticallyInit: Boolean
) : CommandLineRunner {

    @Autowired(required = false)
    private lateinit var streamInitializer: StreamInitializer

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun run(vararg args: String?) {
        if (!isTestProfileActive()) {
            if (isProgrammaticallyInit) streamInitializer.init()
            streamRunner.start()
        }
    }

    private fun isTestProfileActive() = environment.activeProfiles.contains("test")
}
