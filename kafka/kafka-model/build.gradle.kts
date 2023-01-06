// wenn man die files nicht im build(als 'generated source root') haben möchte, sondern direkt in 'java' ordner
tasks.withType<com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask> {
    source(file("src/main/resources/"))
    setOutputDir(file("src/main/java/"))
}
