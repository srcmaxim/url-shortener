package com.srcmaxim.domain.entity

import java.net.URI
import java.net.URL

data class ShortUrl(
    val shortUrlPath: URI,
    val originalUrl: URL,
)
