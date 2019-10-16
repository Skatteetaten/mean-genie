package no.skatteetaten.aurora.mean.genie.core

import io.fabric8.kubernetes.api.model.Doneable
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import io.fabric8.openshift.client.OpenShiftClient
import org.springframework.stereotype.Service

@Service
class K8SCoreRuntime(private val client: OpenShiftClient) {

    fun registerCustomKind(apiVersion: String, kind: String, clazz: Class<out KubernetesResource>) {
        KubernetesDeserializer.registerCustomKind(apiVersion, kind, clazz)
    }

    fun getCustomResourceDefinitionList(): CustomResourceDefinitionList {
        return client.customResourceDefinitions().list()
    }

    fun <T : HasMetadata, L : KubernetesResourceList<*>, D : Doneable<T>> customResourcesClient(
        crd: CustomResourceDefinition,
        resourceType: Class<T>,
        listClass: Class<L>,
        doneClass: Class<D>
    ): MixedOperation<T, L, D, Resource<T, D>> {
        return client.customResources(crd, resourceType, listClass, doneClass)
    }
}