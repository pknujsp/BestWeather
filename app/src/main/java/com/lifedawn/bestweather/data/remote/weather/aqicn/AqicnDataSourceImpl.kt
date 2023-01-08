package com.lifedawn.bestweather.data.remote.weather.aqicn

import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import javax.inject.Inject

class AqicnDataSourceImpl @Inject constructor(private val aqicnRestApi: RestfulApiQuery) : AqicnDataSource {
}