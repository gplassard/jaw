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
    implementation("info.picocli:picocli:4.7.5")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("software.amazon.awssdk:cloudwatchlogs:2.23.16")
    implementation("software.amazon.awssdk:sts:2.23.16")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
    testImplementation("org.assertj:assertj-core:3.25.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}