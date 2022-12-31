package com.AlMLand.common

import com.AlMLand.listener.TwitterStatusListener
import com.AlMLand.twitterToKafkaService.TwitterProperties
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import twitter4j.TwitterException
import twitter4j.TwitterObjectFactory
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class CommonTweetService(
    private val twitterStatusListener: TwitterStatusListener,
    private val twitterProperties: TwitterProperties
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CommonTweetService::class.java)
        private const val TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy"
        private const val TWEET_AS_ROW_JSON = """
                {"created_at":"{0}","id":"{1}","text":"{2}","user":{"id":"{3}"}}
            """
    }

    fun getRules(): Map<String, String> =
        twitterProperties.twitterKeywords.let {
            logger.info("Created filter for twitter stream for keywords {}", it)
            it.associateWith { element -> "Keyword: $element" }
        }

    fun setTweetToTwitterStatusListener(line: String) {
        if (line.isNotEmpty()) {
            getFormattedTweet(line).let {
                try {
                    TwitterObjectFactory.createStatus(it)?.run {
                        twitterStatusListener.onStatus(this)
                    }
                } catch (te: TwitterException) {
                    logger.error("Could not create status for text: {}", it, te)
                }
            }
        }
    }

    private fun getFormattedTweet(data: String?): String =
        (JSONObject(data).get("data") as JSONObject).run {
            arrayOf(
                ZonedDateTime.parse(get("created_at").toString()).withZoneSameInstant(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
                get("id").toString(),
                get("text").toString().replace("\"", "\\\\\""),
                get("author_id").toString()
            )
        }.let {
            formatTweetAsJsonWithParams(it)
        }

    private fun formatTweetAsJsonWithParams(params: Array<String>): String =
        TWEET_AS_ROW_JSON.apply {
            forEachIndexed { index, value -> replace("{$index}", value.toString()) }
        }
}
