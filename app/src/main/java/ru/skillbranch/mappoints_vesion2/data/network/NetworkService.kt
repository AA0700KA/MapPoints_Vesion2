package ru.skillbranch.mappoints_vesion2.data.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkService {

    private val BASE_URL = "https://maps.googleapis.com/maps/api/"
    private val retrofit : Retrofit

    init {
         retrofit = Retrofit.Builder()
             .baseUrl(BASE_URL)
             .addConverterFactory(GsonConverterFactory.create())
             .addCallAdapterFactory(CoroutineCallAdapterFactory())
             .build()
    }

    fun getApi() : Api {
        return retrofit.create(Api::class.java)
    }

}