package com.explorer.makanza

import com.explorer.makanza.plugins.JwtConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JwtConfigTest {
    @Test
    fun missingSecretIsRejected() {
        assertFailsWith<IllegalStateException> { JwtConfig.requireSecret(null) }
        assertFailsWith<IllegalStateException> { JwtConfig.requireSecret("   ") }
    }

    @Test
    fun shortSecretIsRejected() {
        assertFailsWith<IllegalStateException> { JwtConfig.requireSecret("too-short") }
    }

    @Test
    fun placeholderSecretIsRejected() {
        assertFailsWith<IllegalStateException> { JwtConfig.requireSecret("change-me-to-a-long-random-string") }
    }

    @Test
    fun strongSecretIsAccepted() {
        val secret = "a".repeat(JwtConfig.MIN_SECRET_LENGTH)
        assertEquals(secret, JwtConfig.requireSecret(secret))
    }
}
