package com.example.mindfulu

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class App : Application() {
    companion object {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("http://192.168.1.3:3000/") // Make sure this matches your backend URL
            .build()

        val retrofitService = retrofit.create(WebService::class.java)
    }

    override fun onCreate() {
        super.onCreate()
    }
}