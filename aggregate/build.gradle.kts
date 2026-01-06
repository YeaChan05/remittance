plugins {
    alias(libs.plugins.kover)
}
dependencies {
    implementation(project(":common:security"))

    implementation(project(":account:api"))
    implementation(project(":account:repository-jpa"))

    implementation(project(":ledger:api"))
    implementation(project(":ledger:repository-jpa"))

    implementation(project(":member:api"))
    implementation(project(":member:repository-jpa"))

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

kover {
    reports {
        total {
            html {
                onCheck.set(true)
            }
            xml {
                onCheck.set(true)
            }
            verify {
                onCheck.set(true)
                rule {
                    minBound(90)
                }
            }
        }
    }
}
