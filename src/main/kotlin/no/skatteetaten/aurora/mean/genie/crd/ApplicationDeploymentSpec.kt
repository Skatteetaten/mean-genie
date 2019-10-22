package no.skatteetaten.aurora.mean.genie.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
class ApplicationDeploymentSpec : KubernetesResource {
    var applicationDeploymentId: String = ""
    var applicationDeploymentName: String = ""
    var databases: List<String> = emptyList()
    override fun toString(): String {
        return "ApplicationDeploymentSpec(applicationDeploymentId='$applicationDeploymentId', applicationDeploymentName='$applicationDeploymentName', databases=$databases)"
    }
}