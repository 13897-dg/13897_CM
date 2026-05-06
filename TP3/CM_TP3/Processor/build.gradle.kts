plugins {
    kotlin("jvm")
    kotlin("kapt")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))

    implementation("com.google.auto.service:auto-service:1.1.1")
    kapt("com.google.auto.service:auto-service:1.1.1")
    implementation("com.squareup:kotlinpoet:1.14.2")
    implementation(project(":Annotations"))
}

kapt {
    correctErrorTypes = true
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}