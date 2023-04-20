package com.lifedawn.bestweather.data.remote.weather.kma.repository

import android.content.Context
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaCurrentConditionsParameters
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaForecastsParameters
import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.kma.datasource.KmaDataSource
import com.lifedawn.bestweather.data.remote.weather.kma.parser.KmaWebParser
import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaCurrentConditions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.zip
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class KmaWeatherRepositoryImpl @Inject constructor(
    private val kmaDataSource: KmaDataSource,
    private val context: Context
) : KmaWeatherRepository {

    private fun getParsedCurrentConditions(areaCode: String): Flow<ParsedKmaCurrentConditions?> =
        flow {
            val response = kmaDataSource.getCurrentConditions(KmaCurrentConditionsParameters(areaCode))
            response.collect {
                if (it is ApiResponse.Success) {
                    val document = Jsoup.parse(it.data)
                    val parsedCurrentConditions =
                        KmaWebParser.parseCurrentConditions(document, ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                    emit(parsedCurrentConditions)
                } else
                    emit(null)
            }

        }

    private fun getForecastsDocument(areaCode: String): Flow<Document?> = flow {
        val response = kmaDataSource.getForecasts(KmaForecastsParameters(areaCode))
        response.collect {
            if (it is ApiResponse.Success)
                emit(Jsoup.parse(it.data))
            else
                emit(null)

        }
    }

    override fun getWeatherData(
        weatherDataTypes: Set<WeatherDataType>,
        areaCode: String,
        latitude: Double,
        longitude: Double
    ): Flow<WeatherDataDto> = flow {
        var parsedKmaCurrentConditionsFlow: Flow<ParsedKmaCurrentConditions?>? = null
        val forecastsDocumentFlow: Flow<Document?> = getForecastsDocument(areaCode)

        if (weatherDataTypes.contains(WeatherDataType.currentConditions))
            parsedKmaCurrentConditionsFlow = getParsedCurrentConditions(areaCode)

        if (parsedKmaCurrentConditionsFlow == null) {
            forecastsDocumentFlow.collect {
                val weatherDataDto = WeatherDataDto(weatherDataTypes = weatherDataTypes)

                if (weatherDataTypes.contains(WeatherDataType.hourlyForecast))
                    weatherDataDto.hourlyForecasts = makeHourlyForecasts(it, latitude, longitude)
                if (weatherDataTypes.contains(WeatherDataType.dailyForecast))
                    weatherDataDto.dailyForecasts = makeDailyForecasts(it)

                emit(weatherDataDto)
            }
        } else {
            forecastsDocumentFlow.zip(parsedKmaCurrentConditionsFlow) { forecastsDocument, parsedCurrentConditions ->
                val weatherDataDto = WeatherDataDto(weatherDataTypes = weatherDataTypes)

                if (weatherDataTypes.contains(WeatherDataType.hourlyForecast))
                    weatherDataDto.hourlyForecasts = makeHourlyForecasts(forecastsDocument, latitude, longitude)
                if (weatherDataTypes.contains(WeatherDataType.dailyForecast))
                    weatherDataDto.dailyForecasts = makeDailyForecasts(forecastsDocument)
                if (weatherDataTypes.contains(WeatherDataType.currentConditions))
                    weatherDataDto.currentConditions = makeCurrentConditions(parsedCurrentConditions, latitude, longitude)

                weatherDataDto
            }.collect {
                emit(it)
            }
        }
    }

    private fun makeCurrentConditions(
        parsedKmaCurrentConditions: ParsedKmaCurrentConditions?,
        latitude: Double,
        longitude: Double
    ) =
        KmaResponseProcessor.makeFirstCurrentConditions(context, parsedKmaCurrentConditions, latitude, longitude)

    private fun makeHourlyForecasts(
        document: Document?, latitude: Double,
        longitude: Double
    ): List<HourlyForecastDto> {
        val parsedHourlyForecasts = KmaWebParser.parseHourlyForecasts(document)
        return KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(
            context, parsedHourlyForecasts, latitude, longitude
        )
    }

    private fun makeDailyForecasts(document: Document?):
            List<DailyForecastDto> {
        val parsedDailyForecasts = KmaWebParser.parseDailyForecasts(document)
        return KmaResponseProcessor.makeDailyForecastDtoListOfWEB(
            parsedDailyForecasts
        )
    }
}