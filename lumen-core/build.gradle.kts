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
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}




configure<PublishingExtension> {
    publications.create<MavenPublication>("lumen-core") {
        groupId = "me.bottdev"
        artifactId = "lumen-core"
        version = "${project.version}"
        pom.packaging = "jar"
        artifact("$buildDir/libs/lumen-core-${version}.jar")

    }
    repositories {
        mavenLocal()
    }
}

publishing {
    repositories {
        maven {
            name = "LumenCore"
            url = uri("http://mc.the-light.online:9000/private")
            isAllowInsecureProtocol  = true
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.bottdev"
            artifactId = "lumen-core"
            version = "${project.version}"
            from(components["java"])
        }
    }
}
