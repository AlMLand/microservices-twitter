import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("io.github.zafkiel1312.verifyfeign") version "0.4"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.21"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.5.0"
    id("com.palantir.docker") version "0.33.0"
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
}

group = "com.AlMLand"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

extra["twitter4j-stream.version"] = "4.0.7"
extra["avro.version"] = "1.11.1"
extra["kafka-avro-serializer.version"] = "7.3.1"
extra["kotlinx-coroutines-core.version"] = "1.6.4"
extra["jackson-module-kotlin.version"] = "2.14.1"
extra["json.version"] = "20220924"
extra["apache.httpclient.version"] = "4.5.14"
extra["openfeign.feign-jackson.version"] = "12.1"
extra["spring-retry.version"] = "2.0.0"
extra["spring-cloud-config-server.version"] = "4.0.0"

repositories {
    mavenCentral()
}

allprojects {
    apply {
        group = "${project.group}"
        version = "${project.version}"
        java.sourceCompatibility = JavaVersion.VERSION_17
        plugin("kotlin")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("io.github.zafkiel1312.verifyfeign")
        plugin("org.jetbrains.kotlin.plugin.allopen")
        tasks.withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "17"
            }
        }
    }
}

project(":server-configuration") {
    apply {
        dependencies {
            implementation("org.springframework.cloud:spring-cloud-config-server:${property("spring-cloud-config-server.version")}")
        }
    }
}

project(":app-configuration") {
    apply {
        dependencies {
            implementation("org.twitter4j:twitter4j-stream:${property("twitter4j-stream.version")}")
        }
    }
}

project(":common-configuration") {
    apply {
        dependencies {
            implementation("org.springframework.retry:spring-retry:${property("spring-retry.version")}")
        }
    }
}

project(":kafka:kafka-admin") {
    apply {
        dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("kotlinx-coroutines-core.version")}")
        }
    }
}

project(":kafka:kafka-producer") {
    apply {
        dependencies {
            implementation("io.confluent:kafka-avro-serializer:${property("kafka-avro-serializer.version")}")
        }
    }
}

project(":kafka:kafka-model") {
    apply {
        plugin("java")
        plugin("com.github.davidmc24.gradle.plugin.avro")
        dependencies {
            implementation("org.apache.avro:avro:${property("avro.version")}")
        }
    }
}

project(":twitter-to-kafka-service") {
    apply {
        plugin("com.palantir.docker")
        plugin("com.github.davidmc24.gradle.plugin.avro")
        dependencies {
            implementation("org.twitter4j:twitter4j-stream:${property("twitter4j-stream.version")}")
            implementation("org.apache.avro:avro:${property("avro.version")}")
            implementation("org.json:json:${property("json.version")}")
            implementation("io.github.openfeign:feign-jackson:${property("openfeign.feign-jackson.version")}")
            implementation("org.apache.httpcomponents:httpclient:${property("apache.httpclient.version")}")
            implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${property("jackson-module-kotlin.version")}")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("kotlinx-coroutines-core.version")}")
        }
    }
}