package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.fasterxml.jackson.module.kotlin.readValue
import io.fabric8.kubernetes.api.model.ObjectMeta
import no.skatteetaten.aurora.mean.genie.kubernetesObjectMapper
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeploymentSpec
import no.skatteetaten.aurora.mean.genie.model.WatchEvent
import org.junit.jupiter.api.Test

class ApplicationDeploymentWatcherTest {
    @Test
    fun `deserialize watch event`() {
        val watchEvent = WatchEvent(
            "DELETED",
            ApplicationDeployment(ApplicationDeploymentSpec("123", "abc"), ObjectMeta())
        )

        val json = kubernetesObjectMapper().writeValueAsString(watchEvent)
        println(json)
        val recievedEvent = kubernetesObjectMapper().readValue<WatchEvent<ApplicationDeployment>>(json)
        println(recievedEvent)

        assertThat(recievedEvent.type).isEqualTo("DELETED")
        assertThat(recievedEvent.resource).isInstanceOf(ApplicationDeployment::class)
    }
}
