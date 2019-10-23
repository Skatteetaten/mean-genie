package no.skatteetaten.aurora.mean.genie

import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.service.KubernetesWatcher
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class Main(val watcher: KubernetesWatcher) : CommandLineRunner {

    @Throws(Exception::class)
    override fun run(vararg args: String) {

        val url = "/apis/skatteetaten.no/v1/applicationdeployments?watch=true&labelSelector=affiliation=aurora"
        watcher.watch<ApplicationDeployment>(url) {
            if (it.type == "DELETED") {
                logger.info("{}", it)
            }
        }
    }
}
fun main(args: Array<String>) {

    SpringApplication.run(Main::class.java, *args)
}