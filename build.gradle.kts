import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    signing
    `java-library`
    `maven-publish`
    id("org.cadixdev.licenser") version "0.6.1"
    id("dev.adamko.dokkatoo-html") version "2.4.0"
    id("org.jetbrains.kotlin.jvm") version "2.2.10"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

val KOTLIN_VER = "2.2.10"

group = "me.dkim19375"
version = "2.10.11"

license {
    header.set(rootProject.resources.text.fromFile("LICENSE"))
    include("**/*.kt")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://m2.dv8tion.net/releases")
    maven("https://repo.triumphteam.dev/snapshots/")
    maven("https://s01.oss.sonatype.org/content/repositories/releases/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.openjdk.nashorn:nashorn-core:15.6")

    api("commons-io:commons-io:2.20.0")
    api("net.dv8tion:JDA:5.6.1")
    api("org.apache.commons:commons-lang3:3.18.0")
    api("com.github.minndevelopment:jda-ktx:0.12.0")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KOTLIN_VER")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
    api("io.github.dkim19375:dkimcore:1.6.1")
    // api("org.codehaus.groovy:groovy-jsr223:3.0.8")

    // testImplementation "net.dv8tion:JDA:$JDA_VER"
    testImplementation("net.dv8tion:JDA:5.6.1")
    testImplementation("commons-io:commons-io:2.20.0")
    testImplementation("org.apache.commons:commons-lang3:3.18.0")
    testImplementation("com.github.minndevelopment:jda-ktx:0.12.0")

    // testing libs
    testImplementation("org.jetbrains.kotlin:kotlin-test:$KOTLIN_VER")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.0.0")
}

val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets.main.get().allSource.srcDirs)
    archiveClassifier.set("sources")
}

val dokkaHtmlJar by tasks.registering(Jar::class) {
    from(tasks.dokkatooGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
/*        maven {
            def releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            def snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = releasesRepoUrl

            credentials {
                it.username mavenUsername
                it.password mavenPassword
            }
        }*/
    }

    val project = project
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "io.github.dkim19375"
            artifactId = "dkim19375jdautils"
            version = project.version as String

            from(components["kotlin"])
            artifact(sourcesJar)
            artifact(dokkaHtmlJar)

            pom {
                name.set("dkim19375JDAUtils")
                description.set("A kotlin library used to help JDA developers make bots either!")
                url.set("https://github.com/dkim19375/dkim19375JDAUtils")

                packaging = "jar"

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("dkim19375")
                        timezone.set("America/New_York")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/dkim19375/dkim19375JDAUtils.git")
                    developerConnection.set("scm:git:ssh://github.com:dkim19375/dkim19375JDAUtils.git")
                    url.set("https://github.com/dkim19375/dkim19375JDAUtils")
                }
            }
        }
    }
}

nexusPublishing {
    packageGroup.set("io.github.dkim19375")
    this@nexusPublishing.repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(project.findProperty("sonatypeUsername") as? String ?: return@sonatype)
            password.set(project.findProperty("sonatypePassword") as? String ?: return@sonatype)
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks {
    withType<Jar> {
        dependsOn(licenseFormat)
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}
