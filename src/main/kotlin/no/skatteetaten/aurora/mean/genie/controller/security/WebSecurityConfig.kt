package no.skatteetaten.aurora.mean.genie.controller.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.http.HttpServletRequest

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
        @Value("\${management.server.port}") val managementPort: Int,
        val authEntryPoint: BasicAuthenticationEntryPoint

) : WebSecurityConfigurerAdapter() {

    private fun forPort(port: Int) = RequestMatcher { request: HttpServletRequest -> port == request.localPort }

    override fun configure(http: HttpSecurity) {

        http.csrf().disable().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // We don't need sessions to be created.

        http.authorizeRequests()
                .requestMatchers(forPort(managementPort)).permitAll()
                .antMatchers("/docs/index.html").permitAll()
                .antMatchers("/").permitAll()
                .and().httpBasic().realmName("MEAN-GENIE").authenticationEntryPoint(authEntryPoint)
    }
}
