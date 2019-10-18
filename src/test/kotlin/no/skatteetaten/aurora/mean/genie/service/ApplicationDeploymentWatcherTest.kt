package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.fabric8.kubernetes.api.model.WatchEvent
import no.skatteetaten.aurora.mean.genie.ApplicationDeploymanetEventWatcher
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentSpec
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ApplicationDeploymentWatcherTest {
    @Test
    fun `deserialize watch event`() {
        val watchEvent = WatchEvent(ApplicationDeployment(ApplicationDeploymentSpec("123", "abc")),"ADDED")
        val json = jacksonObjectMapper().writeValueAsString(watchEvent)
        println(json)
        val readValue = jacksonObjectMapper().disable(MapperFeature.USE_ANNOTATIONS).readValue<ApplicationDeploymanetEventWatcher>(json)
        assertThat(readValue.type).isEqualTo("ADDED")
        assertThat(readValue.`object`).isInstanceOf(ApplicationDeployment::class)
    }
}