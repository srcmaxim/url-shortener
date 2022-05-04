package com.srcmaxim.controller

import com.srcmaxim.service.ShortenerService
import com.srcmaxim.service.saver.SaveException
import org.jboss.logging.Logger
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo

@Path("/")
class ShortenerController(
    @field:Inject private val shortenerService: ShortenerService
) {

    private val logger = Logger.getLogger(ShortenerController::class.java)

    /**
     * Creates short URL from original URL.
     *
     * Params:
     * originalUrl (string): Original URL to be shortened.
     * customAlias (string): Optional custom key for the URL.
     * expireDate (string): Optional expiration date for the shortened URL.
     * return (string): Shortened URL.
     */
    @POST
    @Path("/shorten")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createShortUrl(
        originalUrl: String,
        @QueryParam("customAlias") customAlias: String?,
        @QueryParam("ttl") ttl: Int?,
        @Context info: UriInfo
    ): Response {
        logger.debugf("Creating redirect: %s %s", originalUrl, customAlias)
        val parsedOriginalUrl: URL = try {
            URL(originalUrl)
        } catch (e: MalformedURLException) {
            return badRequest("Original URL is malformed", originalUrl)
        }
        val parsedCustomAlias: URI? = customAlias?.let {
            try {
                URI(it)
            } catch (e: URISyntaxException) {
                return badRequest("Custom alias is malformed", it)
            }
        }

        return try {
            val shortUrl =
                shortenerService.createShortUrlPath(parsedOriginalUrl, parsedCustomAlias, ttl)
            val shortUrlPath = shortUrl.shortUrlPath.toString()
            val uri: URI = info.baseUriBuilder.path(shortUrlPath).build()
            logger.debugf("Creating redirect successful: %s %s %s", originalUrl, customAlias, uri)
            Response.ok(uri.toString()).build()
        } catch (e: SaveException) {
            logger.debugf("Creating redirect exception: %s %s %s", originalUrl, customAlias, e.message)
            Response.status(Response.Status.BAD_REQUEST)
                .entity(e.message)
                .build()
        }
    }

    /**
     * Redirects short URL to original URl.
     *
     * Params:
     * shortUrl (string): Shortened URL.
     * return (string): Original URL to be shortened.
     */
    @GET
    @Path("/{shortUrl}")
    fun redirectShortToOriginalUrl(@PathParam("shortUrl") shortUrl: String): Response {
        logger.debugf("Redirecting: /%s", shortUrl)
        val originalUrl = shortenerService.getRedirectUrl(shortUrl)?.originalUrl
        return if (originalUrl != null) {
            logger.debugf("Redirect successful: %s", originalUrl)
            Response.temporaryRedirect(originalUrl.toURI()).build()
        } else {
            logger.debugf("Redirect not found: /%s", shortUrl)
            Response.status(Response.Status.NOT_FOUND)
                .entity("No URL Found")
                .build()
        }
    }

    private fun badRequest(message: String, parsedString: String): Response {
        logger.debugf("%s: %s", message, parsedString)
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(message)
            .build()
    }

}
