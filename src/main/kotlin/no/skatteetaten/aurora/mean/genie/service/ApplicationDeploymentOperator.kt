package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.fabric8.kubernetes.client.CustomResource
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
        appCRDClient?.watch(ApplicationDeploymentWatcher())

        return true
    }
}

class ApplicationDeploymentWatcher : Watcher<ApplicationDeployment> {
    override fun eventReceived(action: Watcher.Action?, resource: ApplicationDeployment?) {
        if (action == Watcher.Action.DELETED) {
            logger.info(">> Deleting App: " + resource?.metadata?.name)
            // logger.info(application.toString())
            // logger.info(application.metadata.labels.getOrDefault("affiliation","Did not find affiliation"))
            // logger.info(application.spec)
        }
    }

    override fun onClose(cause: KubernetesClientException?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}