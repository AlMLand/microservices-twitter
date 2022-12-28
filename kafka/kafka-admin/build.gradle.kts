plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.7.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app-configuration"))
    implementation(project(":common-configuration"))
    implementation("org.springframework.kafka:spring-kafka:3.0.0")
}
