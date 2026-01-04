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

include(":account:application-api")
include(":account:api")
include(":account:model")
include(":account:service")
include(":account:exception")
include(":account:infrastructure")
include(":account:repository-jpa")

include(":ledger:application-api")
include(":ledger:api")
include(":ledger:model")
include(":ledger:service")
include(":ledger:exception")
include(":ledger:infrastructure")
include(":ledger:repository-jpa")

include(":common:application-api")
include(":common:api")
include(":common:exception")
include(":common:repository-jpa")

include(":aggregate")
