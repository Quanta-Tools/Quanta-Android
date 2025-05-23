import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*

plugins {
    id("com.android.library")    version "8.10.0"
    id("org.jetbrains.kotlin.android") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    `maven-publish`
    signing
}

val releaseVersion: String? by project
version = releaseVersion ?: "0.0.0"
group   = "tools.quanta"

android {
    namespace = "tools.quanta.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { compose = true }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    api("androidx.lifecycle:lifecycle-process:2.7.0")
    api("androidx.compose.runtime:runtime:1.6.7")
    api("androidx.compose.ui:ui:1.6.7")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    api("androidx.fragment:fragment-ktx:1.6.1")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"]) // Ensure POM includes dependencies from the 'release' component

                // Include sources
                val sourcesJar by tasks.registering(Jar::class) {
                    archiveClassifier.set("sources")
                    from(android.sourceSets["main"].java.srcDirs)
                }

                // Coordinates
                groupId    = project.group.toString()
                artifactId = "sdk"
                version = project.version.toString()

                pom {
                    name.set("Quanta SDK")
                    description.set("Analytics & lifecycle SDK for Android apps")
                    url.set("https://github.com/Quanta-Tools/Quanta-Android")
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("io.github.yspreen")
                            name.set("Nick Spreen")
                            email.set("12631527+yspreen@users.noreply.github.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/Quanta-Tools/Quanta-Android.git")
                        developerConnection.set("scm:git:ssh://git@github.com/Quanta-Tools/Quanta-Android.git")
                        url.set("https://github.com/Quanta-Tools/Quanta-Android")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "ossrh"
                url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/?publishing_type=automatic")
                credentials(HttpHeaderCredentials::class) {
                    name = providers.gradleProperty("ossrhAuthHeaderName").get()
                    value = providers.gradleProperty("ossrhAuthHeaderValue").get()
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }

    tasks.named("generateMetadataFileForReleasePublication") {
        dependsOn(tasks.named("sourcesJar"))
    }

    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["release"])
    }
}

tasks.wrapper {
    gradleVersion = "8.14"
    distributionType = Wrapper.DistributionType.BIN
}
