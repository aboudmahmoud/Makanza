package com.explorer.makanza

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun getApiBaseUrl(): String
