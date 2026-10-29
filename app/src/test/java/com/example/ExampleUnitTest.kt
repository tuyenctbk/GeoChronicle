package com.example

import com.example.data.api.ApiClient
import com.example.data.model.MediaWikiGeosearchResponse
import com.example.data.model.PlaceCategory
import com.example.data.model.SupportedLanguages
import com.example.location.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testBearingDirectionFormatting() {
        assertEquals("N", LocationHelper.formatBearingDirection(0f))
        assertEquals("E", LocationHelper.formatBearingDirection(90f))
        assertEquals("S", LocationHelper.formatBearingDirection(180f))
        assertEquals("W", LocationHelper.formatBearingDirection(270f))
        assertEquals("NE", LocationHelper.formatBearingDirection(45f))
    }

    @Test
    fun testDistanceFormatting() {
        assertEquals("450 m", LocationHelper.formatDistance(450.0))
        assertEquals("1.2 km", LocationHelper.formatDistance(1200.0))
        assertEquals("5.0 km", LocationHelper.formatDistance(5000.0))
    }

    @Test
    fun testCategoryClassification() {
        assertEquals(PlaceCategory.SACRED, PlaceCategory.fromText("St. Peter's Basilica", "ancient cathedral church"))
        assertEquals(PlaceCategory.ARCHITECTURE, PlaceCategory.fromText("Tower of London", "medieval stone fortress"))
        assertEquals(PlaceCategory.MONUMENT, PlaceCategory.fromText("Equestrian Statue of Marcus Aurelius", "bronze monument"))
        assertEquals(PlaceCategory.EVENT, PlaceCategory.fromText("Battle of Marathon", "historic conflict"))
    }

    @Test
    fun testMediaWikiGeosearchParsing() {
        val sampleJson = """
            {
                "batchcomplete": true,
                "query": {
                    "geosearch": [
                        {
                            "pageid": 12345,
                            "ns": 0,
                            "title": "Colosseum",
                            "lat": 41.8902,
                            "lon": 12.4922,
                            "dist": 150.5,
                            "primary": true
                        }
                    ]
                }
            }
        """.trimIndent()

        val adapter = ApiClient.moshi.adapter(MediaWikiGeosearchResponse::class.java)
        val response = adapter.fromJson(sampleJson)
        assertNotNull(response)
        val items = response?.query?.geosearch
        assertNotNull(items)
        assertEquals(1, items?.size)
        assertEquals(12345L, items?.get(0)?.pageid)
        assertEquals("Colosseum", items?.get(0)?.title)
        assertEquals(41.8902, items?.get(0)?.lat ?: 0.0, 0.0001)
    }

    @Test
    fun testSupportedLanguagesCount() {
        // Ensure 10 languages are configured
        assertEquals(10, SupportedLanguages.LANGUAGES.size)
        val codes = SupportedLanguages.LANGUAGES.map { it.code }
        assertTrue(codes.contains("en"))
        assertTrue(codes.contains("es"))
        assertTrue(codes.contains("fr"))
        assertTrue(codes.contains("de"))
        assertTrue(codes.contains("it"))
        assertTrue(codes.contains("ja"))
        assertTrue(codes.contains("zh"))
        assertTrue(codes.contains("pt"))
        assertTrue(codes.contains("vi"))
        assertTrue(codes.contains("ar"))
    }
}
