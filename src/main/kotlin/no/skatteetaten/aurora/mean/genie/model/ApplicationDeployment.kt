package no.skatteetaten.aurora.mean.genie.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.fabric8.kubernetes.api.model.ObjectMeta
import no.skatteetaten.aurora.mean.genie.extensions.AFFILIATION_LABEL
import no.skatteetaten.aurora.mean.genie.service.ApplicationDeploymentResource

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApplicationDeploymentList(
    val items: List<ApplicationDeployment> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApplicationDeployment(
    val kind: String = "ApplicationDeployment",
    val metadata: ObjectMeta,
    val apiVersion: String = "skatteetaten.no/v1"
) {

    fun toResource(): ApplicationDeploymentResource {
        val affiliation = this.affiliation()

        return ApplicationDeploymentResource(
            name = this.metadata.name,
            namespace = this.metadata.namespace,
            affiliation = affiliation
        )
    }

    fun affiliation(): String {
        return this.metadata.labels[AFFILIATION_LABEL]
            ?: throw IllegalStateException("removeAfter is not set or valid timstamp")
    }
}