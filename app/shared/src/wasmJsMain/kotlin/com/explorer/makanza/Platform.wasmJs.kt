package com.explorer.makanza

class WasmPlatform: Platform {
    override val name: String = "WebAssembly"
}

actual fun getPlatform(): Platform = WasmPlatform()
actual fun getApiBaseUrl(): String = "http://localhost:8080"
