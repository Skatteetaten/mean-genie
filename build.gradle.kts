plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.50"
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.50"
    id("org.jlleitschuh.gradle.ktlint") version "9.0.0"
    id("org.sonarqube") version "2.8"
    id("org.springframework.boot") version "2.2.0.RELEASE"
    id("org.asciidoctor.convert") version "2.3.0"

    id("com.gorylenko.gradle-git-properties") version "2.2.0"
    id("com.github.ben-manes.versions") version "0.27.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.12"

    id("no.skatteetaten.gradle.aurora") version "3.2.0"
}

dependencies {
    implementation("com.fkorotkov:kubernetes-dsl:3.0")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.19")
    testImplementation("com.nhaarman:mockito-kotlin:1.6.0")
    testImplementation("no.skatteetaten.aurora:mockmvc-extensions-kotlin:1.0.2")
}
