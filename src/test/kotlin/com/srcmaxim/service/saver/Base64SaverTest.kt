package com.srcmaxim.service.saver

import com.srcmaxim.domain.entity.ShortUrl
import com.srcmaxim.repository.ShortenerRepository
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import java.net.URI
import java.net.URL
import javax.inject.Inject

@QuarkusTest
class Base64SaverTest {

    @Inject
    private lateinit var base64Saver: Base64Saver

    @InjectMock
    private lateinit var shortenerRepository: ShortenerRepository

    @InjectMock
    private lateinit var shortUrlPathService: ShortUrlPathService

    @Test
    fun `when no Short URL Path duplications then return Short URL`() {
        val shortUrlPath = URI("path")
        val originalUrl = URL("https://example.com")
        var shortUrl = ShortUrl(shortUrlPath, originalUrl)
        `when`(shortUrlPathService.getShortUrlPath())
            .thenReturn(shortUrlPath)
        `when`(shortenerRepository.saveIfNotExists(shortUrl, null))
            .thenReturn(true)

        val newShortUrl = base64Saver.save(originalUrl, null)
        MatcherAssert.assertThat(newShortUrl, Matchers.equalTo(shortUrl))
    }

    @Test
    fun `when Short URL Path has duplicates lt 5 times then return Short URL`() {
        val shortUrlPath = URI("path")
        val originalUrl = URL("https://example.com")
        val shortUrl = ShortUrl(shortUrlPath, originalUrl)
        `when`(shortUrlPathService.getShortUrlPath())
            .thenReturn(shortUrlPath)
        `when`(shortenerRepository.saveIfNotExists(shortUrl, null))
            .thenReturn(false, false, false, false, true)

        val newShortUrl = base64Saver.save(originalUrl, null)
        MatcherAssert.assertThat(newShortUrl, Matchers.equalTo(shortUrl))
    }

    @Test
    fun `when Short URL Path has duplicates 5 times then throw SaveException`() {
        val shortUrlPathDuplicate = URI("duplicate")
        val shortUrlPath = URI("path")
        val originalUrl = URL("https://example.com")
        val shortUrl = ShortUrl(shortUrlPath, originalUrl)
        `when`(shortUrlPathService.getShortUrlPath())
            .thenReturn(
                shortUrlPathDuplicate, shortUrlPathDuplicate,
                shortUrlPathDuplicate, shortUrlPathDuplicate, shortUrlPath
            )
        `when`(shortenerRepository.saveIfNotExists(ShortUrl(shortUrlPathDuplicate, originalUrl),null))
            .thenReturn(false)
        `when`(shortenerRepository.saveIfNotExists(shortUrl, null))
            .thenReturn(false, false, false, false, false)

        val saveException = org.junit.jupiter.api.assertThrows<SaveException> {
            base64Saver.save(originalUrl, null)
        }
        MatcherAssert.assertThat(
            saveException.message,
            Matchers.equalTo("Can not shorten Origin URL")
        )
    }
}
