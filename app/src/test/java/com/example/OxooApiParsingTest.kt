package com.example

import com.example.data.local.SettingsPreferences
import com.example.data.remote.HomeContentResponse
import com.example.data.remote.NetworkModule
import com.example.data.remote.isTruthy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OxooApiParsingTest {

    @Test
    fun parseCombinedOxooUrl_extractsBaseAndApiKey() {
        val parsed = SettingsPreferences.parseEndpointInput(
            "https://topmovies.info/rest-api/5ahofo1htud0ak7bkacckwbz"
        )
        assertEquals("https://topmovies.info/rest-api/", parsed.baseUrl)
        assertEquals("5ahofo1htud0ak7bkacckwbz", parsed.apiKey)
        assertEquals("https://topmovies.info/rest-api/5ahofo1htud0ak7bkacckwbz", parsed.combinedUrl)
    }

    @Test
    fun moshi_parsesMixedPhpJsonTypesGracefully() {
        val sampleJson = """
            {
              "latest_movies": [
                {
                  "videos_id": 104,
                  "title": "Sample Cinema",
                  "release": 2025,
                  "imdb_rating": 8.4,
                  "is_paid": 0,
                  "is_tvseries": false,
                  "video_quality": "4K"
                }
              ]
            }
        """.trimIndent()

        val adapter = NetworkModule.moshi.adapter(HomeContentResponse::class.java)
        val parsed = adapter.fromJson(sampleJson)
        assertNotNull(parsed)
        val movie = parsed!!.latestMovies.first()
        assertEquals("104", movie.videosId)
        assertEquals("Sample Cinema", movie.title)
        assertEquals("2025", movie.release)
        assertEquals("8.4", movie.imdbRating)
        assertFalse(movie.isPaid.isTruthy())
        assertEquals("movie", movie.contentType)
        assertTrue("1".isTruthy())
    }
}
