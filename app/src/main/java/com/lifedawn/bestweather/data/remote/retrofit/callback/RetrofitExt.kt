package com.lifedawn.bestweather.data.remote.retrofit.callback

import retrofit2.Call
import retrofit2.Response

fun <T> Response<T>.result(): ApiResponse<T> =
    body()?.run {
        ApiResponse.Success(this@result)
    } ?: errorBody()?.run {
        ApiResponse.Failure(Throwable(string()))
    } ?: ApiResponse.Failure(Throwable("failure"))
