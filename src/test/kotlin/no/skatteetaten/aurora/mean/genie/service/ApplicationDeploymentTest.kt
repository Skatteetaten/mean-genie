package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentSpec
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ApplicationDeploymentTest {
    @Test
    fun `serialize and deserialize ApplicationDeployment`() {
        val applicationDeployment = ApplicationDeployment(spec = ApplicationDeploymentSpec(applicationDeploymentId = "123", applicationDeploymentName = "test"))
        val json = jacksonObjectMapper().writeValueAsString(applicationDeployment)
        println(json)
        val result = jacksonObjectMapper().readValue<ApplicationDeployment>(json)
        assertThat (result.spec.applicationDeploymentId).isEqualTo("123")

    }
}