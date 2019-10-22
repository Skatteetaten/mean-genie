package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.fasterxml.jackson.module.kotlin.readValue
import io.fabric8.kubernetes.api.model.WatchEvent
import io.fabric8.kubernetes.client.utils.Serialization
import no.skatteetaten.aurora.mean.genie.ApplicationConfig
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentSpec
import org.junit.jupiter.api.Test

class ApplicationDeploymentWatcherTest {
    @Test
    fun `deserialize watch event`() {
        ApplicationConfig()
        val watchEvent =
            WatchEvent(ApplicationDeployment(ApplicationDeploymentSpec("123", "abc", emptyList())), "ADDED")
        val json = Serialization.asJson(watchEvent)

        val readValue = Serialization.jsonMapper().readValue<WatchEvent>(json)
        assertThat(readValue.type).isEqualTo("ADDED")
        assertThat(readValue.`object`).isInstanceOf(ApplicationDeployment::class)
    }
}