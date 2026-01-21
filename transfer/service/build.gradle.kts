dependencies {
    api(project(":transfer:model"))
    implementation(project(":transfer:infrastructure"))
    implementation(project(":transfer:exception"))
    implementation(project(":account:infrastructure"))
    implementation(project(":member:infrastructure"))
    // spring tx
    implementation("org.springframework:spring-tx")
}
