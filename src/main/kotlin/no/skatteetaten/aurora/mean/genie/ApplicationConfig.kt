package no.skatteetaten.aurora.mean.genie

import io.fabric8.openshift.client.DefaultOpenShiftClient
import io.fabric8.openshift.client.OpenShiftClient
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint

@Configuration
class ApplicationConfig : BeanPostProcessor {

    // TODO hvorfor returnerer vi OpenShiftClient og ikke DefaultOpenShiftClient som return value
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
}
