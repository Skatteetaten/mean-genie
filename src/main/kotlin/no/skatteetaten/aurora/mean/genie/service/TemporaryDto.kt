package no.skatteetaten.aurora.mean.genie.service

import java.time.Duration
import java.time.Instant

interface BaseResource {
    val ttl: Duration
    val name: String
    val removalTime: Instant
}

data class ApplicationDeploymentResource(
    override val name: String,
    val namespace: String,
    override val ttl: Duration,
    override val removalTime: Instant
) : BaseResource