package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.controller.security.SharedSecretReader
import no.skatteetaten.aurora.mean.genie.kubernetesObjectMapper
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.model.WatchEvent
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux

private val logger = KotlinLogging.logger {}

@Service
class DatabaseService(/*watcher: KubernetesWatcher,*/ val webClient: WebClient, val sharedSecretReader: SharedSecretReader) {
    // init {
    //     val url = "/apis/skatteetaten.no/v1/applicationdeployments?watch=true&labelSelector=affiliation=aurora"
    //     watcher.watch(url, listOf("DELETED")) {
    //         val databases = readDatabaseLabel(it)
    //         deleteSchemaByID(databases).then()
    //     }
    // }

    fun deleteSchemaByID(databases: List<String>): Flux<JsonNode> {
        return databases.toFlux().flatMap { databaseId ->
            webClient
                .delete()
                .uri("/api/v1/schema/$databaseId")
                .header(HttpHeaders.AUTHORIZATION, "aurora-token ${sharedSecretReader.secret}")
                .retrieve()
                .bodyToMono<JsonNode>()
                .log()
                .doOnError {
                    println(it)
                }
                .doOnSuccess {
                    println(it)
                }
        }
    }

    fun readDatabaseLabel(it: JsonNode): List<String> {
        val watchEvent: WatchEvent<ApplicationDeployment> = kubernetesObjectMapper().convertValue(it)
        return watchEvent.resource.spec.databases
    }
}
