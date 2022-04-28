package com.srcmaxim.repository

import com.srcmaxim.domain.entity.ShortUrl
import io.quarkus.test.junit.QuarkusTest
import org.apache.commons.lang3.ThreadUtils.sleep
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.URL
import java.time.Duration
import javax.inject.Inject

val originalUrl = URL("https://example.com")

@QuarkusTest
internal class ShortenerRepositoryTest {

    @Inject
    private lateinit var shortenerRepository: ShortenerRepository

    @Test
    fun `do not delete Short URL if no ttl`() {
        val shortUrlPath = URI("a0")
        shortenerRepository.saveIfNotExists(
            ShortUrl(shortUrlPath, originalUrl),
            null
        )

        shortenerRepository.findById(shortUrlPath.toString())
    }

    @Test
    fun `delete Short URL if ttl expires`() {
        val shortUrlPathA = URI("a1")
        val shortUrlPathB = URI("b1")
        val shortUrlPathC = URI("c1")
        val shortUrlPathD = URI("d1")
        shortenerRepository.saveIfNotExists(ShortUrl(shortUrlPathA, originalUrl), 100)
        shortenerRepository.saveIfNotExists(ShortUrl(shortUrlPathB, originalUrl), 200)
        shortenerRepository.saveIfNotExists(ShortUrl(shortUrlPathC, originalUrl), 400)
        shortenerRepository.saveIfNotExists(ShortUrl(shortUrlPathD, originalUrl), 500)

        sleep(Duration.ofMillis(300))

        val shortUrlAExpired = shortenerRepository.findById(shortUrlPathA.toString())
        val shortUrlBExpired = shortenerRepository.findById(shortUrlPathB.toString())
        val shortUrlCNotExpired = shortenerRepository.findById(shortUrlPathC.toString())
        val shortUrlDNotExpired = shortenerRepository.findById(shortUrlPathD.toString())
        assertThat(shortUrlAExpired, Matchers.nullValue())
        assertThat(shortUrlBExpired, Matchers.nullValue())
        assertThat(shortUrlCNotExpired, Matchers.notNullValue())
        assertThat(shortUrlDNotExpired, Matchers.notNullValue())
    }
}
