val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    application
}

group = "me.bottdev"
version = "0.1"

dependencies {
    implementation(project(":lumen-core"))
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("io.ktor:ktor-server-test-host")
    implementation("org.jetbrains.exposed:exposed-core:0.38.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.38.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.38.2")
    implementation("mysql:mysql-connector-java:8.0.29")
    implementation("org.mindrot:jbcrypt:0.4")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "me.bottdev.lumenserver.ApplicationKt"
    }
}

application {
    mainClass.set("me.bottdev.lumenserver.ApplicationKt")
}
