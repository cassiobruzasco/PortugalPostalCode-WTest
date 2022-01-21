package com.cassiobruzasco.wtest.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder().baseUrl(
                "https://github.com/centraldedados/codigos_postais/raw/master/data/"
            ).addConverterFactory(GsonConverterFactory.create()).build()
        }
    }
}