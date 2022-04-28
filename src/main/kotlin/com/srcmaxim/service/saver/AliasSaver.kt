package com.srcmaxim.service.saver

import com.srcmaxim.domain.entity.ShortUrl
import com.srcmaxim.repository.ShortenerRepository
import java.net.URI
import java.net.URL
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class AliasSaver(
    private val shortenerRepository: ShortenerRepository
) {

    fun save(originalUrl: URL, customAlias: URI, ttl: Int?): ShortUrl {
        val shortUrl = ShortUrl(customAlias, originalUrl)
        val saved = shortenerRepository.saveIfNotExists(shortUrl, ttl)
        return when (saved) {
            false -> throw SaveException
            true -> shortUrl
        }
    }
}
