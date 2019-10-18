package no.skatteetaten.aurora.mean.genie.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource


@JsonIgnoreProperties(ignoreUnknown = true)
data class ApplicationDeploymentSpec(val applicationDeploymentId: String, val applicationDeploymentName: String) : KubernetesResource{

    //private val applicationDeploymentId: String? = null
    //private val applicationDeploymentName: String? = null

    override fun toString(): String {
        return "ApplicationSpec{" +
            "version='" + applicationDeploymentId + '\''.toString() +
            ", selector='" + applicationDeploymentName + '\''.toString()
            '}'.toString()
    }
}