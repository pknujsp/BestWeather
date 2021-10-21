package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.CurrentConditionsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.FiveDaysOfDailyForecastsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.GeoPositionSearchParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.TwelveHoursOfHourlyForecastsParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.geopositionsearch.GeoPositionResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccuWeatherProcessing {
	/**
	 * Current Conditions
	 */
	public static Call<JsonElement> getCurrentConditions(CurrentConditionsParameter currentConditionsParameter,
			JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);
		
		Call<JsonElement> call = querys.getCurrentConditions(currentConditionsParameter.getLocationKey(),
				currentConditionsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		return call;
	}
	
	/**
	 * 5 Days Of Daily Forecast
	 */
	public static Call<JsonElement> get5DaysOfDailyForecasts(FiveDaysOfDailyForecastsParameter fiveDaysOfDailyForecastsParameter,
			JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);
		
		Call<JsonElement> call = querys.get5Days(fiveDaysOfDailyForecastsParameter.getLocationKey(),
				fiveDaysOfDailyForecastsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		
		return call;
	}
	
	/**
	 * 12 Hours of Hourly Forecasts
	 */
	public static Call<JsonElement> get12HoursOfHourlyForecasts(TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter,
			JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_12_HOURLY);
		
		Call<JsonElement> call = querys.get12Hourly(twelveHoursOfHourlyForecastsParameter.getLocationKey(),
				twelveHoursOfHourlyForecastsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		return call;
	}
	
	
	/**
	 * GeoPosition Search
	 */
	public static Call<JsonElement> getGeoPositionSearch(GeoPositionSearchParameter geoPositionSearchParameter,
			JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH);
		
		Call<JsonElement> call = querys.geoPositionSearch(geoPositionSearchParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (response.body() != null) {
					callback.onResponseResult(response);
				} else {
					callback.onResponseResult(new Exception());
				}
			}
			
			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		
		return call;
	}
	
	
	public static void getAccuWeatherForecasts(String latitude, String longitude, @Nullable String locationKey,
			MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		GeoPositionSearchParameter geoPositionSearchParameter = new GeoPositionSearchParameter();
		CurrentConditionsParameter currentConditionsParameter = new CurrentConditionsParameter();
		FiveDaysOfDailyForecastsParameter fiveDaysOfDailyForecastsParameter = new FiveDaysOfDailyForecastsParameter();
		TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter = new TwelveHoursOfHourlyForecastsParameter();
		
		if (locationKey == null) {
			geoPositionSearchParameter.setLatitude(latitude).setLongitude(longitude);
			Call<JsonElement> geoPositionSearchCall = getGeoPositionSearch(geoPositionSearchParameter, new JsonDownloader<JsonElement>() {
				
				@Override
				public void onResponseResult(Response<? extends JsonElement> response) {
					Log.e(RetrofitClient.LOG_TAG, "accu weather geoposition search 성공");
					multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
							RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, response);
					
					Gson gson = new Gson();
					GeoPositionResponse geoPositionResponse = gson.fromJson(response.body().toString(), GeoPositionResponse.class);
					
					final String locationKey = geoPositionResponse.getKey();
					currentConditionsParameter.setLocationKey(locationKey);
					fiveDaysOfDailyForecastsParameter.setLocationKey(locationKey);
					twelveHoursOfHourlyForecastsParameter.setLocationKey(locationKey);
					
					Call<JsonElement> currentConditionsCall = getCurrentConditions(currentConditionsParameter,
							new JsonDownloader<JsonElement>() {
								@Override
								public void onResponseResult(Response<? extends JsonElement> response) {
									Log.e(RetrofitClient.LOG_TAG, "accu weather current conditions 성공");
									multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
											RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, response);
								}
								
								@Override
								public void onResponseResult(Throwable t) {
									Log.e(RetrofitClient.LOG_TAG, "accu weather current conditions 실패");
									multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
											RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
								}
								
								
							});
					
					Call<JsonElement> fiveDaysOfDailyForecastsCall = get5DaysOfDailyForecasts(fiveDaysOfDailyForecastsParameter,
							new JsonDownloader<JsonElement>() {
								@Override
								public void onResponseResult(Response<? extends JsonElement> response) {
									Log.e(RetrofitClient.LOG_TAG, "accu weather daily forecast 성공");
									multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
											RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, response);
								}
								
								@Override
								public void onResponseResult(Throwable t) {
									Log.e(RetrofitClient.LOG_TAG, "accu weather daily forecast 실패");
									multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
											RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
									
								}
								
							});
					
					Call<JsonElement> twelveHoursOfHourlyForecastsCall = get12HoursOfHourlyForecasts(twelveHoursOfHourlyForecastsParameter,
							new JsonDownloader<JsonElement>() {
								@Override
								public void onResponseResult(Response<? extends JsonElement> response) {
									Log.e(RetrofitClient.LOG_TAG, "accu weather hourly forecast 성공");
									multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
											RetrofitClient.ServiceType.ACCU_12_HOURLY, response);
									
								}
								
								@Override
								public void onResponseResult(Throwable t) {
									Log.e(RetrofitClient.LOG_TAG, "accu weather hourly forecast 실패");
									multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
											RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
								}
								
								
							});
					
				}
				
				@Override
				public void onResponseResult(Throwable t) {
					Log.e(RetrofitClient.LOG_TAG, "accu weather geocoding search 실패");
					multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
							RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, t);
					multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
							RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
					multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
							RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
					multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
							RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
				}
				
				
			});
			
		}
	}
	
}
