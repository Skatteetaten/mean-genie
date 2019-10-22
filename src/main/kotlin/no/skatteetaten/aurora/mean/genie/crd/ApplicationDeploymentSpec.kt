package no.skatteetaten.aurora.mean.genie.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.fabric8.kubernetes.api.model.KubernetesResource

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApplicationDeploymentSpec(
    val applicationDeploymentId: String,
    val applicationDeploymentName: String,
    val databases: List<String>
) : KubernetesResource