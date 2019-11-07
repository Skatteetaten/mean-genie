package no.skatteetaten.aurora.mean.genie

import no.skatteetaten.aurora.mean.genie.service.ApplicationDeploymentWatcherService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Main(
    val adWatcher: ApplicationDeploymentWatcherService
) : CommandLineRunner {

    override fun run(vararg args: String) {
        adWatcher.watch()
    }
}

fun main(args: Array<String>) {

    SpringApplication.run(Main::class.java, *args)
}
