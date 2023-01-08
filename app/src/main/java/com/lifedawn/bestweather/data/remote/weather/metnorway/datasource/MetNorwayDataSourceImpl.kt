package com.lifedawn.bestweather.data.remote.weather.metnorway.datasource

import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import javax.inject.Inject

class MetNorwayDataSourceImpl @Inject constructor(private val metNorwayRestApi: RestfulApiQuery) : MetNorwayDataSource {
}