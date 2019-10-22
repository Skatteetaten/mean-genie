package no.skatteetaten.aurora.mean.genie

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.KubernetesResourceMappingProvider
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.client.utils.Serialization
import io.fabric8.openshift.client.DefaultOpenShiftClient
import io.fabric8.openshift.client.OpenShiftClient
import no.skatteetaten.aurora.mean.genie.core.K8SCoreRuntime
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentDoneable
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentList
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentSpec
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint

@JsonDeserialize
interface DefaultJsonDeserializer

class MeanGenieMapping : KubernetesResourceMappingProvider {

    private val mappings = mutableMapOf(
        "ApplicationDeployment" to ApplicationDeployment::class.java
    )

    override fun getMappings() = mappings
}

@Configuration
class ApplicationConfig : BeanPostProcessor {

    init {
        Serialization.jsonMapper().findAndRegisterModules()
        Serialization.jsonMapper().addMixIn(
            ApplicationDeploymentSpec::class.java,
            DefaultJsonDeserializer::class.java
        )
    }

    @Bean
    fun client(): OpenShiftClient {
        return DefaultOpenShiftClient()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun basic(): BasicAuthenticationEntryPoint {
        return BasicAuthenticationEntryPoint().also {
            it.realmName = "MEAN-GENIE"
        }
    }

    @Bean
    fun appCrdClient(k8SCoreRuntime: K8SCoreRuntime): MixedOperation<ApplicationDeployment, ApplicationDeploymentList, ApplicationDeploymentDoneable, Resource<ApplicationDeployment, ApplicationDeploymentDoneable>> {
        k8SCoreRuntime.registerCustomKind(
            "skatteetaten.no/v1",
            "ApplicationDeployment",
            ApplicationDeployment::class.java
        )

        return k8SCoreRuntime.getCustomResourceDefinitionList().items.find {
            "applicationdeployments.skatteetaten.no" == it.metadata.name
        }?.let {
            k8SCoreRuntime.customResourcesClient(
                it,
                ApplicationDeployment::class.java,
                ApplicationDeploymentList::class.java,
                ApplicationDeploymentDoneable::class.java
            )
        }
            ?: throw RuntimeException("Could not find ad crd")
    }
}
