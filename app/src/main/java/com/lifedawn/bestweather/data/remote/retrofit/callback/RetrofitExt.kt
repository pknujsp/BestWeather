package com.lifedawn.bestweather.data.remote.retrofit.callback

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

fun <T : Any> requestApiFlow(request: suspend () -> Response<T>): Flow<ApiResponse<T>> = flow {
    emit(ApiResponse.Loading)

    try {
        val result = request()

        result.body()?.run {
            emit(ApiResponse.Success(this))
        } ?: result.errorBody()?.run {
            emit(ApiResponse.Failure(Throwable(toString())))
        } ?: emit(ApiResponse.Failure(Throwable("failure")))
    } catch (e: Exception) {
        emit(ApiResponse.Failure(e))
    }

}