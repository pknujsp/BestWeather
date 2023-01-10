package com.lifedawn.bestweather.data.remote.retrofit.callback

fun <T> ApiResponse<T>.Success(onResult: ApiResponse.Success<T>.() -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Success)
        onResult(this)
    return this
}

fun <T> ApiResponse<T>.Failure(onResult: ApiResponse.Failure<*>.() -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Failure<*>)
        onResult(this)
    return this
}