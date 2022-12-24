package com.AlMLand.common

import com.AlMLand.common.HttpRequest.*
import com.AlMLand.twittertokafkaservice.TwitterProperties
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
        val entity = getHttpResponseEntity(twitterProperties.twitterBearerToken)
        entity.let {
            val reader = BufferedReader(InputStreamReader(entity.content))
            var line = reader.readLine()
            while (line != null) {
                line = reader.readLine()
                commonTweetService.setTweetToTwitterStatusListener(line)
            }
        }
    }

    fun setupRules(rules: Map<String, String>) {
        val bearerToken = twitterProperties.twitterBearerToken
        val existingRules = getRules(bearerToken)
        if (existingRules.isNotEmpty()) {
            deleteRules(existingRules)
        }
        createRules(bearerToken, rules)
        logger.info("Created rules for twitter stream {}", rules.keys.joinToString(", "))
    }

    private fun createRules(bearerToken: String, rules: Map<String, String>) {
        val (httpClient, httpPost, body) = getHttpClientHttpRequestRequestBody(
            bearerToken,
            rules = rules
        )
        httpPost.entity = body
        val response = httpClient.execute(httpPost)
        val entity = response.entity
        entity?.let { println(EntityUtils.toString(entity, DEFAULT_CHARSET)) }
    }

    private fun deleteRules(existingRules: List<String>) {
        val (httpClient, httpPost, body) = getHttpClientHttpRequestRequestBody(
            twitterProperties.twitterBearerToken,
            existingRules = existingRules
        )
        httpPost.entity = body
        val response = httpClient.execute(httpPost)
        val httpEntity = response.entity
        httpEntity?.let { println(EntityUtils.toString(httpEntity, DEFAULT_CHARSET)) }
    }

    private fun getHttpClientHttpRequestRequestBody(
        bearerToken: String,
        rules: Map<String, String>? = null,
        existingRules: List<String>? = null
    ): Triple<CloseableHttpClient, HttpEntityEnclosingRequestBase, StringEntity> {
        val httpClient = getHttpClient()
        val httpPost = createHttpRequest(POST, bearerToken) as HttpEntityEnclosingRequestBase
        val body = StringEntity(rules?.let { getFormattedString(rules) } ?: existingRules?.let {
            getFormattedString(existingRules)
        })
        return Triple(httpClient, httpPost, body)
    }

    private fun getRules(bearerToken: String): List<String> {
        val entity = getHttpResponseEntity(bearerToken, GET)
        entity.let {
            val json = JSONObject(EntityUtils.toString(entity, DEFAULT_CHARSET))
            return getRulesFromJson(json)
        }
    }

    fun getRulesFromJson(json: JSONObject): List<String> {
        if (json.length() > 1 && json.has("data")) {
            val jsonArray = json.get("data") as JSONArray
            return jsonArray.asSequence().map {
                (it as? JSONObject)?.getString("id") ?: ""
            }.toList()
        }
        return emptyList()
    }

    private fun getHttpResponseEntity(bearerToken: String, httpRequest: HttpRequest = DEFAULT): HttpEntity {
        val httpClient = getHttpClient()
        val request = when (httpRequest) {
            DEFAULT -> createHttpGet(bearerToken)
            GET -> createHttpRequest(GET, bearerToken)
            else -> throw IllegalArgumentException("Not allowed argument as request")
        }
        val httpResponse = httpClient.execute(request)
        return httpResponse.entity
    }

    private fun getFormattedString(ids: List<String>): String {
        val result = ids.joinToString(separator = "\",\"", prefix = "\"", postfix = "\"")
        return String.format(BODY_DELETE_TEMPLATE, result)
    }

    private fun getFormattedString(rules: Map<String, String>): String {
        val sb = StringBuilder()
        rules.forEach { (key, value) -> sb.append("{\"value\": \"$key\", \"tag\": \"$value\"},") }
        return String.format(BODY_ADD_TEMPLATE, sb.toString().substring(0, sb.length - 1))
    }

    private fun getHttpClient(): CloseableHttpClient = HttpClients.custom()
        .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build()

    private fun createHttpGet(bearerToken: String): HttpGet {
        val uriBuilder = URIBuilder(twitterProperties.twitterBaseUrl + twitterProperties.twitterTweetUrl)
        val httpGet = HttpGet(uriBuilder.build())
        httpGet.setHeader("Authorization", "Bearer $bearerToken")
        return httpGet
    }

    private fun createHttpRequest(httpRequest: HttpRequest, bearerToken: String): HttpRequestBase {
        val uriBuilder = URIBuilder(twitterProperties.twitterBaseUrl + twitterProperties.twitterRulesUrl)
        val request = when (httpRequest) {
            POST -> HttpPost(uriBuilder.build())
            GET -> HttpGet(uriBuilder.build())
            else -> throw IllegalArgumentException("Not allowed argument as request")
        }
        request.setHeaders(
            arrayOf(
                BasicHeader("Authorization", "Bearer $bearerToken"),
                BasicHeader("Content-type", "application/json")
            )
        )
        return request
    }
}