package com.AlMLand.listener

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import twitter4j.Status
import twitter4j.StatusAdapter

@Component
class TwitterStatusListener : StatusAdapter() {
    companion object {
        private val logger = LoggerFactory.getLogger(TwitterStatusListener::class.java)
    }

    override fun onStatus(status: Status?) {
        logger.info("Twitter status with text {}", status?.text ?: "no text available")
    }
}