package com.AlMLand.common

import com.AlMLand.config.TwitterProperties
import com.AlMLand.listener.TwitterStatusListener
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import twitter4j.Status
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

    fun getRules(): Map<String, String> {
        val keywords = twitterProperties.twitterKeywords
        val rules = keywords.associateWith { "Keyword: $it" }
        logger.info("Created filter for twitter stream for keywords {}", keywords)
        return rules
    }

    fun setTweetToTwitterStatusListener(line: String) {
        if (line.isNotEmpty()) {
            val tweet = getFormattedTweet(line)
            var status: Status? = null
            try {
                status = TwitterObjectFactory.createStatus(tweet)
            } catch (te: TwitterException) {
                logger.error("Could not create status for text: {}", tweet, te)
            }
            status?.let { twitterStatusListener.onStatus(status) }
        }
    }

    private fun getFormattedTweet(data: String?): String {
        val jsonData = JSONObject(data).get("data") as JSONObject
        val params = arrayOf(
            ZonedDateTime.parse(jsonData.get("created_at").toString()).withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
            jsonData.get("id").toString(),
            jsonData.get("text").toString().replace("\"", "\\\\\""),
            jsonData.get("author_id").toString()
        )
        return formatTweetAsJsonWithParams(params)
    }

    private fun formatTweetAsJsonWithParams(params: Array<String>): String {
        var tweet = TWEET_AS_ROW_JSON
        for ((index, value) in params.withIndex()) {
            tweet = tweet.replace("{$index}", value)
        }
        return tweet
    }

}
