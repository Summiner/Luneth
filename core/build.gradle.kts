import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

dependencies {
    // Redis
    implementation("io.lettuce:lettuce-core:6.6.0.RELEASE")

    // Caffeine
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")

    // Mongo
    implementation(platform("org.mongodb:mongodb-driver-bom:5.6.0"))
    implementation(platform("io.projectreactor:reactor-bom:2025.0.0-M7"))
    implementation("org.mongodb:mongodb-driver-reactivestreams")
    implementation("io.projectreactor:reactor-core")

    // ClassGraph
    implementation("io.github.classgraph:classgraph:4.8.162")

    // Annotations
    compileOnly("org.jetbrains:annotations:24.0.1")
    api(project(":annotations"))
    annotationProcessor(project(":annotations"))
    testAnnotationProcessor(project(":annotations"))

    // Junit Tests
    testImplementation(platform("org.junit:junit-bom:5.13.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
            version = project.version.toString()
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

    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }
}