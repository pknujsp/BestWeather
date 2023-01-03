package com.lifedawn.bestweather.data.remote.flickr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient.getApiService
import com.lifedawn.bestweather.data.remote.retrofit.parameters.flickr.FlickrGetInfoParameter
import com.lifedawn.bestweather.data.remote.retrofit.parameters.flickr.FlickrGetPhotosFromGalleryParameter
import com.lifedawn.bestweather.data.remote.retrofit.responses.flickr.GetInfoPhotoResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.flickr.PhotosFromGalleryResponse
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.*
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class FlickrRepositoryImpl(context: Context) : FlickrRepository {

    init {
        FlickrUtil.init()
    }

    override suspend fun loadImg(flickrRequestParameter: FlickrRequestParameter) =
        suspendCancellableCoroutine<FlickrImgResponse>
        { continuation ->
                cancelAllRequests()
                val lastRefreshDateTime = flickrRequestParameter.refreshDateTime.withZoneSameInstant(flickrRequestParameter.zoneId)
                val timeZone = SimpleTimeZone(lastRefreshDateTime.offset.totalSeconds * 1000, "")
                val currentCalendar = Calendar.getInstance(timeZone)
                currentCalendar[lastRefreshDateTime.year, lastRefreshDateTime.monthValue - 1, lastRefreshDateTime.dayOfMonth, lastRefreshDateTime.hour, lastRefreshDateTime.minute] =
                    lastRefreshDateTime.second
                val sunRiseSunsetCalculator = SunriseSunsetCalculator(
                    Location(flickrRequestParameter.latitude, flickrRequestParameter.longitude),
                    currentCalendar.timeZone
                )
                val sunRiseCalendar = sunRiseSunsetCalculator.getOfficialSunriseCalendarForDate(currentCalendar)
                val sunSetCalendar = sunRiseSunsetCalculator.getOfficialSunsetCalendarForDate(currentCalendar)
                if (sunRiseCalendar == null || sunSetCalendar == null) {
                    glideImgCallback.onLoadedImg(FlickrImgResponse(null, false))
                    return@Runnable
                }
                val currentTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(currentCalendar.timeInMillis)
                val sunRiseTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(sunRiseCalendar.timeInMillis)
                val sunSetTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(sunSetCalendar.timeInMillis)
                var time: String? = null
                //현재 시각 파악 : 낮, 밤, 일출, 일몰(+-20분)
                time = if (currentTimeMinutes < sunRiseTimeMinutes - 2) {
                    //새벽
                    "night"
                } else if (currentTimeMinutes <= sunRiseTimeMinutes + 15) {
                    //일출
                    "sunrise"
                } else if (currentTimeMinutes > sunRiseTimeMinutes + 15 && currentTimeMinutes <= sunSetTimeMinutes - 15) {
                    //낮
                    "day"
                } else if (currentTimeMinutes < sunSetTimeMinutes + 2) {
                    //일몰
                    "sunset"
                } else {
                    //밤
                    "night"
                }
                var weather: String? = null
                when (flickrRequestParameter.weatherProviderType) {
                    WeatherProviderType.KMA_WEB -> {
                        val pty = KmaResponseProcessor.convertPtyTextToCode(flickrRequestParameter.weatherDescription)
                        val sky = KmaResponseProcessor.convertSkyTextToCode(flickrRequestParameter.weatherDescription)
                        weather = if (pty == null) KmaResponseProcessor.getSkyFlickrGalleryName(
                            sky
                        ) else KmaResponseProcessor.getPtyFlickrGalleryName(pty)
                    }
                    WeatherProviderType.KMA_API -> {
                        val code = flickrRequestParameter.weatherDescription.substring(0, 1)
                        weather =
                            if (flickrRequestParameter.weatherDescription.contains("_sky")) KmaResponseProcessor.getSkyFlickrGalleryName(
                                code
                            ) else KmaResponseProcessor.getPtyFlickrGalleryName(code)
                    }
                    WeatherProviderType.ACCU_WEATHER -> weather =
                        AccuWeatherResponseProcessor.getFlickrGalleryName(flickrRequestParameter.weatherDescription)
                    WeatherProviderType.OWM_ONECALL -> weather =
                        OpenWeatherMapResponseProcessor.getFlickrGalleryName(flickrRequestParameter.weatherDescription)
                    WeatherProviderType.MET_NORWAY -> weather =
                        MetNorwayResponseProcessor.getFlickrGalleryName(flickrRequestParameter.weatherDescription)
                }
                val galleryName = "$time $weather"
                // time : sunrise, sunset, day, night
                // weather : clear, partly cloudy, mostly cloudy, overcast, rain, snow

                //이미 다운로드 된 이미지가 있으면 다운로드 하지 않음
                if (BACKGROUND_IMG_MAP.containsKey(galleryName) && BACKGROUND_IMG_MAP[galleryName]!!
                        .img != null
                ) {
                    glideImgCallback.onLoadedImg(FlickrImgResponse(BACKGROUND_IMG_MAP[galleryName], true))
                } else {
                    val photosFromGalleryParameter = FlickrGetPhotosFromGalleryParameter()
                    photosFromGalleryParameter.galleryId = FlickrUtil.getWeatherGalleryId(galleryName)
                    val restfulApiQuery = getApiService(RetrofitClient.ServiceType.FLICKR)
                    val call: Call<JsonElement> = restfulApiQuery.getPhotosFromGallery(photosFromGalleryParameter.map)
                    val imgRequestData = ImgRequestData()
                    IMG_REQUEST_OBJ_SET.add(imgRequestData)
                    imgRequestData.galleryCall = call
                    val finalTime: String = time
                    val finalWeather = weather
                    call.enqueue(object : Callback<JsonElement?> {
                        override fun onResponse(call: Call<JsonElement?>, photosFromGalleryResponse: Response<JsonElement?>) {
                            if (photosFromGalleryResponse.isSuccessful) {
                                val gson = Gson()
                                val photosFromGalleryResponseDto = gson.fromJson(
                                    photosFromGalleryResponse.body(),
                                    PhotosFromGalleryResponse::class.java
                                )
                                if (photosFromGalleryResponseDto.stat == "ok") {
                                    if (photosFromGalleryResponseDto.photos.total != "0") {
                                        // https://live.staticflickr.com/65535/50081787401_355bcec912_b.jpg
                                        // https://live.staticflickr.com/server/id_secret_size.jpg
                                        val randomIdx = Random().nextInt(photosFromGalleryResponseDto.photos.total.toInt())
                                        val photo = photosFromGalleryResponseDto.photos.photo[randomIdx]
                                        val getInfoParameter = FlickrGetInfoParameter()
                                        getInfoParameter.secret = photo.secret
                                        getInfoParameter.photoId = photo.id
                                        val restfulApiQuery = getApiService(RetrofitClient.ServiceType.FLICKR)
                                        val getPhotoInfoCall: Call<JsonElement> = restfulApiQuery.getGetInfo(getInfoParameter.map)
                                        imgRequestData.getPhotoInfoCall = getPhotoInfoCall
                                        getPhotoInfoCall.enqueue(object : Callback<JsonElement?> {
                                            override fun onResponse(call: Call<JsonElement?>, photoInfoResponse: Response<JsonElement?>) {
                                                if (photoInfoResponse.isSuccessful) {
                                                    val getInfoPhotoResponseDto = gson.fromJson(
                                                        photoInfoResponse.body(),
                                                        GetInfoPhotoResponse::class.java
                                                    )
                                                    val backgroundImgUrl =
                                                        ("https://live.staticflickr.com/" + photo.server + "/" + photo.id + "_"
                                                                + (if (getInfoPhotoResponseDto.photo.originalsecret == null) photo.secret else getInfoPhotoResponseDto.photo.originalsecret)
                                                                + if (getInfoPhotoResponseDto.photo.originalsecret == null) "_b.jpg" else "_o.jpg")
                                                    val flickrImgData = FlickrImgData()
                                                    BACKGROUND_IMG_MAP[galleryName] = flickrImgData
                                                    flickrImgData.photo = photo
                                                    flickrImgData.time = finalTime
                                                    flickrImgData.volume = flickrRequestParameter.precipitationVolume
                                                    flickrImgData.weather = finalWeather
                                                    imgRequestData.glideTarget = object : CustomTarget<Bitmap?>() {
                                                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                            val res = resource.copy(Bitmap.Config.ARGB_8888, true)
                                                            BACKGROUND_IMG_MAP[galleryName]!!.img = res
                                                            glideImgCallback.onLoadedImg(
                                                                FlickrImgResponse(
                                                                    BACKGROUND_IMG_MAP[galleryName],
                                                                    true
                                                                )
                                                            )
                                                        }

                                                        override fun onLoadCleared(placeholder: Drawable?) {
                                                            glideImgCallback.onLoadedImg(
                                                                FlickrImgResponse(
                                                                    BACKGROUND_IMG_MAP[galleryName],
                                                                    false
                                                                )
                                                            )
                                                        }

                                                        override fun onLoadFailed(errorDrawable: Drawable?) {
                                                            super.onLoadFailed(errorDrawable)
                                                            glideImgCallback.onLoadedImg(
                                                                FlickrImgResponse(
                                                                    BACKGROUND_IMG_MAP[galleryName],
                                                                    false
                                                                )
                                                            )
                                                        }
                                                    }
                                                    Glide.with(context!!).asBitmap().load(backgroundImgUrl).into(
                                                        imgRequestData.glideTarget!!
                                                    )
                                                } else {
                                                }
                                            }

                                            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                                                glideImgCallback.onLoadedImg(FlickrImgResponse(BACKGROUND_IMG_MAP[galleryName], false))
                                            }
                                        })
                                    } else {
                                        glideImgCallback.onLoadedImg(FlickrImgResponse(BACKGROUND_IMG_MAP[galleryName], false))
                                    }
                                } else {
                                    glideImgCallback.onLoadedImg(FlickrImgResponse(BACKGROUND_IMG_MAP[galleryName], false))
                                }
                            } else {
                                glideImgCallback.onLoadedImg(FlickrImgResponse(BACKGROUND_IMG_MAP[galleryName], false))
                            }
                        }

                        override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                            glideImgCallback.onLoadedImg(FlickrImgResponse(null, false))
                        }
                    })
                }
        }

    override fun cancelAllRequests() {

    }

    data class FlickrImgResponse(val flickrImgData: FlickrImgData?, val successful: Boolean)

    data class ImgRequestData(
        var galleryCall: Call<JsonElement>? = null,
        var getPhotoInfoCall: Call<JsonElement>? = null,
        var glideTarget: CustomTarget<Bitmap>? = null
    )

    data class FlickrRequestParameter(
        val weatherProviderType: WeatherProviderType,
        val weatherDescription: String,
        val latitude: Double,
        val longitude: Double,
        val zoneId: ZoneId,
        val precipitationVolume: String,
        val refreshDateTime: ZonedDateTime
    )

}