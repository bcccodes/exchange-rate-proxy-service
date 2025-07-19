plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.company.team.squad"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val logging by extra("6.0.9")
val logstash by extra("7.4")
val resilience4j by extra("2.3.0")
val mockkVersion by extra("1.14.0")

dependencies {

    //Spring Reactive Web
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    //-- Community lib
    implementation("io.github.oshai:kotlin-logging-jvm:$logging")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstash")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:$resilience4j")
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4j")
    testImplementation("io.mockk:mockk:${mockkVersion}")

    //-- Default
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    //testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
