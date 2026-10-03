package com.application.requiemproject.data.api

import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface AiTranslationApi {
    @POST
    suspend fun generate(@Url url: String, @HeaderMap headers: Map<String, String>, @Body body: JsonObject): Response<JsonObject>

    companion object {
        fun create(): AiTranslationApi = Retrofit.Builder().baseUrl("https://api.openai.com/")
            .client(OkHttpClient.Builder().callTimeout(90, TimeUnit.SECONDS).readTimeout(90, TimeUnit.SECONDS)
                .followRedirects(false).followSslRedirects(false).build())
            .addConverterFactory(GsonConverterFactory.create()).build().create(AiTranslationApi::class.java)
    }
}
