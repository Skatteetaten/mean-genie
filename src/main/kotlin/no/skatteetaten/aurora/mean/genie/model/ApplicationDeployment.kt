package no.skatteetaten.aurora.mean.genie.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.fabric8.kubernetes.api.model.ObjectMeta
import no.skatteetaten.aurora.mean.genie.extensions.REMOVE_AFTER_LABEL
import no.skatteetaten.aurora.mean.genie.service.ApplicationDeploymentResource
import java.time.Duration
import java.time.Instant

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

    fun toResource(now: Instant): ApplicationDeploymentResource {
        val removalTime = this.removalTime()

        return ApplicationDeploymentResource(
            name = this.metadata.name,
            namespace = this.metadata.namespace,
            ttl = Duration.between(now, removalTime),
            removalTime = removalTime
        )
    }

    fun removalTime(): Instant {
        return this.metadata.labels[REMOVE_AFTER_LABEL]?.let {
            Instant.ofEpochSecond(it.toLong())
        } ?: throw IllegalStateException("removeAfter is not set or valid timstamp")
    }
}