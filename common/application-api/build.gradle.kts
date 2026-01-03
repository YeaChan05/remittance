import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    `java-test-fixtures`
}

dependencies {
    testFixturesImplementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))

    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-web")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-restclient-test")

    testFixturesImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testFixturesImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}
