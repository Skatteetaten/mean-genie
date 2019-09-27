package no.skatteetaten.aurora.mean.genie.service

import io.fabric8.openshift.client.DefaultOpenShiftClient
import io.fabric8.openshift.client.OpenShiftClient
import no.skatteetaten.aurora.mean.genie.extensions.applicationDeploymentsTemporary
import org.springframework.stereotype.Service

@Service
class OpenShiftService(
    val client: OpenShiftClient
) {

    fun findApplicationDeployments(): List<ApplicationDeploymentResource> =
        (client as DefaultOpenShiftClient).applicationDeploymentsTemporary().map { it.toResource() }
}