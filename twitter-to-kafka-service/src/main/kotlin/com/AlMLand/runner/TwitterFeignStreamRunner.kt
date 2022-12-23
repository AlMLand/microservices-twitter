package com.AlMLand.runner

import com.AlMLand.common.CommonTweetService
import com.AlMLand.feign.service.TwitterFeignService
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

@Component
@ConditionalOnExpression(
    "\${twitter-to-kafka-service.enable-feign-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-v2-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-mock-tweets}"
)
class TwitterFeignStreamRunner(
    private val twitterFeignService: TwitterFeignService,
    private val commonTweetService: CommonTweetService
) : StreamRunner {
    override fun start() {
        twitterFeignService.setupRules(commonTweetService.getRules())
        twitterFeignService.getTweets()
    }
}