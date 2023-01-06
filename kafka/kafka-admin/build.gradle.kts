repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app-configuration"))
    implementation(project(":common-configuration"))
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}
