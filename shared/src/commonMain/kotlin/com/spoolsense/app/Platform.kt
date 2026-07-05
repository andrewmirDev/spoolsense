package com.spoolsense.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform