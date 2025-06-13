import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.0.0-beta4"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "rs.jamie"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.h2database:h2:2.3.232")
    implementation("io.lettuce:lettuce-core:6.6.0.RELEASE")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
    compileOnly("org.jetbrains:annotations:24.0.1")
}

publishing {

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    publications {
        create<MavenPublication>("luneth") {
            from(components["java"])
            groupId = "rs.jamie"
            artifactId = "luneth"
            version = "${project.version}"
            artifact(sourcesJar.get())
        }
    }
}

tasks {
    val shadowJar = named<ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.getByName("shadow"))
    }

    build {
        dependsOn(shadowJar)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}