package no.skatteetaten.aurora.mean.genie.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.client.CustomResource

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
class ApplicationDeployment : CustomResource() {
    lateinit var spec: ApplicationDeploymentSpec
    override fun toString(): String {
        return "ApplicationDeployment(spec=$spec)"
    }
}


