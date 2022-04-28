package com.srcmaxim.service

import com.srcmaxim.domain.entity.ShortUrl
import com.srcmaxim.repository.ShortenerRepository
import com.srcmaxim.service.saver.AliasSaver
import com.srcmaxim.service.saver.Base64Saver
import io.quarkus.cache.Cache
import io.quarkus.cache.CacheName
import io.smallrye.mutiny.coroutines.awaitSuspending
import java.net.URI
import java.net.URL
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class ShortenerService @Inject constructor(
    private val aliasSaver: AliasSaver,
    private val base64Saver: Base64Saver,
    private val shortenerRepository: ShortenerRepository,
    @CacheName("shortToOriginUrl") private val cache: Cache
) {

    fun createShortUrlPath(originalUrl: URL, customAlias: URI?, ttl: Int?): ShortUrl {
        return when (customAlias) {
            null -> base64Saver.save(originalUrl, ttl)
            else -> aliasSaver.save(originalUrl, customAlias, ttl)
        }
    }

    suspend fun getRedirectUrl(shortUrlPath: String): ShortUrl? = cache.get(shortUrlPath) {
        shortenerRepository.findById(it)
    }.awaitSuspending()
}
