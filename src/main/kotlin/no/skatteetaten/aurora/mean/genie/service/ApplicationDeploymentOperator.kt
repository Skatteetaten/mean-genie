package no.skatteetaten.aurora.mean.genie.service

import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.Watcher
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation
import io.fabric8.kubernetes.client.dsl.Resource
import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentDoneable
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentList
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ApplicationDeploymentOperator(
    private val appCRDClient: NonNamespaceOperation<ApplicationDeployment, ApplicationDeploymentList, ApplicationDeploymentDoneable, Resource<ApplicationDeployment, ApplicationDeploymentDoneable>>? = null
) {

    fun init(): Boolean {

        logger.info("> Registering Application CRD Watch")
        appCRDClient?.watch(object : Watcher<ApplicationDeployment> {
            override fun eventReceived(action: Watcher.Action, application: ApplicationDeployment) {
                if (action == Watcher.Action.DELETED) {
                    logger.info(">> Deleting App: " + application.metadata.name)
                }
            }

            override fun onClose(cause: KubernetesClientException) {}
        })

        return true
    }
}