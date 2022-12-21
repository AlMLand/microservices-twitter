package com.AlMLand.runner

import com.AlMLand.config.MockProperties
import com.AlMLand.exception.TwitterStatusException
import com.AlMLand.listener.TwitterStatusListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component
import twitter4j.TwitterObjectFactory
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

@Component
@ConditionalOnExpression(
    "\${twitter-to-kafka-service.enable-mock-tweets}" +
            "&& not \${twitter-to-kafka-service.enable-v2-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-feign-tweets}"
)
class MockStreamRunnerImpl(
    private val mockProperties: MockProperties,
    private val twitterStatusListener: TwitterStatusListener
) : StreamRunner {

    override fun start() {
        with(mockProperties) {
            startMockTwitterStream(twitterKeywords, mockMinTweetLength, mockMaxTweetLength, mockSleepMs)
        }
    }

    private fun startMockTwitterStream(
        keywords: List<String>,
        minTweetLength: Int,
        maxTweetLength: Int,
        sleepMs: Long
    ) = runBlocking {
        launch {
            try {
                while (true) {
                    val status =
                        TwitterObjectFactory.createStatus(getFormattedTweet(keywords, minTweetLength, maxTweetLength))
                    twitterStatusListener.onStatus(status)
                    delay(sleepMs)
                }
            } catch (tse: TwitterStatusException) {
                logger.error("Error by trying create of twitter status", tse)
            }
        }
    }

    private suspend fun getFormattedTweet(keywords: List<String>, minTweetLength: Int, maxTweetLength: Int) =
        formatTweetAsJsonWithParams(
            arrayOf(
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
                getRandomLong(),
                getRandomTweetContent(keywords, minTweetLength, maxTweetLength),
                getRandomLong()
            )
        )

    private fun getRandomLong() = String().apply { Random.nextLong(Long.MAX_VALUE) }

    private suspend fun formatTweetAsJsonWithParams(params: Array<String>): String {
        var tweet = TWEET_AS_ROW_JSON
        params.onEachIndexed { index, param -> tweet = tweet.replace("{$index}", param) }
        return tweet
    }

    private suspend fun getRandomTweetContent(
        keywords: List<String>,
        minTweetLength: Int,
        maxTweetLength: Int
    ): String {
        val tweet = StringBuilder()
        val tweetLength = getRandomTweetLength(maxTweetLength, minTweetLength)
        return tweet.apply {
            for (i in 0 until tweetLength) {
                with(Random) {
                    append(words[nextInt(words.size)]).append(" ")
                    if (i == tweetLength / 2) {
                        append(keywords[nextInt(keywords.size)]).append(" ")
                    }
                }
            }
        }.toString()
    }

    private fun getRandomTweetLength(maxTweetLength: Int, minTweetLength: Int) =
        Random.nextInt(maxTweetLength - minTweetLength + 1) + minTweetLength

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MockStreamRunnerImpl::class.java)
        private const val TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy"
        private const val TWEET_AS_ROW_JSON = """
                {"created_at":"{0}","id":"{1}","text":"{2}","user":{"id":"{3}"}}
            """
        private val words = arrayOf(
            "Kotlin",
            "as",
            "a",
            "language",
            "provides",
            "only",
            "minimal",
            "low-level",
            "APIs",
            "in",
            "its",
            "standard",
            "library",
            "to",
            "enable",
            "various",
            "other",
            "libraries",
            "to",
            "utilize",
            "coroutines",
            "Unlike",
            "many",
            "other",
            "languages",
            "with",
            "similar",
            "capabilities",
            "async",
            "and",
            "await",
            "are",
            "not",
            "keywords",
            "in",
            "Kotlin",
            "and",
            "are",
            "not",
            "even",
            "part",
            "of",
            "its",
            "standard",
            "library",
            "Moreover",
            "Kotlin's",
            "concept",
            "of",
            "suspending",
            "function",
            "provides",
            "a",
            "safer",
            "and",
            "less",
            "error-prone",
            "abstraction",
            "for",
            "asynchronous",
            "operations",
            "than",
            "futures",
            "and",
            "promises"
        )
    }
}
