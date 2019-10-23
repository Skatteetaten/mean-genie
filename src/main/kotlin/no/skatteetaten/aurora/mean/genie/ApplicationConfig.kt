package no.skatteetaten.aurora.mean.genie

import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.TrustManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.util.StreamUtils
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.SslProvider
import reactor.netty.tcp.TcpClient

@Configuration
class ApplicationConfig : BeanPostProcessor {

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
    fun websocketClient(
        @Qualifier("openshift") tcpClient: TcpClient,
        @Value("\${integrations.openshift.url}") openshiftUrl: String,
        @Value("\${integrations.openshift.tokenLocation:file:/var/run/secrets/kubernetes.io/serviceaccount/token}") token: Resource
    ): ReactorNettyWebSocketClient {
        return ReactorNettyWebSocketClient(
            HttpClient.create()
                .baseUrl(openshiftUrl)
                .headers {
                    it.add(HttpHeaders.AUTHORIZATION, "Bearer ${token.readContent()}")
                    it.add("User-Agent", "Mean-Genie")
                }
        )
    }

    @Qualifier("openshift")
    @Bean
    fun tcpClient(
        @Value("\${integrations.openshift.readTimeout:5000}") readTimeout: Long,
        @Value("\${integrations.openshift.writeTimeout:5000}") writeTimeout: Long,
        @Value("\${integrations.openshift.connectTimeout:5000}") connectTimeout: Int,
        @Qualifier("openshift") trustStore: KeyStore?
    ): TcpClient {
        val trustFactory = TrustManagerFactory.getInstance("X509")
        trustFactory.init(trustStore)

        val sslProvider = SslProvider.builder().sslContext(
            SslContextBuilder
                .forClient()
                .trustManager(trustFactory)
                .build()
        ).build()
        return TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .secure(sslProvider)
            .doOnConnected { connection ->
                connection
                    .addHandlerLast(ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                    .addHandlerLast(WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS))
            }
    }

    @Bean
    @Qualifier("openshift")
    @Profile("local")
    fun localKeyStore(): KeyStore? = null

    @Profile("openshift")
    @Bean
    @Qualifier("openshift")
    fun openshiftSSLContext(@Value("\${trust.store}") trustStoreLocation: String): KeyStore? =
        KeyStore.getInstance(KeyStore.getDefaultType())?.let { ks ->
            ks.load(FileInputStream(trustStoreLocation), "changeit".toCharArray())
            val fis = FileInputStream("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt")
            CertificateFactory.getInstance("X509").generateCertificates(fis).forEach {
                ks.setCertificateEntry((it as X509Certificate).subjectX500Principal.name, it)
            }
            ks
        }
}

fun Resource.readContent() = StreamUtils.copyToString(this.inputStream, StandardCharsets.UTF_8)
