plugins {
    id("java-library")
}

group = "rs.jamie"
version = "1.1.0"

allprojects {
    apply {
        plugin("java-library")
    }

    group = "rs.jamie"
    version = "1.1.0"

    java.sourceCompatibility = JavaVersion.VERSION_16
    java.targetCompatibility = JavaVersion.VERSION_16

    repositories {
        mavenCentral()
    }

}