package no.skatteetaten.aurora.mean.genie.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.client.CustomResource

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
class ApplicationDeployment : CustomResource() {

    var spec: ApplicationDeploymentSpec? = null

    override fun toString(): String {
        return "Application{" +
            "spec=" + spec +
            '}'
    }
}
