package no.skatteetaten.aurora.mean.genie.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
class ApplicationDeploymentSpec : KubernetesResource {
    lateinit var applicationDeploymentId: String
    lateinit var applicationDeploymentName: String
    lateinit var databases: List<String>
    override fun toString(): String {
        return "ApplicationDeploymentSpec(applicationDeploymentId='$applicationDeploymentId', applicationDeploymentName='$applicationDeploymentName', databases=$databases)"
    }
}