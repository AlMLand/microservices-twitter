package com.AlMLand.exception

class TwitterStatusException(message: String) : RuntimeException(message)
class StreamTweetsException(message: String) : RuntimeException(message)
class TwitterServerConnectException(message: String) : RuntimeException(message)