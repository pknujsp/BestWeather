package com.lifedawn.bestweather.flickr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.client.Queries;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.flickr.FlickrGetInfoParameter;
import com.lifedawn.bestweather.retrofit.parameters.flickr.FlickrGetPhotosFromGalleryParameter;
import com.lifedawn.bestweather.retrofit.responses.flickr.GetInfoPhotoResponse;
import com.lifedawn.bestweather.retrofit.responses.flickr.PhotosFromGalleryResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.FlickrUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlickrLoader {
	private static final Map<String, FlickrImgObj> BACKGROUND_IMG_MAP = new HashMap<>();
	private static final Set<ImgRequestObj> IMG_REQUEST_OBJ_SET = new HashSet<>();
	private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

	private FlickrLoader() {
	}

	public static void loadImg(Activity activity, WeatherProviderType weatherProviderType, String val, Double latitude, Double longitude,
	                           ZoneId zoneId, String volume, GlideImgCallback glideImgCallback, ZonedDateTime refreshDateTime) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				cancelAllRequest(activity);
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
					glideImgCallback.onLoadedImg(null, false);
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
				switch (weatherProviderType) {
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
				Log.e("flickrGalleryName", galleryName);

				//이미 다운로드 된 이미지가 있으면 다운로드 하지 않음
				if (BACKGROUND_IMG_MAP.containsKey(galleryName) && BACKGROUND_IMG_MAP.get(galleryName).getImg() != null) {
					glideImgCallback.onLoadedImg(BACKGROUND_IMG_MAP.get(galleryName), true);
				} else {
					FlickrGetPhotosFromGalleryParameter photosFromGalleryParameter = new FlickrGetPhotosFromGalleryParameter();
					photosFromGalleryParameter.setGalleryId(FlickrUtil.getWeatherGalleryId(galleryName));

					Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.FLICKR);
					Call<JsonElement> call = queries.getPhotosFromGallery(photosFromGalleryParameter.getMap());

					ImgRequestObj imgRequestObj = new ImgRequestObj();
					IMG_REQUEST_OBJ_SET.add(imgRequestObj);
					imgRequestObj.galleryCall = call;

					final String finalTime = time;
					final String finalWeather = weather;

					call.enqueue(new Callback<JsonElement>() {
						@Override
						public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
							Gson gson = new Gson();
							final PhotosFromGalleryResponse photosFromGalleryResponse = gson.fromJson(response.body().toString(),
									PhotosFromGalleryResponse.class);

							if (photosFromGalleryResponse.getStat().equals("ok")) {
								if (!photosFromGalleryResponse.getPhotos().getTotal().equals("0")) {
									// https://live.staticflickr.com/65535/50081787401_355bcec912_b.jpg
									// https://live.staticflickr.com/server/id_secret_size.jpg
									final int randomIdx =
											new Random().nextInt(Integer.parseInt(photosFromGalleryResponse.getPhotos().getTotal()));
									final PhotosFromGalleryResponse.Photos.Photo photo =
											photosFromGalleryResponse.getPhotos().getPhoto().get(randomIdx);

									final FlickrGetInfoParameter getInfoParameter = new FlickrGetInfoParameter();
									getInfoParameter.setSecret(photo.getSecret());
									getInfoParameter.setPhotoId(photo.getId());

									Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.FLICKR);
									Call<JsonElement> getPhotoInfoCall = queries.getGetInfo(getInfoParameter.getMap());
									imgRequestObj.getPhotoInfoCall = getPhotoInfoCall;

									getPhotoInfoCall.enqueue(new Callback<JsonElement>() {
										@Override
										public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
											final GetInfoPhotoResponse getInfoPhotoResponse = gson.fromJson(response.body().toString(),
													GetInfoPhotoResponse.class);

											final String backgroundImgUrl =
													"https://live.staticflickr.com/" + photo.getServer() + "/" + photo.getId() + "_"
															+ (getInfoPhotoResponse.getPhoto().getOriginalsecret() == null ?
															photo.getSecret() : getInfoPhotoResponse.getPhoto().getOriginalsecret())
															+ (getInfoPhotoResponse.getPhoto().getOriginalsecret() == null ?
															"_b.jpg" : "_o.jpg");

											final FlickrImgObj flickrImgObj = new FlickrImgObj();
											BACKGROUND_IMG_MAP.put(galleryName, flickrImgObj);

											flickrImgObj.setPhoto(photo);
											flickrImgObj.setTime(finalTime);
											flickrImgObj.setVolume(volume);
											flickrImgObj.setWeather(finalWeather);

											imgRequestObj.glideTarget = new CustomTarget<Bitmap>() {
												@Override
												public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
													Bitmap res = resource.copy(Bitmap.Config.RGB_565, true);
													BACKGROUND_IMG_MAP.get(galleryName).setImg(res);
													glideImgCallback.onLoadedImg(BACKGROUND_IMG_MAP.get(galleryName), true);
												}

												@Override
												public void onLoadCleared(@Nullable Drawable placeholder) {
												}

												@Override
												public void onLoadFailed(@Nullable Drawable errorDrawable) {
													super.onLoadFailed(errorDrawable);
													glideImgCallback.onLoadedImg(BACKGROUND_IMG_MAP.get(galleryName), false);
												}
											};

											Glide.with(activity).asBitmap().load(backgroundImgUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgRequestObj.glideTarget);
										}

										@Override
										public void onFailure(Call<JsonElement> call, Throwable t) {
											glideImgCallback.onLoadedImg(BACKGROUND_IMG_MAP.get(galleryName), false);
										}
									});


								} else {
									glideImgCallback.onLoadedImg(BACKGROUND_IMG_MAP.get(galleryName), false);
								}
							} else {
								glideImgCallback.onLoadedImg(BACKGROUND_IMG_MAP.get(galleryName), false);
							}

						}

						@Override
						public void onFailure(Call<JsonElement> call, Throwable t) {
							glideImgCallback.onLoadedImg(null, false);
						}
					});
				}
			}
		});

	}

	public static void cancelAllRequest(Activity activity) {
		Glide.with(activity).pauseAllRequests();

		for (ImgRequestObj imgRequestObj : IMG_REQUEST_OBJ_SET) {
			if (imgRequestObj.glideTarget != null) {
				Glide.with(activity).clear(imgRequestObj.glideTarget);
			} else if (imgRequestObj.getPhotoInfoCall != null) {
				imgRequestObj.getPhotoInfoCall.cancel();
			} else if (imgRequestObj.galleryCall != null) {
				imgRequestObj.galleryCall.cancel();
			}
		}

		IMG_REQUEST_OBJ_SET.clear();
	}

	private static Bitmap setBrightness(Bitmap src, int value) {
		// original image size
		int width = src.getWidth();
		int height = src.getHeight();
		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
		// color information
		int A, R, G, B;
		int pixel;

		// scan through all pixels
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				// get pixel color
				pixel = src.getPixel(x, y);
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);

				// increase/decrease each channel
				R += value;
				if (R > 255) {
					R = 255;
				} else if (R < 0) {
					R = 0;
				}

				G += value;
				if (G > 255) {
					G = 255;
				} else if (G < 0) {
					G = 0;
				}

				B += value;
				if (B > 255) {
					B = 255;
				} else if (B < 0) {
					B = 0;
				}

				// apply new pixel color to output bitmap
				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}

		// return final image
		return bmOut;
	}

	public interface GlideImgCallback {
		void onLoadedImg(FlickrImgObj flickrImgObj, boolean successful);
	}

	private static class ImgRequestObj {
		Call<JsonElement> galleryCall;
		Call<JsonElement> getPhotoInfoCall;
		CustomTarget<Bitmap> glideTarget;
	}
}
