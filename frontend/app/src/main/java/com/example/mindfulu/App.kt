package com.example.a222117007_m7

import android.app.Application
import com.example.mindfulu.WebService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class App:Application() {
    companion object{
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder().addConverterFactory(
            MoshiConverterFactory.create(moshi)
        ).baseUrl("http://10.10.0.234:3000/").build()
        val retrofitService = retrofit.create(WebService::class.java)
    }
    override fun onCreate() {
        super.onCreate()
    }
}