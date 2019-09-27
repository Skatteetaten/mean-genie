package no.skatteetaten.aurora.mean.genie.service

interface BaseResource {
    val name: String
}

data class ApplicationDeploymentResource(
    override val name: String,
    val namespace: String,
    val affiliation: String
) : BaseResource