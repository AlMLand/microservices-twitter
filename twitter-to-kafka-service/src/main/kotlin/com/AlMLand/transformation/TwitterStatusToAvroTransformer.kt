package com.AlMLand.transformation

import com.AlMLand.kafka.avro.model.TwitterAvroModel
import org.springframework.stereotype.Component
import twitter4j.Status

@Component
class TwitterStatusToAvroTransformer {
    fun getAvroModel(status: Status) =
        with(status) {
            TwitterAvroModel(user.id, id, text, createdAt.time)
        }
}