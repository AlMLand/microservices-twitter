package com.AlMLand

import com.AlMLand.runner.StreamRunner
import com.AlMLand.twitterToKafkaService.CommonProperties
import com.AlMLand.twitterToKafkaService.TwitterProperties
import org.slf4j.LoggerFactory
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
    private val commonProperties: CommonProperties,
    private val twitterProperties: TwitterProperties,
    private val streamRunner: StreamRunner,
    private var environment: Environment
) :
    CommandLineRunner {
    companion object {
        private val logger = LoggerFactory.getLogger(TwitterToKafkaServiceApplication::class.java)
    }

    override fun run(vararg args: String?) {
        logger.info(
            "${commonProperties.appStart}${commonProperties.infoMessage}: " +
                    twitterProperties.twitterKeywords.joinToString(", ")
        )
        if (!environment.activeProfiles.contains("test")) streamRunner.start()
    }
}
