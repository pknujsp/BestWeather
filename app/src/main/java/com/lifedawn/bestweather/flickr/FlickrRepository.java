package com.lifedawn.bestweather.flickr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.client.Queries;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.flickr.FlickrGetInfoParameter;
import com.lifedawn.bestweather.retrofit.parameters.flickr.FlickrGetPhotosFromGalleryParameter;
import com.lifedawn.bestweather.retrofit.responses.flickr.GetInfoPhotoResponse;
import com.lifedawn.bestweather.retrofit.responses.flickr.PhotosFromGalleryResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.FlickrUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.MetNorwayResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class FlickrRepository {
	private static FlickrRepository INSTANCE;
	private final ExecutorService executorService = Executors.newFixedThreadPool(4);
	private final List<ImgRequestData> IMG_REQUEST_OBJ_SET = new CopyOnWriteArrayList<>();
	private final Map<String, FlickrImgData> BACKGROUND_IMG_MAP = new ConcurrentHashMap<>();

	public static void initialize() {
		if (INSTANCE == null) {
			INSTANCE = new FlickrRepository();
			FlickrUtil.init();
		}
	}

	public static FlickrRepository getINSTANCE() {
		return INSTANCE;
	}

	private FlickrRepository() {

	}

	public void loadImg(Context context, FlickrRequestParameter flickrRequestParameter, GlideImgCallback glideImgCallback) {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				cancelAllRequests(context);

				final ZonedDateTime lastRefreshDateTime =
						flickrRequestParameter.refreshDateTime.withZoneSameInstant(flickrRequestParameter.zoneId);

				SimpleTimeZone timeZone = new SimpleTimeZone(lastRefreshDateTime.getOffset().getTotalSeconds() * 1000, "");
				Calendar currentCalendar = Calendar.getInstance(timeZone);
				currentCalendar.set(lastRefreshDateTime.getYear(), lastRefreshDateTime.getMonthValue() - 1,
						lastRefreshDateTime.getDayOfMonth(), lastRefreshDateTime.getHour(), lastRefreshDateTime.getMinute(),
						lastRefreshDateTime.getSecond());

				SunriseSunsetCalculator sunRiseSunsetCalculator = new SunriseSunsetCalculator(
						new com.luckycatlabs.sunrisesunset.dto.Location(flickrRequestParameter.latitude, flickrRequestParameter.longitude),
						currentCalendar.getTimeZone());
				Calendar sunRiseCalendar = sunRiseSunsetCalculator.getOfficialSunriseCalendarForDate(currentCalendar);
				Calendar sunSetCalendar = sunRiseSunsetCalculator.getOfficialSunsetCalendarForDate(currentCalendar);

				if (sunRiseCalendar == null || sunSetCalendar == null) {
					glideImgCallback.onLoadedImg(new FlickrImgResponse(null, false));
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
				switch (flickrRequestParameter.weatherProviderType) {
					case KMA_WEB:
						String pty = KmaResponseProcessor.convertPtyTextToCode(flickrRequestParameter.weatherDescription);
						String sky = KmaResponseProcessor.convertSkyTextToCode(flickrRequestParameter.weatherDescription);
						weather = (pty == null) ? KmaResponseProcessor.getSkyFlickrGalleryName(
								sky) : KmaResponseProcessor.getPtyFlickrGalleryName(pty);
						break;
					case KMA_API:
						String code = flickrRequestParameter.weatherDescription.substring(0, 1);
						weather = flickrRequestParameter.weatherDescription.contains("_sky") ? KmaResponseProcessor.getSkyFlickrGalleryName(
								code) : KmaResponseProcessor.getPtyFlickrGalleryName(code);
						break;
					case ACCU_WEATHER:
						weather = AccuWeatherResponseProcessor.getFlickrGalleryName(flickrRequestParameter.weatherDescription);
						break;
					case OWM_ONECALL:
						weather = OpenWeatherMapResponseProcessor.getFlickrGalleryName(flickrRequestParameter.weatherDescription);
						break;
					case MET_NORWAY:
						weather = MetNorwayResponseProcessor.getFlickrGalleryName(flickrRequestParameter.weatherDescription);
						break;
				}

				final String galleryName = time + " " + weather;
				// time : sunrise, sunset, day, night
				// weather : clear, partly cloudy, mostly cloudy, overcast, rain, snow

				//이미 다운로드 된 이미지가 있으면 다운로드 하지 않음
				if (BACKGROUND_IMG_MAP.containsKey(galleryName) && BACKGROUND_IMG_MAP.get(galleryName).getImg() != null) {
					glideImgCallback.onLoadedImg(new FlickrImgResponse(BACKGROUND_IMG_MAP.get(galleryName), true));

				} else {
					FlickrGetPhotosFromGalleryParameter photosFromGalleryParameter = new FlickrGetPhotosFromGalleryParameter();
					photosFromGalleryParameter.setGalleryId(FlickrUtil.getWeatherGalleryId(galleryName));

					Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.FLICKR);
					Call<JsonElement> call = queries.getPhotosFromGallery(photosFromGalleryParameter.getMap());

					final ImgRequestData imgRequestData = new ImgRequestData();
					IMG_REQUEST_OBJ_SET.add(imgRequestData);
					imgRequestData.galleryCall = call;

					final String finalTime = time;
					final String finalWeather = weather;

					call.enqueue(new Callback<JsonElement>() {
						@Override
						public void onResponse(Call<JsonElement> call, Response<JsonElement> photosFromGalleryResponse) {
							if (photosFromGalleryResponse.isSuccessful()) {
								final Gson gson = new Gson();
								final PhotosFromGalleryResponse photosFromGalleryResponseDto = gson.fromJson(photosFromGalleryResponse.body(),
										PhotosFromGalleryResponse.class);

								if (photosFromGalleryResponseDto.getStat().equals("ok")) {
									if (!photosFromGalleryResponseDto.getPhotos().getTotal().equals("0")) {
										// https://live.staticflickr.com/65535/50081787401_355bcec912_b.jpg
										// https://live.staticflickr.com/server/id_secret_size.jpg
										final int randomIdx =
												new Random().nextInt(Integer.parseInt(photosFromGalleryResponseDto.getPhotos().getTotal()));
										final PhotosFromGalleryResponse.Photos.Photo photo =
												photosFromGalleryResponseDto.getPhotos().getPhoto().get(randomIdx);

										final FlickrGetInfoParameter getInfoParameter = new FlickrGetInfoParameter();
										getInfoParameter.setSecret(photo.getSecret());
										getInfoParameter.setPhotoId(photo.getId());

										Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.FLICKR);
										Call<JsonElement> getPhotoInfoCall = queries.getGetInfo(getInfoParameter.getMap());
										imgRequestData.getPhotoInfoCall = getPhotoInfoCall;

										getPhotoInfoCall.enqueue(new Callback<JsonElement>() {
											@Override
											public void onResponse(Call<JsonElement> call, Response<JsonElement> photoInfoResponse) {
												if (photoInfoResponse.isSuccessful()) {
													final GetInfoPhotoResponse getInfoPhotoResponseDto = gson.fromJson(photoInfoResponse.body(),
															GetInfoPhotoResponse.class);

													final String backgroundImgUrl =
															"https://live.staticflickr.com/" + photo.getServer() + "/" + photo.getId() + "_"
																	+ (getInfoPhotoResponseDto.getPhoto().getOriginalsecret() == null ?
																	photo.getSecret() : getInfoPhotoResponseDto.getPhoto().getOriginalsecret())
																	+ (getInfoPhotoResponseDto.getPhoto().getOriginalsecret() == null ?
																	"_b.jpg" : "_o.jpg");

													final FlickrImgData flickrImgData = new FlickrImgData();
													BACKGROUND_IMG_MAP.put(galleryName, flickrImgData);

													flickrImgData.setPhoto(photo);
													flickrImgData.setTime(finalTime);
													flickrImgData.setVolume(flickrRequestParameter.precipitationVolume);
													flickrImgData.setWeather(finalWeather);

													imgRequestData.glideTarget = new CustomTarget<Bitmap>() {
														@Override
														public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
															Bitmap res = resource.copy(Bitmap.Config.ARGB_8888, true);
															BACKGROUND_IMG_MAP.get(galleryName).setImg(res);
															glideImgCallback.onLoadedImg(new FlickrImgResponse(BACKGROUND_IMG_MAP.get(galleryName), true));
														}

														@Override
														public void onLoadCleared(@Nullable Drawable placeholder) {
															glideImgCallback.onLoadedImg(new FlickrImgResponse(BACKGROUND_IMG_MAP.get(galleryName), false));
														}

														@Override
														public void onLoadFailed(@Nullable Drawable errorDrawable) {
															super.onLoadFailed(errorDrawable);
															glideImgCallback.onLoadedImg(new FlickrImgResponse(BACKGROUND_IMG_MAP.get(galleryName), false));

														}
													};

													Glide.with(context).asBitmap().load(backgroundImgUrl)
															.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(imgRequestData.glideTarget);

												} else {
												}
											}

											@Override
											public void onFailure(Call<JsonElement> call, Throwable t) {
												glideImgCallback.onLoadedImg(new FlickrImgResponse(BACKGROUND_IMG_MAP.get(galleryName), false));
											}
										});

									} else {
										glideImgCallback.onLoadedImg(new FlickrImgResponse(BACKGROUND_IMG_MAP.get(galleryName), false));

									}
								} else {
									glideImgCallback.onLoadedImg(new FlickrImgResponse(BACKGROUND_IMG_MAP.get(galleryName), false));
								}
							} else {
								glideImgCallback.onLoadedImg(new FlickrImgResponse(BACKGROUND_IMG_MAP.get(galleryName), false));
							}
						}

						@Override
						public void onFailure(Call<JsonElement> call, Throwable t) {
							glideImgCallback.onLoadedImg(new FlickrImgResponse(null, false));
						}
					});
				}
			}
		});

	}

	public void cancelAllRequests(Context context) {
		for (ImgRequestData imgRequestData : IMG_REQUEST_OBJ_SET) {
			if (imgRequestData.glideTarget != null) {
				if (imgRequestData.glideTarget.getRequest().isRunning())
					Glide.with(context).clear(imgRequestData.glideTarget);
			} else if (imgRequestData.getPhotoInfoCall != null) {
				imgRequestData.getPhotoInfoCall.cancel();
			} else if (imgRequestData.galleryCall != null) {
				imgRequestData.galleryCall.cancel();
			}
		}

		IMG_REQUEST_OBJ_SET.clear();
	}


	public interface GlideImgCallback {
		void onLoadedImg(FlickrImgResponse flickrImgResponse);
	}

	public static class FlickrImgResponse {
		public final FlickrImgData flickrImgData;
		public final boolean successful;

		public FlickrImgResponse(FlickrImgData flickrImgData, boolean successful) {
			this.flickrImgData = flickrImgData;
			this.successful = successful;
		}
	}

	public static class ImgRequestData {
		Call<JsonElement> galleryCall;
		Call<JsonElement> getPhotoInfoCall;
		CustomTarget<Bitmap> glideTarget;
	}

	public static class FlickrRequestParameter {
		public final WeatherProviderType weatherProviderType;
		public final String weatherDescription;
		public final Double latitude;
		public final Double longitude;
		public final ZoneId zoneId;
		public final String precipitationVolume;
		public final ZonedDateTime refreshDateTime;

		public FlickrRequestParameter(WeatherProviderType weatherProviderType, String weatherDescription, Double latitude, Double longitude, ZoneId zoneId, String precipitationVolume, ZonedDateTime refreshDateTime) {
			this.weatherProviderType = weatherProviderType;
			this.weatherDescription = weatherDescription;
			this.latitude = latitude;
			this.longitude = longitude;
			this.zoneId = zoneId;
			this.precipitationVolume = precipitationVolume;
			this.refreshDateTime = refreshDateTime;
		}
	}
}
