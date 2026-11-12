package com.example.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface TopMoviesApiService {

    @GET
    suspend fun getHomeContent(
        @Url url: String,
        @Header("API-KEY") apiKey: String
    ): Response<HomeContentResponse>

    @GET
    suspend fun getVideoList(
        @Url url: String,
        @Header("API-KEY") apiKey: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): Response<List<VideoSummary>>

    @GET
    suspend fun getSingleDetails(
        @Url url: String,
        @Header("API-KEY") apiKey: String,
        @Query("type") type: String,
        @Query("id") id: String
    ): Response<SingleDetailsResponse>

    @GET
    suspend fun getLiveTvByCategory(
        @Url url: String,
        @Header("API-KEY") apiKey: String,
        @Query("page") page: Int = 1
    ): Response<List<LiveTvCategory>>

    @GET
    suspend fun search(
        @Url url: String,
        @Header("API-KEY") apiKey: String,
        @Query("q") query: String,
        @Query("type") type: String = "movie,tvseries,live_tv",
        @Query("page") page: Int = 1
    ): Response<SearchResponse>

    @GET
    suspend fun getGenres(
        @Url url: String,
        @Header("API-KEY") apiKey: String
    ): Response<List<GenreItem>>

    @GET
    suspend fun getCountries(
        @Url url: String,
        @Header("API-KEY") apiKey: String
    ): Response<List<CountryItem>>

    @GET
    suspend fun getConfig(
        @Url url: String,
        @Header("API-KEY") apiKey: String
    ): Response<AppConfigResponse>
}
