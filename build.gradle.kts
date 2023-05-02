import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
        mavenCentral()
    }

    dependencies {
        classpath(Libraries.sonarqube_gradle_plugin)
        classpath(Libraries.kotlin_gradle_plugin)
//        classpath(Libraries.ktlint_gradle_plugin)
    }
}

plugins {
    id(BuildPlugins.spring_dependency_management) version Versions.dependency_management
    id(BuildPlugins.spring_boot) version Versions.spring_boot
    id(BuildPlugins.sonarqube) version Versions.sonarqube
//    id(BuildPlugins.ktlint) version Versions.ktlint
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.allopen") version Versions.kotlin
    kotlin("plugin.noarg") version Versions.kotlin
    kotlin("kapt") version Versions.kotlin
    kotlin("plugin.spring") version Versions.kotlin apply false
    kotlin("plugin.jpa") version Versions.kotlin apply false
    idea
    jacoco
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

sonarqube {
    properties {
        property("sonar.host.url", project.findProperty("sonarHostUrl") ?: "")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.exclusions", "**/*Test*.*,**/Q*.java,**/*Repository.kt,**/*Interceptor.kt,**entity/*,**/*Entity.*,**/*Constants.*,**/*Advice.kt")
        property("sonar.cpd.exclusions", "**/*Config.kt,**/*Configuration.kt,**/*Advice.kt")
        property("sonar.tests", "src/integration-test/kotlin,src/test/kotlin")
        property("sonar.test.inclusions", "**/*Test.kt,**/*TestConfig.kt")
        property("sonar.coverage.exclusions", "**/*Test*.*,**/Q*.java,**/*Repository.kt,**/*Interceptor.kt,**/*Advice.kt,**entity/*,**/*Entity.*,**/*Constants.*")
//        property("sonar.qualitygate.wait", "false")
    }
}

allprojects {
    repositories {
        maven("https://plugins.gradle.org/m2/")
        maven("https://jitpack.io")
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("idea")
        plugin("java-library")
        plugin("kotlin")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.jetbrains.kotlin.plugin.allopen")
        plugin("org.jetbrains.kotlin.plugin.noarg")
        plugin("org.jetbrains.kotlin.plugin.jpa")
        plugin("org.jetbrains.kotlin.kapt")
        plugin("jacoco")
//        plugin("java-test-fixtures")
    }

//    apply(plugin = BuildPlugins.ktlint)

    sourceSets {
        create("integration-test") {
            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output

            resources.srcDir(file("src/integration-test/resources"))
        }
    }

    val integrationTestImplementation by configurations.getting {
        extendsFrom(configurations.implementation.get(), configurations.testImplementation.get())
    }

    configurations {
        "integrationTestRuntimeOnly" {
            extendsFrom(configurations.testRuntimeOnly.get())
        }
    }

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
            mavenBom(Libraries.spring_boot_dependencyManagement)
            mavenBom(Libraries.spring_cloud_dependencyManagement)
        }
    }

    tasks.jar {
        enabled = true
    }

    tasks.bootJar {
        enabled = false
    }

    jacoco {
        toolVersion = "0.8.7"
    }

    sonarqube {
        properties {
            property("sonar.coverage.jacoco.xmlReportPaths", "${project.buildDir}/reports/jacoco/test/jacocoTestReport.xml")
//            property("sonar.coverage.jacoco.xmlReportPaths", "${project.rootDir}/build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml")
        }
    }

    tasks.test {
        extensions.configure(JacocoTaskExtension::class) {
            setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
        }

        reports {
            html.required.set(true)
            junitXml.required.set(true)
        }
    }

    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
        useJUnitPlatform()
        testLogging {
            events = setOf(FAILED, PASSED, SKIPPED)
        }
    }

    val integrationTest = task<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = sourceSets["integration-test"].output.classesDirs
        classpath = sourceSets["integration-test"].runtimeClasspath

        reports {
            html.isEnabled = true
            junitXml.isEnabled = true
        }

        extensions.configure(JacocoTaskExtension::class) {
            setDestinationFile(file("$buildDir/jacoco/jacoco-integration.exec"))
        }

        shouldRunAfter("test")
    }

    tasks.jacocoTestReport {
        executionData(fileTree(buildDir).include("/jacoco/*.exec"))

        // /reports/jacoco/test/jacocoTestReport.xml
        reports {
            xml.required.set(true)
            xml.outputLocation.set(file("$buildDir/reports/jacoco/test/jacocoTestReport.xml"))
            html.required.set(true)
            html.outputLocation.set(file("$buildDir/reports/jacoco/test/html"))
        }

        mustRunAfter("test", "integrationTest")
    }

    tasks.check {
        dependsOn("integrationTest")
        dependsOn(tasks.jacocoTestReport)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xinline-classes",
                "-progressive"
            )
            jvmTarget = "11"
        }
    }

    tasks.withType<org.gradle.jvm.tasks.Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    allOpen {
        annotation("javax.persistence.Entity")
        annotation("javax.persistence.MappedSuperclass")
        annotation("javax.persistence.Embeddable")
    }

    noArg {
        annotation("javax.persistence.Entity")
        annotation("javax.persistence.MappedSuperclass")
        annotation("javax.persistence.Embeddable")
    }

    dependencies {
        implementation(Libraries.spring_boot_starter)
        implementation(Libraries.kotlin_reflect)
        implementation(Libraries.kotlin_stdlib_jdk8)
        implementation(Libraries.kassava)
        implementation(Libraries.slf4j_api)
        implementation(Libraries.kotlin_logging)
        implementation(Libraries.logback_classic)

//        testImplementation(Libraries.h2_database)
//        testImplementation(Libraries.kotest_faker)
        testImplementation(Libraries.kotest_runner_junit5)
        testImplementation(Libraries.kotest_assertions_core)
        testImplementation(Libraries.kotest_assertions_json)
        testImplementation(Libraries.kotest_property)
//        testImplementation(Libraries.kotest_extensions_spring)
        testImplementation(Libraries.kotest_ext_spring)
        testImplementation(Libraries.mockk)
        testImplementation(Libraries.ninja_squad_springmockk)
        testImplementation(Libraries.spring_boot_starter_test)

        kapt("org.springframework.boot:spring-boot-configuration-processor")
    }
}

dependencies {
}

tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}

task<JacocoReport>("jacocoRootReport") {
    dependsOn(subprojects.map { it.tasks.withType<JacocoReport>() })
    sourceDirectories.setFrom(subprojects.map { it.tasks.findByName("jacocoTestReport")!!.property("sourceDirectories") })
    classDirectories.setFrom(subprojects.map { it.tasks.findByName("jacocoTestReport")!!.property("classDirectories") })
    executionData.setFrom(
        project.fileTree(".") {
            include("**/build/jacoco/jacoco.exec")
        }
    )
    onlyIf {
        true
    }
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
