package com.loan555.mvvm_musicapp.api.config

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiSearchConfig {
    private const val BaseUrl = "http://ac.mp3.zing.vn/"
    private val builder = Retrofit.Builder()
        .baseUrl(BaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
    val retrofit = builder.build()!!
}