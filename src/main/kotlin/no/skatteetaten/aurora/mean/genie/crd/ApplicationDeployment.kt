package no.skatteetaten.aurora.mean.genie.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.fabric8.kubernetes.client.CustomResource

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApplicationDeployment(val spec: ApplicationDeploymentSpec?) : CustomResource()


