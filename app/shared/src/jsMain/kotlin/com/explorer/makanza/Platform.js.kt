package com.explorer.makanza

class JSPlatform: Platform {
    override val name: String = "JavaScript"
}

actual fun getPlatform(): Platform = JSPlatform()
actual fun getApiBaseUrl(): String = "http://localhost:8080"
