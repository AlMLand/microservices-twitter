repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    implementation(project(":kafka:kafka-admin"))
    implementation(project(":kafka:kafka-model"))
    implementation(project(":kafka:kafka-producer"))
    implementation(project(":app-configuration"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

docker {
    name = "${project.name}:${project.version}"
    setDockerfile(File("/src/main/docker/Dockerfile"))
    files("/build/libs/twitter-to-kafka-service-${project.version}.jar")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets {
    test {
        java.setSrcDirs(listOf("src/test/integrationtest", "src/test/unittest"))
    }
}