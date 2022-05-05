package com.srcmaxim.repository

import com.srcmaxim.domain.entity.ShortUrl
import io.vertx.mutiny.core.eventbus.EventBus
import java.net.URI
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped

const val initialDatabaseCapacity = 1000

@ApplicationScoped
class ShortenerRepository(
    bus: EventBus
) {

    private val kvDatabase = ConcurrentHashMap<String, String>(initialDatabaseCapacity)
    private val cleaner = Cleaner(kvDatabase) {
        bus.publish("invalidateShortToOriginUrl", it)
    }

    fun saveIfNotExists(shortUrl: ShortUrl, ttl: Int?): Boolean {
        val key = shortUrl.shortUrlPath.toString()
        val value = shortUrl.originalUrl.toString()
        val firstTime = kvDatabase.putIfAbsent(key, value) == null
        if (firstTime) {
            ttl?.let { cleaner.addExpire(it, key) }
        }
        return firstTime
    }

    fun findById(shortUrlPath: String): ShortUrl? {
        return kvDatabase[shortUrlPath]?.let { value ->
            ShortUrl(URI(shortUrlPath), URL(value))
        }
    }
}
