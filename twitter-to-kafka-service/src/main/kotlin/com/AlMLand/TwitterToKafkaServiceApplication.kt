package com.AlMLand

import com.AlMLand.config.Configuration
import com.AlMLand.runner.StreamRunner
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

private val logger = LoggerFactory.getLogger(TwitterToKafkaServiceApplication::class.java)

fun main(args: Array<String>) {
    runApplication<TwitterToKafkaServiceApplication>(*args)
}

@SpringBootApplication
open class TwitterToKafkaServiceApplication(
    private val configuration: Configuration,
    private val streamRunner: StreamRunner
) :
    CommandLineRunner {
    override fun run(vararg args: String?) {
        logger.info(
            "${configuration.appStart}${configuration.infoMessage}: " +
                    configuration.twitterKeywords.joinToString(", ")
        )
        streamRunner.start()
    }
}
