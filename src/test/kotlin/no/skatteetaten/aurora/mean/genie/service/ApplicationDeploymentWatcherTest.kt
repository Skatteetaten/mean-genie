package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.fabric8.kubernetes.api.model.ObjectMeta
import no.skatteetaten.aurora.mean.genie.ApplicationConfig
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeploymentSpec
import no.skatteetaten.aurora.mean.genie.model.WatchEvent
import org.junit.jupiter.api.Test

class ApplicationDeploymentWatcherTest {
    @Test
    fun `deserialize watch event`() {
        ApplicationConfig()

        val watchEvent = WatchEvent<ApplicationDeployment>(
            "DELETED",
            ApplicationDeployment(ApplicationDeploymentSpec("123", "abc"), ObjectMeta())
        )

        val json = ObjectMapper().writeValueAsString(watchEvent)
        println(json)
        val readValue = ObjectMapper().readValue<WatchEvent>(json)
        println(readValue)

        assertThat(readValue.type).isEqualTo("ADDED")
        assertThat(readValue.`object`).isInstanceOf(ApplicationDeployment::class)
    }
}
