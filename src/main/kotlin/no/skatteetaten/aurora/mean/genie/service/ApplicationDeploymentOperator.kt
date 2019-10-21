package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.Watcher
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation
import io.fabric8.kubernetes.client.dsl.Resource
import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentDoneable
import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeploymentList
import org.springframework.stereotype.Service
import java.io.IOException

private val logger = KotlinLogging.logger {}

@Service
class ApplicationDeploymentOperator(
    private val appCRDClient: NonNamespaceOperation<ApplicationDeployment, ApplicationDeploymentList, ApplicationDeploymentDoneable, Resource<ApplicationDeployment, ApplicationDeploymentDoneable>>? = null
) {

    fun init(): Boolean {
        logger.info("> Registering Application CRD Watch")
        appCRDClient?.watch(ApplicationDeploymentWatcher())

        return true
    }
}

class ApplicationDeploymentWatcher : Watcher<ApplicationDeployment> {
    override fun eventReceived(action: Watcher.Action?, resource: ApplicationDeployment?) {
        logger.info { resource }
        if (action == Watcher.Action.DELETED) {
            logger.info(">> Deleting App: " + resource?.metadata?.name)
            // logger.info(application.toString())
            // logger.info(application.metadata.labels.getOrDefault("affiliation","Did not find affiliation"))
            logger.info { resource?.spec }
        }
    }

    override fun onClose(cause: KubernetesClientException?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class ApplicationDeploymentDeserializer : JsonDeserializer<ApplicationDeployment>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): ApplicationDeployment {
        return objectMapper.readValue(jsonParser, ApplicationDeployment::class.java)
    }

    @JsonDeserialize
    private interface DefaultJsonDeserializer// Reset default json deserializer

    companion object {

        private val objectMapper = ObjectMapper()

        init {
            objectMapper.registerModule(SimpleModule())
                .addMixIn(ApplicationDeployment::class.java, DefaultJsonDeserializer::class.java)
                .registerKotlinModule()
        }
    }
}
