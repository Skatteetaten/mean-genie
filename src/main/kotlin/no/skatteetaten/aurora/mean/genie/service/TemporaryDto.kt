package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.annotation.JsonInclude

interface BaseResource {
    val name: String
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApplicationDeploymentResource(
    override val name: String,
    val namespace: String,
    val affiliation: String,
    val meanGenie: String?
) : BaseResource