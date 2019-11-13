package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test
import org.junit.jupiter.api.Assertions

class SharedSecretReaderTest {

    @Test
    fun `Verify that instance without secretLocation and secretValue throws exception`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            SharedSecretReader(null, null)
        }
    }

    @Test
    fun `Verify that instance with defiend secretValue exposes value`() {
        val testValue = "testValue"
        val sharedSecretReader = SharedSecretReader(null, testValue)
        assertThat(sharedSecretReader.secret).isEqualTo(testValue)
    }

    @Test
    fun `Verify that instance with defined secretLocation gets value from file`() {
        val secretLocation = "src/test/resources/test-token.txt"
        val sharedSecretReader = SharedSecretReader(secretLocation, null)
        assertThat(sharedSecretReader.secret).isEqualTo("test-token")
    }

    @Test
    fun `Verify that instance with invalid secretLocation throws exception`() {
        val secretLocation = "wrong/url/nothing/here/token"
        Assertions.assertThrows(IllegalStateException::class.java) {
            SharedSecretReader(secretLocation, null)
        }
    }
}