plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

// Compiles the shared receipt parser directly so the tests exercise the exact
// file that ships in commonMain. The parser is pure Kotlin with no Compose or
// platform dependencies, so it builds standalone on the JVM.
sourceSets {
    main {
        kotlin.srcDir("../composeApp/src/commonMain/kotlin/data/ocr")
        kotlin.exclude("**/CameraPreview.kt", "**/ReceiptOcr.kt", "**/ReceiptImagePicker.kt")
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
