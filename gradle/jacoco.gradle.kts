import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

apply(plugin = "jacoco")

extensions.configure<JacocoPluginExtension> {
    toolVersion = "0.8.11"
    reportsDirectory.set(layout.buildDirectory.dir("jacoco"))
}

tasks.named<Test>("test").configure {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.named<JacocoReport>("jacocoTestReport").configure {
    dependsOn(tasks.named<Test>("test"))

    classDirectories.setFrom(
        files(classDirectories.files.map { dir ->
            fileTree(dir) {
                exclude(
                    "**/request/**",
                    "**/response/**",
                    "**/presentation/**",
                    "**/repository/**",
                    "**/exception/**",
                    "**/aop/**",
                    "**/common/**",
                    "**/demo/**",
                    "**/enums/**",
                    "**/DCSApplication.class"
                )
            }
        })
    )

    executionData.setFrom(
        fileTree(layout.buildDirectory.dir("jacoco")) {
            include("test.exec")
        }
    )

    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }

    finalizedBy("jacocoTestCoverageVerification")
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification").configure {
    dependsOn(tasks.named("jacocoTestReport"))

    classDirectories.setFrom(
        files(classDirectories.files.map { dir ->
            fileTree(dir) {
                exclude(
                    "**/request/**",
                    "**/response/**",
                    "**/presentation/**",
                    "**/repository/**",
                    "**/exception/**",
                    "**/aop/**",
                    "**/config/**",
                    "**/DCSApplication.class"
                )
            }
        })
    )

    violationRules {
        rule {
            element = "CLASS"
            limit {
                counter = "METHOD"
                value = "COVEREDRATIO"
                minimum = BigDecimal("0.00")
            }
        }
    }
}
