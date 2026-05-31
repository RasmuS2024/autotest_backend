plugins {
    id("java")
    id("io.qameta.allure") version "4.0.2"
}

group = "framework"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.rest-assured:rest-assured:5.5.0")
    implementation("io.rest-assured:json-path:5.5.0")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("io.cucumber:cucumber-java:7.21.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.21.0")

    testImplementation("io.qameta.allure:allure-cucumber7-jvm:2.35.1")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    testImplementation("org.slf4j:slf4j-simple:2.0.16")
}

allure {
    version.set("2.35.1")
    adapter {
        aspectjWeaver.set(true)
        frameworks {
            junit5 {
                adapterVersion.set("2.35.1")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
}