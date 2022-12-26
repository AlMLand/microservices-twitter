plugins {
    id("java")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.5.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.avro:avro:1.11.1")
}

// wenn man die files nicht im build(als 'generated source root') haben möchte, sondern direkt in 'java' ordner
//tasks.withType<com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask> {
//    setOutputDir(file("src/main/java/"))
//}
