repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app-configuration"))
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-starter-aop")
}
