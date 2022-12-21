package com.AlMLand.exception

class StreamTweetsException(message: String) : RuntimeException(message)

class RequestTweetException(message: String) : RuntimeException(message)

class TwitterStatusException(message: String) : RuntimeException(message)
