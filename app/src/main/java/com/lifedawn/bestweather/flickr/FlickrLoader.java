package com.lifedawn.bestweather.flickr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.GlideApp;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.flickr.FlickrGetPhotosFromGalleryParameter;
import com.lifedawn.bestweather.retrofit.responses.flickr.PhotosFromGalleryResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.FlickrUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlickrLoader {
	public static final Map<String, FlickrImgObj> BACKGROUND_IMG_MAP = new HashMap<>();

	private FlickrLoader() {
	}

	public static void loadImg(Activity activity, WeatherDataSourceType weatherDataSourceType, String val, Double latitude, Double longitude,
	                           ZoneId zoneId, String volume, GlideImgCallback glideImgCallback, ZonedDateTime refreshDateTime) {
		MyApplication.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				final ZonedDateTime lastRefreshDateTime = refreshDateTime.withZoneSameInstant(zoneId);

				SimpleTimeZone timeZone = new SimpleTimeZone(lastRefreshDateTime.getOffset().getTotalSeconds() * 1000, "");
				Calendar currentCalendar = Calendar.getInstance(timeZone);
				currentCalendar.set(lastRefreshDateTime.getYear(), lastRefreshDateTime.getMonthValue() - 1,
						lastRefreshDateTime.getDayOfMonth(), lastRefreshDateTime.getHour(), lastRefreshDateTime.getMinute(),
						lastRefreshDateTime.getSecond());

				SunriseSunsetCalculator sunRiseSunsetCalculator = new SunriseSunsetCalculator(
						new com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude), currentCalendar.getTimeZone());
				Calendar sunRiseCalendar = sunRiseSunsetCalculator.getOfficialSunriseCalendarForDate(currentCalendar);
				Calendar sunSetCalendar = sunRiseSunsetCalculator.getOfficialSunsetCalendarForDate(currentCalendar);

				if (sunRiseCalendar == null || sunSetCalendar == null) {
					glideImgCallback.onLoadedImg(null, null, false);
					return;
				}

				final long currentTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(currentCalendar.getTimeInMillis());
				final long sunRiseTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(sunRiseCalendar.getTimeInMillis());
				final long sunSetTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(sunSetCalendar.getTimeInMillis());

				String time = null;
				//현재 시각 파악 : 낮, 밤, 일출, 일몰(+-20분)
				if (currentTimeMinutes < sunRiseTimeMinutes - 2) {
					//새벽
					time = "night";
				} else if (currentTimeMinutes <= sunRiseTimeMinutes + 15) {
					//일출
					time = "sunrise";
				} else if (currentTimeMinutes > sunRiseTimeMinutes + 15 && currentTimeMinutes <= sunSetTimeMinutes - 15) {
					//낮
					time = "day";
				} else if (currentTimeMinutes < sunSetTimeMinutes + 2) {
					//일몰
					time = "sunset";
				} else {
					//밤
					time = "night";
				}

				String weather = null;
				switch (weatherDataSourceType) {
					case KMA_WEB:
						String pty = KmaResponseProcessor.convertPtyTextToCode(val);
						String sky = KmaResponseProcessor.convertSkyTextToCode(val);
						weather = (pty == null) ? KmaResponseProcessor.getSkyFlickrGalleryName(
								sky) : KmaResponseProcessor.getPtyFlickrGalleryName(pty);
						break;
					case KMA_API:
						String code = val.substring(0, 1);
						weather = val.contains("_sky") ? KmaResponseProcessor.getSkyFlickrGalleryName(
								code) : KmaResponseProcessor.getPtyFlickrGalleryName(code);
						break;
					case ACCU_WEATHER:
						weather = AccuWeatherResponseProcessor.getFlickrGalleryName(val);
						break;
					case OWM_ONECALL:
						weather = OpenWeatherMapResponseProcessor.getFlickrGalleryName(val);
						break;
				}

				final String galleryName = time + " " + weather;
				// time : sunrise, sunset, day, night
				// weather : clear, partly cloudy, mostly cloudy, overcast, rain, snow


				//이미 다운로드 된 이미지가 있으면 다운로드 하지 않음
				if (BACKGROUND_IMG_MAP.containsKey(galleryName)) {
					glideImgCallback.onLoadedImg(BACKGROUND_IMG_MAP.get(galleryName).getImg(), BACKGROUND_IMG_MAP.get(galleryName), true);
				} else {
					FlickrGetPhotosFromGalleryParameter photosFromGalleryParameter = new FlickrGetPhotosFromGalleryParameter();
					photosFromGalleryParameter.setGalleryId(FlickrUtil.getWeatherGalleryId(galleryName));

					Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.FLICKR);
					Call<JsonElement> call = querys.getPhotosFromGallery(photosFromGalleryParameter.getMap());
					String finalTime = time;
					String finalWeather = weather;

					call.enqueue(new Callback<JsonElement>() {
						@Override
						public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
							if (activity == null) {
								glideImgCallback.onLoadedImg(null, null, false);
								return;
							}

							Gson gson = new Gson();
							PhotosFromGalleryResponse photosFromGalleryResponse = gson.fromJson(response.body().toString(),
									PhotosFromGalleryResponse.class);

							if (photosFromGalleryResponse.getStat().equals("ok")) {
								if (!photosFromGalleryResponse.getPhotos().getTotal().equals("0")) {
									// https://live.staticflickr.com/65535/50081787401_355bcec912_b.jpg
									// https://live.staticflickr.com/server/id_secret_size.jpg
									int randomIdx = new Random().nextInt(Integer.parseInt(photosFromGalleryResponse.getPhotos().getTotal()));
									PhotosFromGalleryResponse.Photos.Photo photo = photosFromGalleryResponse.getPhotos().getPhoto().get(randomIdx);
									final String backgroundImgUrl = "https://live.staticflickr.com/" + photo.getServer() + "/" + photo.getId() + "_" + photo.getSecret() + "_b.jpg";

									final FlickrImgObj flickrImgObj = new FlickrImgObj();
									flickrImgObj.setPhoto(photo);
									flickrImgObj.setTime(finalTime);
									flickrImgObj.setVolume(volume);
									flickrImgObj.setWeather(finalWeather);
									BACKGROUND_IMG_MAP.put(galleryName, flickrImgObj);

									GlideApp.with(activity).asBitmap().load(backgroundImgUrl).into(new CustomTarget<Bitmap>() {
										@Override
										public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
											BACKGROUND_IMG_MAP.get(galleryName).setImg(resource);
											glideImgCallback.onLoadedImg(BACKGROUND_IMG_MAP.get(galleryName).getImg(), BACKGROUND_IMG_MAP.get(galleryName), true);
										}

										@Override
										public void onLoadCleared(@Nullable Drawable placeholder) {
											glideImgCallback.onLoadedImg(null, BACKGROUND_IMG_MAP.get(galleryName), false);
										}

										@Override
										public void onLoadFailed(@Nullable Drawable errorDrawable) {
											super.onLoadFailed(errorDrawable);

											glideImgCallback.onLoadedImg(null, BACKGROUND_IMG_MAP.get(galleryName), false);
										}
									});
								} else {
									glideImgCallback.onLoadedImg(null, BACKGROUND_IMG_MAP.get(galleryName), false);
								}
							} else {
								glideImgCallback.onLoadedImg(null, BACKGROUND_IMG_MAP.get(galleryName), false);
							}
						}

						@Override
						public void onFailure(Call<JsonElement> call, Throwable t) {
							glideImgCallback.onLoadedImg(null, null, false);
						}
					});
				}
			}
		});

	}

	public interface GlideImgCallback {
		void onLoadedImg(Bitmap bitmap, FlickrImgObj flickrImgObj, boolean successful);
	}
}
