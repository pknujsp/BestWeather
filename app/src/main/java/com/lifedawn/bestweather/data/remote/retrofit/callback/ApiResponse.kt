package com.lifedawn.bestweather.data.remote.retrofit.callback

import retrofit2.Response

sealed class ApiResponse<out T> {
    class Success<T>(response: Response<T>) : ApiResponse<T>() {
        val data = response.body()
    }

    class Failure<T>(throwable: Throwable) : ApiResponse<T>() {
        val message: String? = throwable.localizedMessage
    }
}
