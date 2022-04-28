package com.srcmaxim

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

@QuarkusTest
class ShortenerControllerTest {

    private final val serviceURL = "http://localhost:8081"
    private final val base64Pattern = Pattern.compile("$serviceURL/[\\p{Alnum}-_]{6}")

    @Test
    fun `when short URL with alias is created then follow alias link`() {
        given()
            .`when`()
            .contentType(ContentType.JSON)
            .body("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            .queryParam("customAlias", "NeverGiveUp")
            .post("/shorten")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(`is`("$serviceURL/NeverGiveUp"))

        given()
            .`when`()
            .redirects().follow(false)
            .get("/NeverGiveUp")
            .then()
            .statusCode(307)
            .header("Location", `is`("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))

        // Follow link
        given()
            .`when`()
            .get("/NeverGiveUp")
            .then()
            .statusCode(200);
    }

    @Test
    fun `when short URL without alias is created then follow base64{6} link`() {
        val shortUrl = given()
            .`when`()
            .contentType(ContentType.JSON)
            .body("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            .post("/shorten")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(notNullValue())
            .extract()
            .body().asString()

        base64Pattern.matcher(shortUrl).matches()

        val shortPath = shortUrl.substring("http://localhost:8081".length)
        given()
            .`when`()
            .redirects().follow(false)
            .get(shortPath)
            .then()
            .statusCode(307)
            .header("Location", `is`("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))

        // Follow link
        given()
            .`when`()
            .get(shortPath)
            .then()
            .statusCode(200)
    }

    @Test
    fun `when no short URL is created then return 404`() {
        val shortPath = "/no_url"
        given()
            .`when`()
            .redirects().follow(false)
            .get(shortPath)
            .then()
            .statusCode(404)
            .header("Location", Matchers.nullValue())
    }
}
