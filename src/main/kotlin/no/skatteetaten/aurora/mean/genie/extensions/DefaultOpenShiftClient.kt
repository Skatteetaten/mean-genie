package no.skatteetaten.aurora.mean.genie.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.openshift.client.DefaultOpenShiftClient
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeploymentList
import okhttp3.Request

// TODO finn en bedre måte på å finne et knipe applicationdeployments
fun DefaultOpenShiftClient.applicationDeploymentsTemporary(): List<ApplicationDeployment> {
    val url =
        this.openshiftUrl.toURI().resolve("/apis/skatteetaten.no/v1/applicationdeployments?labelSelector=removeAfter")

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