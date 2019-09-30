package no.skatteetaten.aurora.mean.genie.service

import java.time.Duration
import java.time.Instant

interface BaseResource {
    val name: String
}

data class ApplicationDeploymentResource(
    override val name: String,
    val namespace: String,
    val ttl: Duration,
    val removalTime: Instant
    // val affiliation: String
) : BaseResource