plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.7.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app-configuration"))
    implementation("org.springframework.boot:spring-boot:3.0.0")
    implementation("org.springframework.retry:spring-retry:2.0.0")
    implementation("org.springframework.boot:spring-boot-starter-aop:3.0.0")
}
