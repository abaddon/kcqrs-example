
group = "io.github.abaddon.kcqrs"

object Meta {
    const val desc = "KCQRS Examples"
    const val license = "Apache-2.0"
    const val githubRepo = "abaddon/kcqrs-example"
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}

object Versions {
    const val kcqrsCoreVersion = "0.0.3"
    const val kcqrsEventStoreDBVersion = "0.0.3"
    const val kcqrsTestVersion = "0.0.3"
    const val kustomCompareVersion = "0.0.1"
    const val slf4jVersion = "1.7.25"
    const val kotlinVersion = "1.6.0"
    const val kotlinCoroutineVersion = "1.6.0"
    const val jacksonModuleKotlinVersion = "2.13.0"
    const val junitJupiterVersion = "5.7.0"
    const val jacocoToolVersion = "0.8.7"
    const val jvmTarget = "11"
    const val eventStoreDBVersion = "1.0.0"

}

plugins {
    kotlin("jvm") version "1.6.0"
    id("com.palantir.git-version") version "0.13.0"
    jacoco
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()

val lastTag=details.lastTag.substring(1)
val snapshotTag= {
    val list=lastTag.split(".")
    val third=(list.last().toInt() + 1).toString()
    "${list[0]}.${list[1]}.$third-SNAPSHOT"
}
version = if(details.isCleanTag) lastTag else snapshotTag()

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }

}

dependencies {
    //KCQRS Modules
    implementation("io.github.abaddon.kcqrs:kcqrs-core:${Versions.kcqrsCoreVersion}")
    implementation("io.github.abaddon.kcqrs:kcqrs-EventStoreDB:${Versions.kcqrsEventStoreDBVersion}")
    implementation("io.github.abaddon:kustomCompare:${Versions.kustomCompareVersion}")

    //EventStoreDB
    implementation("com.eventstore:db-client-java:${Versions.eventStoreDBVersion}")

    implementation("org.slf4j:slf4j-api:${Versions.slf4jVersion}")
    implementation("org.slf4j:slf4j-simple:${Versions.slf4jVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutineVersion}")
    implementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiterVersion}") // JVM dependency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutineVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jacksonModuleKotlinVersion}")
    implementation(kotlin("test"))

    testImplementation(kotlin("test"))
    testImplementation("io.github.abaddon.kcqrs:kcqrs-test:${Versions.kcqrsTestVersion}")
}

jacoco {
    toolVersion = Versions.jacocoToolVersion
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = Versions.jvmTarget
}


