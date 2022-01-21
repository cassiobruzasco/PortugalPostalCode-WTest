package com.cassiobruzasco.wtest.data

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface DownloadService {

    @GET
    fun downloadCsv(@Url csvUrl: String): Call<ResponseBody>
}