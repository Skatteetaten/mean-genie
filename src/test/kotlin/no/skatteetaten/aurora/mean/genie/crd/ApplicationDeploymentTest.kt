package no.skatteetaten.aurora.mean.genie.crd

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.readValue
import io.fabric8.kubernetes.client.utils.Serialization
import no.skatteetaten.aurora.mean.genie.ApplicationConfig
import org.junit.jupiter.api.Test

class ApplicationDeploymentTest {
    @Test
    fun `serialize and deserialize ApplicationDeployment`() {
        ApplicationConfig()
        val applicationDeployment = ApplicationDeployment(
            spec = ApplicationDeploymentSpec(
                applicationDeploymentId = "123",
                applicationDeploymentName = "test",
                databases = emptyList()
            )
        )
        val json = Serialization.jsonMapper().writeValueAsString(applicationDeployment)
        println(json)
        val result = Serialization.jsonMapper().readValue<ApplicationDeployment>(json)
        assertThat(result.spec?.applicationDeploymentId).isEqualTo("123")
    }
}