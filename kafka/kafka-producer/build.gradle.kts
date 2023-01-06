repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    implementation(project(":app-configuration"))
    implementation(project(":kafka:kafka-model"))
    implementation("org.springframework.kafka:spring-kafka")
}
