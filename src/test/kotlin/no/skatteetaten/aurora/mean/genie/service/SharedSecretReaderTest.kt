package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SharedSecretReaderTest {

    @Test
    fun `Instance without secretLocation and secretValue throws exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            SharedSecretReader(null, null)
        }
    }

    @Test
    fun `Defined secretValue exposes value`() {
        val testValue = "testValue"
        val sharedSecretReader = SharedSecretReader(null, testValue)
        assertThat(sharedSecretReader.secret).isEqualTo(testValue)
    }

    @Test
    fun `SecretLocation gets value from file`() {
        val secretLocation = "src/test/resources/test-token.txt"
        val sharedSecretReader = SharedSecretReader(secretLocation, null)
        assertThat(sharedSecretReader.secret).isEqualTo("test-token")
    }

    @Test
    fun `Invalid secretLocation throws exception`() {
        val secretLocation = "wrong/url/nothing/here/token"
        assertThrows(IllegalStateException::class.java) {
            SharedSecretReader(secretLocation, null)
        }
    }
}
