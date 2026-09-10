plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.srm"
version = "0.0.1-SNAPSHOT"
description = "ms-backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "BUNDLE"
            includes = listOf("com.srm.msbackend.services.*")
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        csv.required = true
        html.required = true
    }
    doLast {
        val reportFile = layout.buildDirectory
            .file("reports/jacoco/test/jacocoTestReport.csv")
            .get()
            .asFile

        if (reportFile.exists()) {
            val rows = reportFile.readLines().drop(1)
                .filter { it.isNotBlank() }
                .map { it.split(",") }
            val instructionsMissed = rows.sumOf { it[3].toLong() }
            val instructionsCovered = rows.sumOf { it[4].toLong() }
            val branchesMissed = rows.sumOf { it[5].toLong() }
            val branchesCovered = rows.sumOf { it[6].toLong() }
            val instructionCoverage = (instructionsCovered * 100.0) /
                    (instructionsMissed + instructionsCovered)
            val branchCoverage = (branchesCovered * 100.0) /
                    (branchesMissed + branchesCovered)

            logger.lifecycle(
                "JaCoCo coverage: %.2f%% instructions, %.2f%% branches".format(
                    instructionCoverage,
                    branchCoverage
                )
            )
            logger.lifecycle("HTML report: ${layout.buildDirectory.file("reports/jacoco/test/html/index.html").get().asFile}")
        }
    }
}
