package no.skatteetaten.aurora.mean.genie.service

import io.fabric8.openshift.client.DefaultOpenShiftClient
import io.fabric8.openshift.client.OpenShiftClient
import no.skatteetaten.aurora.mean.genie.extensions.applicationDeployments
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OpenShiftService(
    val client: OpenShiftClient
) {

    fun findApplicationDeployments(now: Instant = Instant.now()): List<ApplicationDeploymentResource> =
        (client as DefaultOpenShiftClient).applicationDeployments()
            .map { it.toResource(now) }
}