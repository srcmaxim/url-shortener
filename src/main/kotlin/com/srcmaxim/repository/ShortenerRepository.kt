package com.srcmaxim.repository

import com.datastax.oss.driver.api.core.type.reflect.GenericType
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession
import com.srcmaxim.domain.entity.ShortUrl
import io.vertx.mutiny.core.eventbus.EventBus
import java.net.URI
import java.net.URL
import javax.enterprise.context.ApplicationScoped

const val insertStatement = "INSERT INTO ks1.short_url (short_url_path, original_url) values (?, ?) IF NOT EXISTS"
const val insertStatementTtl = "INSERT INTO ks1.short_url (short_url_path, original_url) values (?, ?) IF NOT EXISTS USING TTL ?"
const val selectStatement = "SELECT original_url, ttl(original_url) FROM ks1.short_url WHERE short_url_path = ?"

@ApplicationScoped
class ShortenerRepository(
    bus: EventBus,
    private var session: QuarkusCqlSession,
) {

    private val cleaner = Cleaner {
        bus.publish("invalidateShortToOriginUrl", it)
    }

    fun saveIfNotExists(shortUrl: ShortUrl, ttl: Int?): Boolean {
        val shortUrlPath = shortUrl.shortUrlPath.toString()
        val originalUrl = shortUrl.originalUrl.toString()
        val resultSet = if (ttl != null) {
            session.execute(insertStatementTtl, shortUrlPath, originalUrl, ttl)
        } else {
            session.execute(insertStatement, shortUrlPath, originalUrl)
        }
        val wasApplied = resultSet.wasApplied()
        if (wasApplied) {
            ttl?.let { cleaner.addExpire(ttl, shortUrlPath) }
        }
        return wasApplied
    }

    fun findById(shortUrlPath: String): ShortUrl? {
        val resultSet = session.execute(selectStatement, shortUrlPath)
        val originalUrl = resultSet.one()?.get(0, GenericType.STRING)
        return originalUrl?.let {
            val ttl = resultSet.one()?.get(1, GenericType.INTEGER)
            ttl?.let { cleaner.addExpire(ttl, shortUrlPath) }
            ShortUrl(URI(shortUrlPath), URL(originalUrl))
        }
    }
}
