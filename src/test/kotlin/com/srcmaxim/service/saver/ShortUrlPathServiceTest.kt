package com.srcmaxim.service.saver

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.regex.Pattern
import javax.inject.Inject

@QuarkusTest
class ShortUrlPathServiceTest {

    @Inject
    private lateinit var shortUrlPathService: ShortUrlPathService

    private final val base64Pattern = Pattern.compile("[\\p{Alnum}-_]{6}")

    @Test
    fun getShortUrlPath() {
        val shortUrlPath = shortUrlPathService.getShortUrlPath().toString()
        val matches = base64Pattern.matcher(shortUrlPath).matches()
        assertTrue(matches)
    }
}
