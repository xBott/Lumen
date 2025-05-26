plugins {
    kotlin("jvm") version "1.9.22"
}

group = "me.bottdev"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lumen-core"))
    implementation("io.ktor:ktor-client-core:2.3.4")
    implementation("io.ktor:ktor-client-cio:2.3.4")
    implementation("io.ktor:ktor-client-websockets:2.3.4")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

tasks.test {
    useJUnitPlatform()
}