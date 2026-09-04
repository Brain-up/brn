import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotestAssertionsVersion: String by properties
val kotlinVersion: String by properties
val flywayVersion: String by properties
val commonsLang3Version: String by properties
val assertjVersion: String by properties
val plexusUtilsVersion: String by properties
val log4jApiKotlinVersion: String by properties
val jsonVersion: String by properties
val junitVersion: String by properties
val mockkVersion: String by properties
val testContainersVersion: String by properties
val okhttp3Version: String by properties
val kotlinxCoroutinesCoreVersion: String by properties
val springCloudContractWiremockVersion: String by properties
val springDocOpenApiVersion: String by properties

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.jetbrains.kotlin.plugin.allopen")
    jacoco
    id("org.sonarqube") version "6.2.0.5505"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("software.amazon.awssdk:bom:2.31.78")
    }
    dependencies {
        // Override the version managed by the Spring Boot BOM (3.17.0) to patch CVE-2025-48924.
        dependency("org.apache.commons:commons-lang3:$commonsLang3Version")
        // Override the version managed by the Spring Boot BOM (3.27.3) to patch CVE-2026-24400 (XXE in isXmlEqualTo).
        dependency("org.assertj:assertj-core:$assertjVersion")
        // Force the version pulled transitively via spring-cloud-contract-wiremock (3.5.1) to patch CVE-2025-67030 (path traversal / RCE).
        dependency("org.codehaus.plexus:plexus-utils:$plexusUtilsVersion")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
    testImplementation("org.springframework.security:spring-security-test")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    implementation("com.google.firebase:firebase-admin:9.9.0")

    implementation("com.auth0:java-jwt:4.5.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesCoreVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxCoroutinesCoreVersion")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:$log4jApiKotlinVersion")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion")

    implementation("software.amazon.awssdk:s3")
    implementation("com.google.cloud:google-cloud-storage:2.69.0")

    implementation("org.json:json:$jsonVersion")
    implementation("commons-io:commons-io:2.17.0")

    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:$springCloudContractWiremockVersion")
    testImplementation("org.amshove.kluent:kluent:1.68") // should be deleted after kotest move all of it
    testImplementation(kotlin("test")) // should be deleted after kotest move all of it
    testImplementation("io.kotest:kotest-assertions-core:$kotestAssertionsVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")

    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:testcontainers-postgresql:$testContainersVersion")
    testImplementation("org.testcontainers:testcontainers-jdbc:$testContainersVersion")
    testImplementation("org.testcontainers:testcontainers-localstack:$testContainersVersion")
    testImplementation("com.squareup.okhttp3:okhttp:$okhttp3Version")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttp3Version")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// --- ktlint - kotlin code style plugin ---
val ktlint by configurations.creating

configurations {
    ktlint
}

dependencies {
    ktlint("com.pinterest.ktlint:ktlint-cli:1.5.0") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
    // ktlint(project(":custom-ktlint-ruleset")) // in case of custom ruleset
}

val ktlintCheck by tasks.registering(JavaExec::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
    args(
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

tasks.check {
    dependsOn(ktlintCheck)
}

tasks.register<JavaExec>("ktlintFormat") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style and format"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
    args(
        "-F",
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

providers
    .exec {
        commandLine("git", "config", "core.hooksPath", ".githooks")
    }.result
    .get()

tasks.named("compileKotlin") { dependsOn("ktlintCheck") }

tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration-test")
    }
    finalizedBy("jacocoTestReport")
}

tasks.withType<JacocoReport> {
    dependsOn("test")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("jacoco/coverage.xml"))
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacoco/html"))
    }
    afterEvaluate {
        classDirectories.setFrom(
            files(
                classDirectories.files.map {
                    fileTree(it).apply {
                        exclude(
                            "com/epam/brn/dto/**",
                            "com/epam/brn/model/**",
                            "com/epam/brn/config/**",
                            "com/epam/brn/exception/**",
                            "com/epam/brn/Application*",
                            "com/epam/brn/service/azure/tts/config/**",
                            "com/epam/brn/webclient/customizer/**",
                            "com/epam/brn/webclient/model/**",
                        )
                    }
                },
            ),
        )
    }
    executionData.setFrom(layout.buildDirectory.file("jacoco/test.exec"))
}

tasks.register<Test>("integrationTest") {
    useJUnitPlatform { includeTags("integration-test") }
    mustRunAfter(tasks["test"])
    group = "Verification"
    description = "Runs the integration tests on Postgres Test Container."
}

sonarqube {
    properties {
        // Root project information
        property("sonar.projectKey", "Brain-up_brn")
        property("sonar.organization", "brain-up")
        property("sonar.host.url", "https://sonarcloud.io")

        property("sonar.coverage.jacoco.xmlReportPaths", "./build/jacoco/coverage.xml")
        property("sonar.language", "kotlin")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.working.directory", "./build/sonar")
        property(
            "sonar.coverage.exclusions",
            "**/com/epam/brn/dto/**," +
                "**/com/epam/brn/model/**," +
                "**/com/epam/brn/config/**," +
                "**/com/epam/brn/exception/**," +
                "**/com/epam/brn/Application*," +
                "**/com/epam/brn/service/load/InitialDataLoader*," +
                "**/com/epam/brn/service/load/FirebaseUserDataLoader*," +
                "**/com/epam/brn/service/azure/tts/AzureVoiceLoader*," +
                "**/com/epam/brn/service/azure/tts/config/**," +
                "**/com/epam/brn/webclient/customizer/**," +
                "**/com/epam/brn/webclient/model/**",
        )
    }
}
