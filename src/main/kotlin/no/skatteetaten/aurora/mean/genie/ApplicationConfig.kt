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
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.net.ssl.TrustManagerFactory
import no.skatteetaten.aurora.filter.logging.AuroraHeaderFilter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.util.StreamUtils
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.SslProvider
import reactor.netty.tcp.TcpClient

const val HEADER_KLIENTID = "KlientID"

@Configuration
@EnableAsync
class ApplicationConfig(
    @Value("\${spring.application.name}") val applicationName: String
) : BeanPostProcessor {

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
                    it.add("User-Agent", applicationName)
                }
        )
    }

    @Bean
    fun webClientDbh(
        @Qualifier("dbh") tcpClient: TcpClient,
        @Value("\${integrations.dbh.url}") dbhUrl: String
    ): WebClient =
        WebClient
            .builder()
            .baseUrl(dbhUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HEADER_KLIENTID, applicationName)
            .defaultHeader(AuroraHeaderFilter.KORRELASJONS_ID, UUID.randomUUID().toString())
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.from(tcpClient)
                        .compress(true)
                )
            ).build()

    @Bean
    @Qualifier("openshift")
    fun openshiftTcpClientWrapper(
        @Value("\${integrations.openshift.readTimeout:5000}") readTimeout: Long,
        @Value("\${integrations.openshift.writeTimeout:5000}") writeTimeout: Long,
        @Value("\${integrations.openshift.connectTimeout:5000}") connectTimeout: Int,
        trustStore: KeyStore?
    ): TcpClient = tcpClient(readTimeout, writeTimeout, connectTimeout, trustStore)

    @Bean
    @Qualifier("dbh")
    fun dbhTcpClientWrapper(
        @Value("\${integrations.dbh.readTimeout:5000}") readTimeout: Long,
        @Value("\${integrations.dbh.writeTimeout:5000}") writeTimeout: Long,
        @Value("\${integrations.dbh.connectTimeout:5000}") connectTimeout: Int,
        trustStore: KeyStore?
    ): TcpClient = tcpClient(readTimeout, writeTimeout, connectTimeout, trustStore)

    fun tcpClient(
        readTimeout: Long,
        writeTimeout: Long,
        connectTimeout: Int,
        trustStore: KeyStore?
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
    @Profile("local")
    fun localKeyStore(): KeyStore? = null

    @Profile("openshift")
    @Bean
    fun openshiftSSLContext(@Value("\${trust.store}") trustStoreLocation: String): KeyStore =
        KeyStore.getInstance(KeyStore.getDefaultType())?.let { ks ->
            ks.load(FileInputStream(trustStoreLocation), "changeit".toCharArray())
            val fis = FileInputStream("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt")
            CertificateFactory.getInstance("X509").generateCertificates(fis).forEach {
                ks.setCertificateEntry((it as X509Certificate).subjectX500Principal.name, it)
            }
            ks
        } ?: throw Exception("KeyStore getInstance did not return KeyStore")
}

fun Resource.readContent() = StreamUtils.copyToString(this.inputStream, StandardCharsets.UTF_8)
