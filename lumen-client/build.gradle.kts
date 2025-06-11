plugins {
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
}

group = "me.bottdev"
version = "1.0.1"

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
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks.test {
    useJUnitPlatform()
}


configure<PublishingExtension> {
    publications.create<MavenPublication>("lumen-client") {
        groupId = "me.bottdev"
        artifactId = "lumen-client"
        version = "${project.version}"
        pom.packaging = "jar"
        artifact("$buildDir/libs/lumen-client-${version}.jar")

    }
    repositories {
        mavenLocal()
    }
}

publishing {
    repositories {
        maven {
            name = "LumenClient"
            url = uri("http://mc.the-light.online:9000/private")
            isAllowInsecureProtocol  = true
            credentials(PasswordCredentials::class) {
                username = System.getenv("LumenClientUsername")
                password = System.getenv("LumenClientPassword")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.bottdev"
            artifactId = "lumen-client"
            version = "${project.version}"
            from(components["java"])
        }
    }
}

