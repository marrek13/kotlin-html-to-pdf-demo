import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

group = "pl.marrek13"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.apache.commons.pool)
    implementation(libs.playwright)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "21"
        }
    }

    register<JavaExec>("playwrightInstallChromium") {
        classpath(sourceSets["test"].runtimeClasspath)
        mainClass.set("com.microsoft.playwright.CLI")
        environment["PLAYWRIGHT_BROWSERS_PATH"] = "$projectDir/build/pw-browsers"
        args = listOf("install", "chromium")
    }

    assemble {
        dependsOn("playwrightInstallChromium")
    }
}
