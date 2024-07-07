import net.researchgate.release.ReleaseExtension

plugins {
    id("java")
    id("application")
    alias(libs.plugins.jreleaser.plugin)
    alias(libs.plugins.shadow.plugin)
    alias(libs.plugins.release.plugin)
}

group = "fr.gplassard"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass = "fr.gplassard.jaw.Jaw"
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

tasks.register<Copy>("copyGradleProperties") {
    from("gradle.properties")
    into("src/main/resources")
}

tasks.named("build").configure {
    dependsOn("copyGradleProperties")
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
            artifact {
                path = file("build/distributions/jaw-${version}.zip")
            }
        }
    }
}

configure<ReleaseExtension> {
    tagTemplate.set("v\${version}")
    preTagCommitMessage.set("release(jaw) - pre tag commit: ")
    tagCommitMessage.set("release(jaw) - creating tag: ")
    newVersionCommitMessage.set("release(jaw) - new version commit: ")
}
