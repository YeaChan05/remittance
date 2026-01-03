dependencies {
    implementation(project(":ledger:api"))
    implementation(project(":ledger:repository-jpa"))

    integrationTestImplementation("org.testcontainers:junit-jupiter")
    integrationTestImplementation("org.testcontainers:mysql")
    integrationTestImplementation(testFixtures(project(":common:application-api")))

    runtimeOnly("com.mysql:mysql-connector-j") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }

    integrationTestRuntimeOnly("com.mysql:mysql-connector-j") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
}
