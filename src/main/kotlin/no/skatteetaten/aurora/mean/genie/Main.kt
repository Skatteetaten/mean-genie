package no.skatteetaten.aurora.mean.genie

import no.skatteetaten.aurora.mean.genie.service.DatabaseService
import no.skatteetaten.aurora.mean.genie.service.KubernetesWatcher
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Main(val watcher: KubernetesWatcher, val databaseService: DatabaseService) : CommandLineRunner {

    @Throws(Exception::class)
    override fun run(vararg args: String) {

        val url = "/apis/skatteetaten.no/v1/applicationdeployments?watch=true&labelSelector=affiliation=aurora"
        watcher.watch(url, listOf("DELETED")) {
            val databases = databaseService.readDatabaseLabel(it)
            databaseService.deleteSchemaByID(databases).then()
        }
    }
}

fun main(args: Array<String>) {

    SpringApplication.run(Main::class.java, *args)
}