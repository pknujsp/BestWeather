package com.lifedawn.bestweather.ui.findaddress

import com.lifedawn.bestweather.data.remote.retrofit.parameters.google.GoogleFindPlaceParameterRest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object GoogleFindPlaceProcessing {
    fun getGooglePlaceSearchResponse(response: String?): GooglePlaceSearchResponse {
        return Gson().fromJson(response, GooglePlaceSearchResponse::class.java)
    }

    fun findPlace(input: String?, callback: JsonDownloader) {
        val restfulApiQuery: RestfulApiQuery = RetrofitClient.getApiService(RetrofitClient.ServiceType.GOOGLE_PLACE_SEARCH)
        val parameter: GoogleFindPlaceParameterRest = GoogleFindPlaceParameterRest(input)
        val call: Call<JsonElement> = restfulApiQuery.getFindPlaceSearch(parameter.getMap())
        call.enqueue(object : Callback<JsonElement?> {
            public override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.body() != null) {
                    val googlePlaceSearchResponse: GooglePlaceSearchResponse = getGooglePlaceSearchResponse(response.body().toString())
                    if (googlePlaceSearchResponse.getStatus().equals("OK")) {
                        callback.onResponseResult(response, googlePlaceSearchResponse, response.body().toString())
                    } else {
                        callback.onResponseResult(Exception())
                    }
                } else {
                    callback.onResponseResult(Exception())
                }
            }

            public override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                callback.onResponseResult(t)
            }
        })
    }
}