package com.example.mindfulu

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class App : Application() {
    companion object {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        // Add logging interceptor for debugging
        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Configure OkHttp with longer timeouts
        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("http://10.15.3.148:3000/") // Make sure this ends with /
            .build()

        val retrofitService = retrofit.create(WebService::class.java)
    }

    override fun onCreate() {
        super.onCreate()
    }
}