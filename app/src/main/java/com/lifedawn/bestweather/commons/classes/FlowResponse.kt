package com.lifedawn.bestweather.commons.classes

import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.io.IOException

abstract class FlowResponse<T> {

    fun onFlowResult(response: Response<T>): Flow<ApiResponse<T>> = flow {
        if (response.body() != null) {
            response.body()?.also {
                emit(ApiResponse.Success(it))
            }
        } else {
            response.errorBody()?.also {
                try {
                    emit(ApiResponse.Error(it.toString()))
                } catch (e: IOException) {
                    emit(ApiResponse.Error("failed"))
                }
            } ?: emit(ApiResponse.Error("failed"))
        }
    }
}