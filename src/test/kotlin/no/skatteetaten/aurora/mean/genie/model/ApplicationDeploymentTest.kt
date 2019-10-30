package no.skatteetaten.aurora.mean.genie.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.readValue
import io.fabric8.kubernetes.api.model.ObjectMeta
import no.skatteetaten.aurora.mean.genie.kubernetesObjectMapper
import org.junit.jupiter.api.Test

class ApplicationDeploymentTest {
    @Test
    fun `serialize and deserialize ApplicationDeployment`() {

        val applicationDeployment = ApplicationDeployment(ApplicationDeploymentSpec("123", "abc"), ObjectMeta())
        val json = kubernetesObjectMapper().writeValueAsString(applicationDeployment)
        val result = kubernetesObjectMapper().readValue<ApplicationDeployment>(json)
        assertThat(result.spec.applicationDeploymentId).isEqualTo("123")
    }
}