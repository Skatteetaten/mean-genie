package no.skatteetaten.aurora.mean.genie.service

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.Watcher
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation
import io.fabric8.kubernetes.client.dsl.Resource
import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.core.K8SCoreRuntime
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentDoneable
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentList
import org.springframework.stereotype.Service

@Service
class ApplicationDeploymentOperator(private val k8SCoreRuntime: K8SCoreRuntime) {

    private val logger = KotlinLogging.logger {}
    private var applicationDeploymentCRD: CustomResourceDefinition? = null
    private var applicationWatchRegistered = false
    private var appCRDClient: NonNamespaceOperation<ApplicationDeployment, ApplicationDeploymentList, ApplicationDeploymentDoneable, Resource<ApplicationDeployment, ApplicationDeploymentDoneable>>? = null



    fun areRequiredCRDsPresent(): Boolean {
        try {
            k8SCoreRuntime.registerCustomKind(
                "skatteetaten.no/v1",
                "ApplicationDeployment",
                ApplicationDeployment::class.java
            )
            val crds = k8SCoreRuntime.getCustomResourceDefinitionList()
            for (crd in crds.items) {
                val metadata = crd.metadata
                if (metadata != null) {
                    val name = metadata.name
                    if ("applicationdeployments.skatteetaten.no" == name) {
                        applicationDeploymentCRD = crd
                        appCRDClient = k8SCoreRuntime.customResourcesClient(
                            crd,
                            ApplicationDeployment::class.java,
                            ApplicationDeploymentList::class.java,
                            ApplicationDeploymentDoneable::class.java
                        )
                    }
                }
            }
            return if (applicationDeploymentCRD != null) {
                true
            } else {
                logger.error("> Custom CRDs required to work not found please check your installation!")
                logger.error(
                    "\t > App CRD: " + (applicationDeploymentCRD?.metadata?.name ?: "missing applicationDeploymentCRD")
                )
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("> Init sequence not done")
        }

        return false
    }

    fun registerApplicationWatch() {
        logger.info("> Registering Application CRD Watch")
        appCRDClient?.watch(object : Watcher<ApplicationDeployment> {
            override fun eventReceived(action: Watcher.Action, application: ApplicationDeployment) {
                if (action == Watcher.Action.DELETED) {
                    logger.info(">> Deleting App: " + application.metadata.name)
                }
            }

            override fun onClose(cause: KubernetesClientException) {}
        })
    }

    fun init(): Boolean {
        appCRDClient = k8SCoreRuntime.customResourcesClient(
                applicationDeploymentCRD!!,
                ApplicationDeployment::class.java,
                ApplicationDeploymentList::class.java,
                ApplicationDeploymentDoneable::class.java
            )

        if (!applicationWatchRegistered) {
            registerApplicationWatch()
        }

        return true
    }

}