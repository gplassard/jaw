plugins {
    id("java")
    id("application")
    alias(libs.plugins.jreleaser.plugin)
    alias(libs.plugins.shadow.plugin)
    alias(libs.plugins.release.plugin)
}

group = "fr.gplassard"
version = "0.0.1-SNAPSHOT"

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

jreleaser {
    project {
        authors = listOf("gplassard")
        description = "Tail AWS CloudWatch logs"
        license = "Apache-2.0"
        copyright = "gplassard"
        links {
            homepage = "https://github.com/gplassard/jaw"
        }
    }
    distributions {
        distributions.create("jaw") {
            artifact {
                path = file("build/libs/jaw-${version}-all.jar")
            }
        }
    }
}
