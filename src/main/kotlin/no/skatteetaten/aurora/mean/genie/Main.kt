package no.skatteetaten.aurora.mean.genie

import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.service.ApplicationDeploymentOperator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Main : CommandLineRunner {
    private val logger = KotlinLogging.logger {}
    @Autowired
    private val appsOperator: ApplicationDeploymentOperator? = null

    private var initDone = false
    private var crdsFound = false

    fun main(args: Array<String>) {

        SpringApplication.run(Main::class.java, *args)
    }

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        crdsFound = appsOperator!!.areRequiredCRDsPresent()
        if (crdsFound) {
            initDone = appsOperator.init()
            logger.info("> App Service Init.")
        }
    }
}