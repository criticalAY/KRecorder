import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    `maven-publish`
    signing
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KRecorderCore"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "io.github.criticalay.krecorder.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// ── Maven Central Publishing ──

group = property("GROUP").toString()
version = property("VERSION_NAME").toString()

publishing {
    publications.withType<MavenPublication> {
        artifactId = "krecorder-core"

        pom {
            name.set(property("POM_NAME").toString())
            description.set(property("POM_DESCRIPTION").toString())
            url.set(property("POM_URL").toString())

            licenses {
                license {
                    name.set(property("POM_LICENCE_NAME").toString())
                    url.set(property("POM_LICENCE_URL").toString())
                }
            }
            developers {
                developer {
                    id.set(property("POM_DEVELOPER_ID").toString())
                    name.set(property("POM_DEVELOPER_NAME").toString())
                    email.set(property("POM_DEVELOPER_EMAIL").toString())
                }
            }
            scm {
                url.set(property("POM_SCM_URL").toString())
                connection.set(property("POM_SCM_CONNECTION").toString())
                developerConnection.set(property("POM_SCM_DEV_CONNECTION").toString())
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl

            credentials {
                username = findProperty("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME")
                password = findProperty("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKeyId = findProperty("signing.keyId")?.toString() ?: System.getenv("SIGNING_KEY_ID")
    val signingKey = findProperty("signing.key")?.toString() ?: System.getenv("SIGNING_KEY")
    val signingPassword = findProperty("signing.password")?.toString() ?: System.getenv("SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications)
}
