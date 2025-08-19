plugins {
    id("java-library")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    // processor doesn't depend on anything other than basic JDK APIs
}

tasks.withType<Jar> {
    manifest {
        attributes["Implementation-Title"] = "Luneth Processor"
    }
}