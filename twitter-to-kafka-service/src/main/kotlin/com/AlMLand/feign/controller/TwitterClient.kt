package com.AlMLand.feign.controller

import feign.Headers
import feign.Param
import feign.RequestLine

interface TwitterClient {
    @RequestLine("GET")
    @Headers("Authorization: Bearer {bearerToken}")
    fun getTweet(@Param("bearerToken") bearerToken: String): LinkedHashMap<String, Any>
}