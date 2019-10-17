package no.skatteetaten.aurora.mean.genie

import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.service.ApplicationDeploymentOperator
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Main(val appsOperator: ApplicationDeploymentOperator) : CommandLineRunner {
    private val logger = KotlinLogging.logger {}

    private var initDone = false

    @Throws(Exception::class)
    override fun run(vararg args: String) {

        initDone = appsOperator.init()
            logger.info("> App Service Init.")
    }
}
fun main(args: Array<String>) {

    SpringApplication.run(Main::class.java, *args)
}
