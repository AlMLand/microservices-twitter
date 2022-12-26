import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

project("kafka-model") {
    apply {
        group = "com.AlMLand"
        version = "0.0.1-SNAPSHOT"
        plugin("io.github.zafkiel1312.verifyfeign")
    }
}

project("kafka-admin") {
    apply {
        group = "com.AlMLand"
        version = "0.0.1-SNAPSHOT"
        plugin("io.github.zafkiel1312.verifyfeign")
    }
}

project("kafka-producer") {
    apply {
        group = "com.AlMLand"
        version = "0.0.1-SNAPSHOT"
        plugin("io.github.zafkiel1312.verifyfeign")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}