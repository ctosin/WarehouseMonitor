plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    application
}

group = "monitor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.4.0"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.10.2"))

    implementation("io.ktor:ktor-network:2.3.12")
    implementation("com.hivemq:hivemq-mqtt-client:1.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.slf4j:slf4j-api:2.0.18")
    implementation("ch.qos.logback:logback-classic:1.6.3")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("monitor.MonitorAppKt")
}

tasks.test {
    useJUnitPlatform()
}
