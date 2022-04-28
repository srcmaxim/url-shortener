package com.srcmaxim.service.saver

import com.srcmaxim.domain.entity.ShortUrl
import com.srcmaxim.repository.ShortenerRepository
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import java.net.URI
import java.net.URL
import javax.inject.Inject

@QuarkusTest
class AliasSaverTest {

    @Inject
    private lateinit var aliasSaver: AliasSaver

    @InjectMock
    private lateinit var shortenerRepository: ShortenerRepository

    @Test
    fun `when no Short URL Path duplications then return Short URL`() {
        val customAlias = URI("path")
        val originalUrl = URL("https://example.com")
        var shortUrl = ShortUrl(customAlias, originalUrl)
        `when`(shortenerRepository.saveIfNotExists(shortUrl, null))
            .thenReturn(true)

        val newShortUrl = aliasSaver.save(originalUrl, customAlias, null)
        assertThat(newShortUrl, Matchers.equalTo(shortUrl))
    }

    @Test
    fun `when Short URL Path has duplicate then throw SaveException`() {
        val customAlias = URI("path")
        val originalUrl = URL("https://example.com")
        val shortUrl = ShortUrl(customAlias, originalUrl)
        `when`(shortenerRepository.saveIfNotExists(shortUrl, null))
            .thenThrow(SaveException)

        val saveException = assertThrows<SaveException> {
            aliasSaver.save(originalUrl, customAlias, null)
        }
        assertThat(saveException.message, equalTo("Can not shorten Origin URL"))
    }
}
