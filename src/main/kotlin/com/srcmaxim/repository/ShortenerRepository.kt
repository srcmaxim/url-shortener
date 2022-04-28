package com.srcmaxim.repository

import com.srcmaxim.domain.entity.ShortUrl
import java.net.URI
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped

const val initialDatabaseCapacity = 100

@ApplicationScoped
class ShortenerRepository {

    private val kvDatabase = ConcurrentHashMap<String, String>(initialDatabaseCapacity)
    private val cleaner = Cleaner(kvDatabase)

    fun saveIfNotExists(shortUrl: ShortUrl, ttl: Int?): Boolean {
        val key = shortUrl.shortUrlPath.toString()
        val value = shortUrl.originalUrl.toString()
        val firstTime = kvDatabase.putIfAbsent(key, value) == null
        if (firstTime && ttl != null) {
            val expireAt = ttl + System.currentTimeMillis()
            cleaner.addExpire(expireAt, key)
        }
        return firstTime
    }

    fun findById(shortUrlPath: String): ShortUrl? {
        return kvDatabase[shortUrlPath]?.let { value ->
            ShortUrl(URI(shortUrlPath), URL(value))
        }
    }
}