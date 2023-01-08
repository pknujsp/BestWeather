package com.lifedawn.bestweather.data.remote.weather.owm.datasource

import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import javax.inject.Inject

class OwmDataSourceImpl @Inject constructor(private val owmOneCallRestApi: RestfulApiQuery) : OwmDataSource {
}