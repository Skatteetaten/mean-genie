package no.skatteetaten.aurora.mean.genie.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
data class ApplicationDeploymentSpec(val applicationDeploymentId: String, val applicationDeploymentName: String, val databases: List<String>) : KubernetesResource