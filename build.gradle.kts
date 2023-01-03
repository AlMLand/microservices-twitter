plugins {
    id("org.springframework.boot") version "3.0.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("io.github.zafkiel1312.verifyfeign") version "0.4"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.21"
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
}

group = "com.AlMLand"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

project("twitter-to-kafka-service") {
    apply {
        group = "com.AlMLand"
        version = "0.0.1-SNAPSHOT"
        plugin("kotlin")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("io.github.zafkiel1312.verifyfeign")
        plugin("org.jetbrains.kotlin.plugin.allopen")
    }
}

project("app-configuration") {
    apply {
        group = "com.AlMLand"
        version = "0.0.1-SNAPSHOT"
        plugin("kotlin")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("io.github.zafkiel1312.verifyfeign")
        plugin("org.jetbrains.kotlin.plugin.allopen")
    }
}

project("kafka") {
    apply {
        group = "com.AlMLand"
        version = "0.0.1-SNAPSHOT"
        plugin("kotlin")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("io.github.zafkiel1312.verifyfeign")
        plugin("org.jetbrains.kotlin.plugin.allopen")
    }
}

project("common-configuration") {
    apply {
        group = "com.AlMLand"
        version = "0.0.1-SNAPSHOT"
        plugin("kotlin")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("io.github.zafkiel1312.verifyfeign")
        plugin("org.jetbrains.kotlin.plugin.allopen")
    }
}
