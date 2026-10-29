package com.example.data.api

import com.example.data.model.MediaWikiGeosearchResponse
import com.example.data.model.MediaWikiPagesResponse
import com.example.data.model.NominatimReverseResponse
import com.example.data.model.WikiPageSummary
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

/**
 * Retrofit service interface specifically for MediaWiki APIs:
 * - action=query & list=geosearch
 * - action=query & prop=extracts|pageimages|coordinates|info
 * - REST API summary fallback
 */
interface MediaWikiApiService {

    /**
     * MediaWiki Geosearch API:
     * https://{lang}.wikipedia.org/w/api.php?action=query&list=geosearch&gscoord={lat}|{lon}&gsradius={radius}&gslimit={limit}&format=json
     */
    @Headers("User-Agent: GeoChronicle-HistoryWalk/1.0 (Android; Open Knowledge Educational Tour)")
    @GET
    suspend fun searchNearbyLandmarks(
        @Url endpointUrl: String,
        @Query("action") action: String = "query",
        @Query("list") list: String = "geosearch",
        @Query("gscoord") coord: String,
        @Query("gsradius") radiusMeters: Int,
        @Query("gslimit") limit: Int = 40,
        @Query("gsglobe") globe: String = "earth",
        @Query("format") format: String = "json",
        @Query("formatversion") formatVersion: Int = 2
    ): MediaWikiGeosearchResponse

    /**
     * MediaWiki Query API for multiple page extracts, images, and coordinates by page IDs:
     * https://{lang}.wikipedia.org/w/api.php?action=query&pageids={pageids}&prop=extracts|pageimages|coordinates|info&inprop=url&format=json
     */
    @Headers("User-Agent: GeoChronicle-HistoryWalk/1.0 (Android; Open Knowledge Educational Tour)")
    @GET
    suspend fun getPageDetailsByIds(
        @Url endpointUrl: String,
        @Query("action") action: String = "query",
        @Query("pageids") pageIds: String,
        @Query("prop") prop: String = "extracts|pageimages|coordinates|info",
        @Query("exintro") exIntro: Boolean = true,
        @Query("explaintext") explainText: Boolean = true,
        @Query("exsentences") exSentences: Int = 4,
        @Query("piprop") piProp: String = "thumbnail",
        @Query("pithumbsize") thumbSize: Int = 600,
        @Query("inprop") inProp: String = "url",
        @Query("format") format: String = "json",
        @Query("formatversion") formatVersion: Int = 2
    ): MediaWikiPagesResponse

    /**
     * MediaWiki REST API Page Summary endpoint:
     * https://{lang}.wikipedia.org/api/rest_v1/page/summary/{title}
     */
    @Headers("User-Agent: GeoChronicle-HistoryWalk/1.0 (Android; Open Knowledge Educational Tour)")
    @GET
    suspend fun getPageSummary(
        @Url fullSummaryUrl: String
    ): WikiPageSummary
}

interface NominatimApiService {

    @Headers("User-Agent: GeoChronicle-Android/1.0 (contact: geo-chronicle@example.com)")
    @GET("reverse?format=json")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("accept-language") language: String? = null
    ): NominatimReverseResponse
}

object ApiClient {
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val mediaWikiService: MediaWikiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://en.wikipedia.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MediaWikiApiService::class.java)
    }

    val nominatimService: NominatimApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NominatimApiService::class.java)
    }
}
