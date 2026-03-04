import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotestAssertionsVersion: String by properties
val kotlinVersion: String by properties
val flywayVersion: String by properties
val log4jApiKotlinVersion: String by properties
val jsonVersion: String by properties
val junitVersion: String by properties
val mockkVersion: String by properties
val testContainersVersion: String by properties
val okhttp3Version: String by properties
val kotlinxCoroutinesCoreVersion: String by properties
val springCloudContractWiremockVersion: String by properties
val springDocOpenApiVersion: String by properties
val awsSdkVersion: String by properties
val postgresqlVersion: String by properties

plugins {
    id("org.springframework.boot") version "3.1.12"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.jpa") version "2.1.20"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.1.20"
    jacoco
    id("org.sonarqube") version "5.1.0.4882"
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
        mavenBom("software.amazon.awssdk:bom:$awsSdkVersion")
        mavenBom("org.junit:junit-bom:$junitVersion")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.security:spring-security-test")

    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    implementation("com.google.firebase:firebase-admin:9.4.3")

    implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesCoreVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxCoroutinesCoreVersion")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:$log4jApiKotlinVersion")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion")
//    implementation("org.springdoc:springdoc-openapi-kotlin:2.2.0")

    implementation("software.amazon.awssdk:s3")
    implementation("com.google.cloud:google-cloud-storage:2.45.0")

    implementation("org.json:json:$jsonVersion")
    implementation("commons-io:commons-io:2.18.0")

    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:$springCloudContractWiremockVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestAssertionsVersion")
    testImplementation("org.amshove.kluent:kluent:1.73") // To be removed after full migration to Kotest
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion") // To be removed after full migration to Kotest

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
//        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    // JUnit - версии будут из BOM
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")

//    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
//    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.natpryce:hamkrest:1.8.0.1")

    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("org.testcontainers:localstack:$testContainersVersion")
    testImplementation("com.amazonaws:aws-java-sdk-s3:1.12.780")
    testImplementation("com.squareup.okhttp3:okhttp:$okhttp3Version")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttp3Version")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
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

tasks.named("compileKotlin") { dependsOn("ktlintCheck") }

tasks.withType<Test> {
    useJUnitPlatform {
        excludeTags("integration-test")
    }
}

tasks.test {
    finalizedBy("jacocoTestReport")
}

tasks.withType<JacocoReport> {
    dependsOn("test")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(file("$buildDir/jacoco/coverage.xml"))
        csv.required.set(false)
        html.outputLocation.set(file("$buildDir/jacoco/html"))
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
    executionData.setFrom("$buildDir/jacoco/test.exec")
}

// task<Test>("integrationTest") {
//    useJUnitPlatform { includeTags("integration-test") }
//    mustRunAfter(tasks["test"])
//    group = "Verification"
//    description = "Runs the integration tests on Postgres Test Container."
// }

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    dependsOn("compileTestKotlin", "processTestResources")

    filter {
        // Включаем тесты из пакета integration
        includeTestsMatching("com.epam.brn.integration.*")
        // Включаем тесты с суффиксом IT
        includeTestsMatching("*IT")
        includeTestsMatching("*IntegrationTest")
    }

    useJUnitPlatform()

    shouldRunAfter(tasks.test)

    // Для TestContainers
    systemProperty("spring.profiles.active", "integration-tests")
    environment("TESTCONTAINERS_RYUK_DISABLED", "true")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
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
