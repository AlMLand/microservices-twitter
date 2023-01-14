plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.7.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app-configuration"))
    implementation("org.springframework.kafka:spring-kafka")
}
