package no.skatteetaten.aurora.mean.genie.service
//
// import assertk.assertThat
// import assertk.assertions.isEqualTo
// import assertk.assertions.isInstanceOf
// import com.fasterxml.jackson.module.kotlin.readValue
// import io.fabric8.kubernetes.api.model.WatchEvent
// import io.fabric8.kubernetes.client.utils.Serialization
// import no.skatteetaten.aurora.mean.genie.ApplicationConfig
// import no.skatteetaten.aurora.mean.genie.model.ApplicationDeployment
// import no.skatteetaten.aurora.mean.genie.model.ApplicationDeploymentSpec
// import org.junit.jupiter.api.Test
//
// class ApplicationDeploymentWatcherTest {
//     @Test
//     fun `deserialize watch event`() {
//         ApplicationConfig()
//         val applicationDeploymentSpec = ApplicationDeploymentSpec().apply {
//             applicationDeploymentId = "123"
//             applicationDeploymentName = "abc"
//             databases = listOf("DB1")
//         }
//         val applicationDeployment = ApplicationDeployment().apply {
//             spec = applicationDeploymentSpec
//         }
//
//         val watchEvent = WatchEvent(applicationDeployment, "ADDED")
//         val json = Serialization.asJson(watchEvent)
//         val readValue = Serialization.jsonMapper().readValue<WatchEvent>(json)
//
//         assertThat(readValue.type).isEqualTo("ADDED")
//         assertThat(readValue.`object`).isInstanceOf(ApplicationDeployment::class)
//     }
// }
