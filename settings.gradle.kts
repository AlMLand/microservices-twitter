rootProject.name = "microservices-twitter"
include("twitter-to-kafka-service")
include("app-configuration")
include("kafka")
include("kafka:kafka-admin")
findProject(":kafka:kafka-admin")?.name = "kafka-admin"
include("kafka:kafka-model")
findProject(":kafka:kafka-model")?.name = "kafka-model"
include("kafka:kafka-producer")
findProject(":kafka:kafka-producer")?.name = "kafka-producer"
include("common-configuration")
