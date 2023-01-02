package com.AlMLand.common

import com.AlMLand.common.HttpRequest.*
import com.AlMLand.twitterToKafkaService.TwitterProperties
import org.apache.http.HttpEntity
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.*
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader

// https://github.com/twitterdev/Twitter-API-v2-sample-code/blob/main/Filtered-Stream/FilteredStreamDemo.java

enum class HttpRequest { GET, POST, DEFAULT }

@Component
@ConditionalOnExpression(
    "\${twitter-to-kafka-service.enable-v2-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-feign-tweets} " +
            "&& not \${twitter-to-kafka-service.enable-mock-tweets}"
)
@SuppressWarnings("TooManyFunctions")
class TwitterV2StreamHelper(
    private val twitterProperties: TwitterProperties,
    private val commonTweetService: CommonTweetService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(TwitterV2StreamHelper::class.java)
        private const val DEFAULT_CHARSET = "UTF-8"
        private const val BODY_ADD_TEMPLATE = "{\"add\": [%s]}"
        private const val BODY_DELETE_TEMPLATE = "{\"delete\":{\"ids\":[%s]}}"
    }

    fun connectStream() {
        BufferedReader(InputStreamReader(getHttpResponseEntity(twitterProperties.twitterBearerToken).content)).run {
            var line = readLine()
            while (line != null) {
                commonTweetService.setTweetToTwitterStatusListener(line)
                line = readLine()
            }
        }
    }

    fun setupRules(rules: Map<String, String>) {
        getRules(twitterProperties.twitterBearerToken).run {
            if (isNotEmpty()) {
                deleteRules(this)
            }
        }
        createRules(twitterProperties.twitterBearerToken, rules).run {
            logger.info("Created rules for twitter stream {}", rules.keys.joinToString(", "))
        }
    }

    private fun createRules(bearerToken: String, rules: Map<String, String>) {
        getHttpClientHttpRequestRequestBody(
            bearerToken,
            rules = rules
        ).run {
            second.entity = third
            first.execute(second)
        }.run {
            entity
        }?.let {
            println(EntityUtils.toString(it, DEFAULT_CHARSET))
        }
    }

    private fun deleteRules(existingRules: List<String>) {
        getHttpClientHttpRequestRequestBody(
            twitterProperties.twitterBearerToken,
            existingRules = existingRules
        ).run {
            second.entity = third
            first.execute(second)
        }.run {
            entity
        }?.let {
            println(EntityUtils.toString(it, DEFAULT_CHARSET))
        }
    }

    private fun getHttpClientHttpRequestRequestBody(
        bearerToken: String,
        rules: Map<String, String>? = null,
        existingRules: List<String>? = null
    ): Triple<CloseableHttpClient, HttpEntityEnclosingRequestBase, StringEntity> =
        Triple(
            first = getHttpClient(),
            second = createHttpRequest(POST, bearerToken) as HttpEntityEnclosingRequestBase,
            third = StringEntity(rules?.let { getFormattedString(rules) } ?: existingRules?.let {
                getFormattedString(existingRules)
            })
        )

    private fun getRules(bearerToken: String): List<String> =
        getRulesFromJson(JSONObject(EntityUtils.toString(getHttpResponseEntity(bearerToken, GET), DEFAULT_CHARSET)))

    fun getRulesFromJson(json: JSONObject): List<String> =
        with(json) {
            if (length() > 1 && has("data")) {
                (get("data") as JSONArray).run {
                    asSequence().map {
                        (it as? JSONObject)?.getString("id") ?: ""
                    }.toList()
                }
            } else emptyList()
        }

    private fun getHttpResponseEntity(bearerToken: String, httpRequest: HttpRequest = DEFAULT): HttpEntity =
        when (httpRequest) {
            DEFAULT -> createHttpGet(bearerToken)
            GET -> createHttpRequest(GET, bearerToken)
            else -> throw IllegalArgumentException("Not allowed argument as request")
        }.let {
            getHttpClient().execute(it).entity
        }

    private fun getFormattedString(ids: List<String>): String =
        ids.joinToString(separator = "\",\"", prefix = "\"", postfix = "\"").let {
            String.format(BODY_DELETE_TEMPLATE, it)
        }

    private fun getFormattedString(rules: Map<String, String>): String =
        with(StringBuilder()) {
            rules.forEach { (key, value) -> append("{\"value\": \"$key\", \"tag\": \"$value\"},") }
            String.format(BODY_ADD_TEMPLATE, substring(0, length - 1))
        }

    private fun getHttpClient(): CloseableHttpClient = HttpClients.custom()
        .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build()

    private fun createHttpGet(bearerToken: String): HttpGet =
        HttpGet(URIBuilder(twitterProperties.twitterBaseUrl + twitterProperties.twitterTweetUrl).build()).apply {
            setHeader("Authorization", "Bearer $bearerToken")
        }

    private fun createHttpRequest(httpRequest: HttpRequest, bearerToken: String): HttpRequestBase =
        URIBuilder(twitterProperties.twitterBaseUrl + twitterProperties.twitterRulesUrl).let {
            when (httpRequest) {
                POST -> HttpPost(it.build())
                GET -> HttpGet(it.build())
                else -> throw IllegalArgumentException("Not allowed argument as request")
            }
        }.apply {
            setHeaders(
                arrayOf(
                    BasicHeader("Authorization", "Bearer $bearerToken"),
                    BasicHeader("Content-type", "application/json")
                )
            )
        }
}
