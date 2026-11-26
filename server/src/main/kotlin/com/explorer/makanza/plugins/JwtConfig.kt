package com.explorer.makanza.plugins

/**
 * JWT signing secret. It must come from the JWT_SECRET environment variable:
 * there is deliberately no built-in fallback, because a default secret in a
 * public repo would let anyone forge login tokens.
 */
object JwtConfig {
    const val MIN_SECRET_LENGTH = 32

    val secret: String by lazy { requireSecret(System.getenv("JWT_SECRET")) }

    fun requireSecret(value: String?): String {
        check(!value.isNullOrBlank()) {
            "JWT_SECRET is not set. Set it to a random string of at least $MIN_SECRET_LENGTH characters " +
                "(see .env.example) before starting the server."
        }
        check(!value.startsWith("change-me")) {
            "JWT_SECRET is still the placeholder from .env.example. Replace it with your own random string."
        }
        check(value.length >= MIN_SECRET_LENGTH) {
            "JWT_SECRET is too short (${value.length} characters). Use at least $MIN_SECRET_LENGTH characters."
        }
        return value
    }
}
