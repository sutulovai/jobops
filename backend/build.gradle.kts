import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("nu.studer.jooq") version "9.0"
}

group = "com.sutulovai.jobops"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val jjwtVersion = "0.12.6"
val pdfboxVersion = "3.0.3"
val testcontainersVersion = "1.20.4"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    jooqGenerator("org.postgresql:postgresql")

    // jOOQ codegen
    jooqGenerator("org.jooq:jooq-meta-extensions:3.19.15")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // PDF text extraction
    implementation("org.apache.pdfbox:pdfbox:$pdfboxVersion")

    // OpenAPI docs
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.assertj:assertj-core")
}

jooq {
    version = "3.19.15"
    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = Logging.WARN
                generator.apply {
                    name = "org.jooq.codegen.JavaGenerator"
                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        properties.apply {
                            add(
                                org.jooq.meta.jaxb.Property().apply {
                                    key = "scripts"
                                    value = "src/main/resources/db/migration/*.sql"
                                }
                            )
                            add(
                                org.jooq.meta.jaxb.Property().apply {
                                    key = "sort"
                                    value = "flyway"
                                }
                            )
                            add(
                                org.jooq.meta.jaxb.Property().apply {
                                    key = "defaultNameCase"
                                    value = "lower"
                                }
                            )
                        }
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = false
                        isImmutablePojos = false
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.sutulovai.jobops.jooq"
                        directory = "build/generated-src/jooq/main"
                    }
                }
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Repo-root .env is not read by the JVM; export vars into the bootRun process for local dev.
tasks.named<BootRun>("bootRun") {
    val envFile = layout.projectDirectory.file("../.env").asFile
    if (envFile.isFile) {
        envFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .forEach { line ->
                val eq = line.indexOf('=')
                if (eq <= 0) return@forEach
                val key = line.substring(0, eq).trim()
                if (key.isEmpty()) return@forEach
                var value = line.substring(eq + 1).trim()
                if (value.length >= 2) {
                    val q = value[0]
                    if ((q == '"' || q == '\'') && value.last() == q) {
                        value = value.substring(1, value.length - 1)
                    }
                }
                environment(key, value)
            }
    }
}

// jOOQ code generation disabled — project uses plain DSL (table/field strings)
tasks.named("generateJooq") {
    enabled = false
}

sourceSets {
    main {
        java {
            srcDir("build/generated-src/jooq/main")
        }
    }
    create("functionalTest") {
        java.srcDir("src/functionalTest/java")
        resources.srcDir("src/functionalTest/resources")
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
}

val functionalTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

tasks.register<Test>("functionalTest") {
    description = "Runs functional tests."
    group = "verification"
    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("functionalTest")
}
