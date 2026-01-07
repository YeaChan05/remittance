rootProject.name = "remittance"

pluginManagement {
    buildscript {
        repositories {
            gradlePluginPortal()
        }
    }

    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":account:api")
include(":account:model")
include(":account:service")
include(":account:exception")
include(":account:infrastructure")
include(":account:repository-jpa")

include(":ledger:api")
include(":ledger:model")
include(":ledger:service")
include(":ledger:exception")
include(":ledger:infrastructure")
include(":ledger:repository-jpa")

include(":member:api")
include(":member:api-internal")
include(":member:model")
include(":member:service")
include(":member:exception")
include(":member:infrastructure")
include(":member:repository-jpa")

include(":auth:api")
include(":auth:service")
include(":auth:exception")
include(":auth:infrastructure")


include(":common:application-api")
include(":common:api")
include(":common:exception")
include(":common:repository-jpa")
include(":common:security")

include(":aggregate")
