dependencies {
    api(project(":member:model"))
    implementation(project(":member:infrastructure"))
    implementation(project(":member:exception"))
    implementation(rootProject.libs.jjwt.api)
    implementation(rootProject.libs.jjwt.impl)
    implementation(rootProject.libs.jjwt.jackson)
}
