import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.7.21"
}

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:3.1.5")
    implementation("org.twitter4j:twitter4j-stream:4.0.7")
    implementation("org.json:json:20220924")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")
    implementation("io.github.openfeign:feign-jackson:9.3.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
