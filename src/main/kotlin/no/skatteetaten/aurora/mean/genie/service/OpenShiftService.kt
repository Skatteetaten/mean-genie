package no.skatteetaten.aurora.mean.genie.service

import io.fabric8.openshift.client.DefaultOpenShiftClient
import io.fabric8.openshift.client.OpenShiftClient
import no.skatteetaten.aurora.mean.genie.extensions.applicationDeploymentsTemporary
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OpenShiftService(
    val client: OpenShiftClient
) {

    fun findTemporaryApplicationDeployments(now: Instant = Instant.now()): List<ApplicationDeploymentResource> =
        (client as DefaultOpenShiftClient).applicationDeploymentsTemporary()
            .map { it.toResource(now) }
}