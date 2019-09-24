package no.skatteetaten.aurora.mean.genie.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.openshift.client.DefaultOpenShiftClient
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeploymentList
import okhttp3.Request

fun DefaultOpenShiftClient.deleteApplicationDeployment(namespace: String, name: String): Boolean {
    val url =
        this.openshiftUrl.toURI().resolve("/apis/skatteetaten.no/v1/namespaces/$namespace/applicationdeployments/$name")
    return try {
        val request = Request.Builder().url(url.toString()).delete().build()
        this.httpClient.newCall(request).execute().use {
            it.isSuccessful
        }
    } catch (e: Exception) {
        throw KubernetesClientException(
            "Error occurred while deleting temporary application deployment namespace=$namespace name=$name",
            e
        )
    }
}

fun DefaultOpenShiftClient.applicationDeploymentsTemporary(): List<ApplicationDeployment> {
    val url =
        this.openshiftUrl.toURI().resolve("/apis/skatteetaten.no/v1/applicationdeployments")

    return try {
        val request = Request.Builder().url(url.toString()).build()
        val response = this.httpClient.newCall(request).execute()

        jacksonObjectMapper().readValue(response.body()?.bytes(), ApplicationDeploymentList::class.java)
            ?.items
            ?: throw KubernetesClientException("Error occurred while fetching temporary application deployments")
    } catch (e: Exception) {
        throw KubernetesClientException("Error occurred while fetching temporary application deployments", e)
    }
}
