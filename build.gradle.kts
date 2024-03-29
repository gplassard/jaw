plugins {
    id("java")
    id("application")
}

group = "fr.gplassard"
version = "1.0.0-SNAPSHOTT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass = "fr.gplassard.saw.Saw"
}

dependencies {
    implementation(libs.picocli)
    implementation(libs.commons.lang)
    implementation(libs.aws.cloudwatch)
    implementation(libs.aws.sts)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.logback)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.mockito)
    testImplementation(libs.assertj)
}

tasks.withType<Test> {
    useJUnitPlatform()
}