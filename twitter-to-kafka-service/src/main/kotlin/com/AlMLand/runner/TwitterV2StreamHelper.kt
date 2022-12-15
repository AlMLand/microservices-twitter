package com.AlMLand.runner

import com.AlMLand.config.Configuration
import com.AlMLand.listener.TwitterStatusListener
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component
import twitter4j.Status
import twitter4j.TwitterException
import twitter4j.TwitterObjectFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale.ENGLISH

// https://github.com/twitterdev/Twitter-API-v2-sample-code/blob/main/Filtered-Stream/FilteredStreamDemo.java
private val logger = LoggerFactory.getLogger(TwitterV2StreamHelper::class.java)
private const val TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy"
private const val TWEET_AS_ROW_JSON = """
    {"created_at":"{0}","id":"{1}","text":"{2}","user":{"id":"{3}"}}
"""

@Component
@ConditionalOnExpression("\${twitter-to-kafka-service.enable-v2-tweets} && not \${twitter-to-kafka-service.enable-mock-tweets}")
open class TwitterV2StreamHelper(
    private val configuration: Configuration,
    private val twitterStatusListener: TwitterStatusListener
) {

    fun connectStream(bearerToken: String) {
        val httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build()
        val uriBuilder = URIBuilder(configuration.twitterV2BaseUrl)
        val httpGet = HttpGet(uriBuilder.build())
        httpGet.setHeader("Authorization", "Bearer $bearerToken")

        val httpResponse = httpClient.execute(httpGet)
        val entity = httpResponse.entity
        entity?.let {
            val reader = BufferedReader(InputStreamReader(entity.content))
            var line = reader.readLine()
            while (line != null) {
                line = reader.readLine()
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
        }
    }

    fun setupRules(bearerToken: String, rules: Map<String, String>) {
        val existingRules = getRules(bearerToken)
        if (existingRules.isNotEmpty()) {
            deleteRules(bearerToken, existingRules)
        }
        createRules(bearerToken, rules)
        logger.info("Created rules for twitter stream {}", rules.keys.joinToString(", "))
    }

    private fun createRules(bearerToken: String, rules: Map<String, String>) {
        val httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build()
        val uriBuilder = URIBuilder(configuration.twitterV2RulesBaseUrl)
        val httpPost = HttpPost(uriBuilder.build())
        httpPost.setHeaders(
            arrayOf(
                BasicHeader("Authorization", "Bearer $bearerToken"),
                BasicHeader("Content-type", "application/json")
            )
        )
        val body = StringEntity(getFormattedString("{\"add\": [%s]}", rules))
        httpPost.entity = body
        val response = httpClient.execute(httpPost)
        val entity = response.entity
        entity?.let { println(EntityUtils.toString(entity, "UTF-8")) }
    }

    private fun deleteRules(bearerToken: String, existingRules: List<String>) {
        val httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build()
        val uriBuilder = URIBuilder(configuration.twitterV2RulesBaseUrl)
        val httpPost = HttpPost(uriBuilder.build())
        httpPost.setHeaders(
            arrayOf(
                BasicHeader("Authorization", "Bearer $bearerToken"),
                BasicHeader("Content-type", "application/json")
            )
        )
        val body = StringEntity(getFormattedString("{\"delete\":{\"ids\":[%s]}}", existingRules))
        httpPost.entity = body
        val response = httpClient.execute(httpPost)
        val httpEntity = response.entity
        httpEntity?.let { println(EntityUtils.toString(httpEntity, "UTF-8")) }
    }

    private fun getRules(bearerToken: String): List<String> {
        val httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build()
        val uriBuilder = URIBuilder(configuration.twitterV2RulesBaseUrl)
        val httpGet = HttpGet(uriBuilder.build())
        httpGet.setHeaders(
            arrayOf(
                BasicHeader("Authorization", "Bearer $bearerToken"),
                BasicHeader("Content-type", "application/json")
            )
        )
        val httpResponse = httpClient.execute(httpGet)
        val entity = httpResponse.entity
        entity?.let {
            val json = JSONObject(EntityUtils.toString(entity, "UTF-8"))
            if (json.length() > 1 && json.has("data")) {
                val jsonArray = json.get("data") as JSONArray
                return jsonArray.asSequence().map {
                    (it as? JSONObject)?.getString("id") ?: ""
                }.toList()
            }
        }
        return emptyList()
    }

    private fun getFormattedString(string: String, ids: List<String>): String {
        val result = when (ids.size) {
            1 -> "\"${ids[0]}\""
            else -> {
                val sb = StringBuilder()
                for (id in ids) {
                    sb.append("\"$id\",")
                }
                sb.substring(0, sb.length - 1)
            }
        }
        return String.format(string, result)
    }

    private fun getFormattedString(string: String, rules: Map<String, String>): String {
        return when (rules.size) {
            1 -> {
                val key = rules.keys.first()
                String.format(string, "{\"value\": \"$key\", \"tag\": \"${rules[key]}\"}")
            }

            else -> {
                val sb = StringBuilder()
                for ((key, value) in rules) {
                    sb.append("{\"value\": \"$key\", \"tag\": \"$value\"},")
                }
                return String.format(string, sb.toString().substring(0, sb.length - 1))
            }
        }
    }

    private fun getFormattedTweet(data: String?): String {
        val jsonData = JSONObject(data).get("data") as JSONObject
        val params = arrayOf(
            ZonedDateTime.parse(jsonData.get("created_at").toString()).withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, ENGLISH)),
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
