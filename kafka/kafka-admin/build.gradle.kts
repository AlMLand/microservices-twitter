plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.7.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app-configuration"))
    implementation(project(":common-configuration"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.springframework.kafka:spring-kafka:3.0.0")
    implementation("org.springframework.boot:spring-boot-starter:3.0.0")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.0.0")
}
