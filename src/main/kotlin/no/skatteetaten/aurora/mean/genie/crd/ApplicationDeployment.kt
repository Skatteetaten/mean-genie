package no.skatteetaten.aurora.mean.genie.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.client.CustomResource
import no.skatteetaten.aurora.mean.genie.service.ApplicationDeploymentDeserializer

@JsonDeserialize(using=ApplicationDeploymentDeserializer::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ApplicationDeployment(val spec: ApplicationDeploymentSpec? ) : CustomResource()


