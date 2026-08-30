package com.aura.core.data.di

import com.aura.core.data.remote.AtmosphereApi
import com.aura.core.data.remote.CatalogApi
import com.aura.core.data.remote.LyricsApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Base URL points ONLY at your own backend (Cloudflare Workers / Firebase
 * Functions / Ktor server — see Section 9 of the project spec). This app
 * never holds an OpenAI, Genius/Musixmatch, R2, or Supabase credential.
 *
 * Replace with your real deployed backend URL before building a release.
 */
private const val BASE_URL = "https://api.aura-app.example.com/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // Never log full bodies in release builds — avoid leaking user data,
            // per the "avoid logging tokens/personal data" best practice.
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideCatalogApi(retrofit: Retrofit): CatalogApi = retrofit.create(CatalogApi::class.java)

    @Provides
    @Singleton
    fun provideAtmosphereApi(retrofit: Retrofit): AtmosphereApi = retrofit.create(AtmosphereApi::class.java)

    @Provides
    @Singleton
    fun provideLyricsApi(retrofit: Retrofit): LyricsApi = retrofit.create(LyricsApi::class.java)
}
