import com.diffplug.spotless.LineEnding

plugins {
  java
  `java-library`
  `maven-publish`
  signing
  alias(libs.plugins.spotless)
  alias(libs.plugins.nexus)
}

defaultTasks("build")

val signRequired = !rootProject.property("dev").toString().toBoolean()

group = "tr.com.infumia"

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  compileJava { options.encoding = Charsets.UTF_8.name() }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
    (options as StandardJavadocDocletOptions).tags("todo")
  }

  val javadocJar by
      creating(Jar::class) {
        dependsOn("javadoc")
        archiveClassifier.set("javadoc")
        from(javadoc)
      }

  val sourcesJar by
      creating(Jar::class) {
        dependsOn("classes")
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
      }

  build {
    dependsOn(spotlessApply)
    dependsOn(jar)
    dependsOn(sourcesJar)
    dependsOn(javadocJar)
  }
}

repositories { mavenCentral() }

dependencies {
  compileOnly(libs.lombok)
  compileOnly(libs.annotations)

  annotationProcessor(libs.lombok)

  testAnnotationProcessor(libs.lombok)
}

publishing {
  publications {
    val publication =
        create<MavenPublication>("mavenJava") {
          groupId = project.group.toString()
          artifactId = "terminable"
          version = project.version.toString()

          from(components["java"])
          artifact(tasks["sourcesJar"])
          artifact(tasks["javadocJar"])
          pom {
            name.set("Terminable")
            description.set("Fully Customizable Tab plugin for Velocity servers.")
            url.set("https://infumia.com.tr/")
            licenses {
              license {
                name.set("MIT License")
                url.set("https://mit-license.org/license.txt")
              }
            }
            developers {
              developer {
                id.set("portlek")
                name.set("Hasan Demirtaş")
                email.set("utsukushihito@outlook.com")
              }
            }
            scm {
              connection.set("scm:git:git://github.com/infumia/terminable.git")
              developerConnection.set("scm:git:ssh://github.com/infumia/terminable.git")
              url.set("https://github.com/infumia/terminable")
            }
          }
        }

    signing {
      isRequired = signRequired
      if (isRequired) {
        useGpgCmd()
        sign(publication)
      }
    }
  }
}

nexusPublishing { repositories { sonatype() } }

spotless {
  lineEndings = LineEnding.UNIX

  val prettierConfig =
    mapOf(
      "prettier" to "2.8.8",
      "prettier-plugin-java" to "2.2.0",
    )

  format("encoding") {
    target("*.*")
    targetExclude("modifier/agent/src/main/resources/realm-format-modifier-core.txt")
    encoding("UTF-8")
    endWithNewline()
    trimTrailingWhitespace()
  }

  yaml {
    target(
      "**/src/main/resources/*.yaml",
      "**/src/main/resources/*.yml",
      ".github/**/*.yml",
      ".github/**/*.yaml",
    )
    endWithNewline()
    trimTrailingWhitespace()
    val jackson = jackson()
    jackson.yamlFeature("LITERAL_BLOCK_STYLE", true)
    jackson.yamlFeature("MINIMIZE_QUOTES", true)
    jackson.yamlFeature("SPLIT_LINES", false)
  }

  kotlinGradle {
    target("**/*.gradle.kts")
    indentWithSpaces(2)
    endWithNewline()
    trimTrailingWhitespace()
    ktlint()
  }

  java {
    target("**/src/**/java/**/*.java")
    importOrder()
    removeUnusedImports()
    indentWithSpaces(2)
    endWithNewline()
    trimTrailingWhitespace()
    prettier(prettierConfig)
      .config(
        mapOf("parser" to "java", "tabWidth" to 2, "useTabs" to false, "printWidth" to 100),
      )
  }
}
