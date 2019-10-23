package no.skatteetaten.aurora.mean.genie.model
//
// import assertk.assertThat
// import assertk.assertions.isEqualTo
// import com.fasterxml.jackson.module.kotlin.readValue
// import io.fabric8.kubernetes.api.model.ObjectMeta
// import io.fabric8.kubernetes.client.utils.Serialization
// import no.skatteetaten.aurora.mean.genie.ApplicationConfig
// import org.junit.jupiter.api.Test
//
// class ApplicationDeploymentTest {
//     @Test
//     fun `serialize and deserialize ApplicationDeployment`() {
//         ApplicationConfig()
//
//         val applicationDeployment = ApplicationDeployment(ApplicationDeploymentSpec(), ObjectMeta())
//
//
//
//         val json = Serialization.jsonMapper().writeValueAsString(applicationDeployment)
//         println(json)
//         val result = Serialization.jsonMapper().readValue<ApplicationDeployment>(json)
//         assertThat(result.spec.applicationDeploymentId).isEqualTo("123")
//     }
// }
