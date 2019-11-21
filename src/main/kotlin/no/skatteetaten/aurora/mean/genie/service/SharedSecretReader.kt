package no.skatteetaten.aurora.mean.genie.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException

private val logger = KotlinLogging.logger {}

/**
 * Component for reading the shared secret used for authentication. You may specify the shared secret directly using
 * the aurora.token.value property, or specify a file containing the secret with the aurora.token.location property.
 * TODO: Trenger vi at denne er slik? Kan vi ikke alltid bare støtte location og gjøre som vi gjør i openshiftToken?
 * TOOD: Kunne vi lagd en Bean med en Qualifier som gir oss token verdien? Så kan vi hente den fra fil i main og mocke bean i test?
 */
@Component
class SharedSecretReader(
    @Value("\${aurora.token.location:}") private val secretLocation: String?,
    @Value("\${aurora.token.value:}") private val secretValue: String?
) {

    val secret = initSecret()

    private fun initSecret() =
        if (secretLocation.isNullOrEmpty() && secretValue.isNullOrEmpty()) {
            throw IllegalArgumentException("Either aurora.token.location or aurora.token.value must be specified")
        } else {
            if (secretValue.isNullOrEmpty()) {
                val secretFile = File(secretLocation).absoluteFile
                try {
                    logger.info("Reading token from file {}", secretFile.absolutePath)
                    secretFile.readText()
                } catch (e: IOException) {
                    throw IllegalStateException("Unable to read shared secret from specified location [${secretFile.absolutePath}]")
                }
            } else {
                secretValue
            }
        }
}
