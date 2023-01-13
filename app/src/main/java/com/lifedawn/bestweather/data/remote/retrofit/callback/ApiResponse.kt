package com.lifedawn.bestweather.data.remote.retrofit.callback

sealed class ApiResponse<out T> {
    class Success<out T>(response: T) : ApiResponse<T>() {
        val data = response
    }

    class Failure(throwable: Throwable) : ApiResponse<Nothing>() {
        val message: String? = throwable.localizedMessage
    }

    object Loading: ApiResponse<Nothing>()
}
