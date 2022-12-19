package com.AlMLand.feign.controller

import feign.Body
import feign.Headers
import feign.Param
import feign.RequestLine

@Headers("Authorization: Bearer {bearerToken}")
interface TwitterFeignClient {
    @RequestLine("GET /2/tweets/search/stream?tweet.fields=created_at&expansions=author_id")
    fun getTweet(@Param("bearerToken") bearerToken: String): LinkedHashMap<String, Any>

    @RequestLine("GET /2/tweets/search/stream/rules")
    @Headers("Content-type: application/json")
    fun getRules(@Param("bearerToken") bearerToken: String): LinkedHashMap<String, Any>

    @RequestLine("POST /2/tweets/search/stream/rules")
    @Headers("Content-type: application/json")
    @Body("{rules}")
    fun deleteRules(@Param("bearerToken") bearerToken: String, @Param("rules") rules: String)

    @RequestLine("POST /2/tweets/search/stream/rules")
    @Headers("Content-type: application/json")
    @Body("{rules}")
    fun createRules(@Param("bearerToken") bearerToken: String, @Param("rules") rules: String)
}