plugins {
    id("java")
    id("idea")
    id("no.skatteetaten.gradle.aurora") version "4.5.4"
}

aurora {
    useKotlinDefaults
    useSpringBootDefaults
    useAsciiDoctor
    useSonar

    versions {
        javaSourceCompatibility = "17"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")

    implementation("com.fkorotkov:kubernetes-dsl:2.8.1")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("no.skatteetaten.aurora.springboot:aurora-spring-boot-webflux-starter:1.4.+")
    implementation("io.projectreactor.addons:reactor-extra:3.4.8")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.7")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.12.5")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("no.skatteetaten.aurora:mockmvc-extensions-kotlin:1.1.8")
    testImplementation("no.skatteetaten.aurora:mockwebserver-extensions-kotlin:1.3.1")
    testImplementation("io.projectreactor:reactor-test:3.4.22")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
}
