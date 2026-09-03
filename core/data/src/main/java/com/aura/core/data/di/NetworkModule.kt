package com.aura.core.data.di

import android.util.Log
import com.aura.core.auth.AuthRepository
import com.aura.core.data.remote.AtmosphereApi
import com.aura.core.data.remote.CatalogApi
import com.aura.core.data.remote.LyricsApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
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
private const val BASE_URL = "https://aura-backend.sonameydenaura.workers.dev/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Attaches the current Firebase ID token as `Authorization: Bearer <token>`
     * on every request, once the backend's endpoints are protected (see the
     * matching src/lib/firebaseAuth.ts middleware in the AURA_Backend project).
     * Firebase caches the token locally and only makes a network call when it's
     * actually expired, so this blocking call is cheap in the common case —
     * still, an unauthenticated user (no current FirebaseUser) simply sends
     * no Authorization header, which public/dev endpoints tolerate fine.
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(authRepository: AuthRepository): Interceptor = Interceptor { chain ->
        val token = runBlocking { authRepository.getIdToken() }
        Log.d("AURA_TOKEN", "Bearer $token")
        val request = if (token != null) {
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // Never log full bodies in release builds — avoid leaking user data,
            // per the "avoid logging tokens/personal data" best practice.
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
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
