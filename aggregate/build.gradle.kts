dependencies {
    implementation(project(":common:security"))


    implementation(project(":account:api"))
    implementation(project(":account:repository-jpa"))
    implementation(project(":account:mq-rabbitmq"))

    implementation(project(":transfer:api"))
    implementation(project(":transfer:repository-jpa"))
    implementation(project(":transfer:mq-rabbitmq"))

    implementation(project(":member:api"))
    implementation(project(":member:repository-jpa"))

    integrationTestImplementation("org.testcontainers:junit-jupiter")
    integrationTestImplementation("org.testcontainers:mysql")
    integrationTestImplementation("org.testcontainers:rabbitmq")
    integrationTestImplementation(testFixtures(project(":common:application-api")))
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    integrationTestImplementation(project(":account:model"))
    integrationTestImplementation(project(":member:model"))
    integrationTestImplementation(project(":transfer:model"))
    integrationTestImplementation(project(":transfer:infrastructure"))

    runtimeOnly("com.mysql:mysql-connector-j") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }


    integrationTestRuntimeOnly("com.mysql:mysql-connector-j") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }

    integrationTestRuntimeOnly("org.testcontainers:testcontainers")
    integrationTestRuntimeOnly("org.testcontainers:jdbc")
    integrationTestRuntimeOnly("org.testcontainers:rabbitmq")
}
