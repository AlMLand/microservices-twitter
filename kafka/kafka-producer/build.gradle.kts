plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.7.21"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    implementation(project(":app-configuration"))
    implementation(project(":kafka:kafka-model"))
    implementation("org.springframework.kafka:spring-kafka:3.0.0")
    implementation("io.confluent:kafka-avro-serializer:7.3.1")
}
