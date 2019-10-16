package no.skatteetaten.aurora.mean.genie.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
class ApplicationDeploymentSpec : KubernetesResource {

    private var applicationDeploymentId: String? = "UNKNOWN"
    private var applicationDeploymentName: String? = "UNKNOWN"

    override fun toString(): String {
        return "ApplicationSpec{" +
            "applicationDeploymentName='" + applicationDeploymentName + '\'' +
            ", applicationDeploymentId='" + applicationDeploymentId + '\'' +
            '}'
    }
}