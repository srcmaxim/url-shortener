package com.srcmaxim.service.saver

import com.srcmaxim.domain.entity.ShortUrl
import com.srcmaxim.repository.ShortenerRepository
import java.net.URL
import javax.enterprise.context.ApplicationScoped

const val maxTimes = 5

@ApplicationScoped
class Base64Saver(
    private val shortUrlPathService: ShortUrlPathService,
    private val shortenerRepository: ShortenerRepository
) {

    fun save(originalUrl: URL, ttl: Int?): ShortUrl {
        val shortUrlPath = shortUrlPathService.getShortUrlPath()
        val shortUrl = ShortUrl(shortUrlPath, originalUrl)
        return saveShortUrlPath(0, shortUrl, ttl)
    }

    private fun saveShortUrlPath(times: Int, shortUrl: ShortUrl, ttl: Int?): ShortUrl {
        if (times >= maxTimes) {
            throw SaveException
        }
        val saved = shortenerRepository.saveIfNotExists(shortUrl, ttl)
        return when (saved) {
            false -> {
                val newShortUrl = ShortUrl(
                    shortUrlPathService.getShortUrlPath(),
                    shortUrl.originalUrl
                )
                saveShortUrlPath(times + 1, newShortUrl, ttl)
            }
            true -> shortUrl
        }
    }
}
