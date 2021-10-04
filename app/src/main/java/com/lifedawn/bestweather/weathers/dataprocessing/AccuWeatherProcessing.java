package com.lifedawn.bestweather.weathers.dataprocessing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.CurrentConditionsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.FiveDaysOfDailyForecastsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.GeoPositionSearchParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.TwelveHoursOfHourlyForecastsParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidLandParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidTaParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse.MidLandFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaRoot;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.retrofit.util.RetrofitCallListManager;
import com.lifedawn.bestweather.weathers.dataprocessing.callback.KmaVilageFcstCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccuWeatherProcessing {
	/**
	 * Current Conditions
	 */
	public Call<JsonObject> getCurrentConditions(CurrentConditionsParameter, JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
		//basetime설정
		if (calendar.get(Calendar.MINUTE) < 40) {
			calendar.add(Calendar.HOUR_OF_DAY, -1);
		}
		
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd", Locale.US);
		SimpleDateFormat HH = new SimpleDateFormat("HH", Locale.US);
		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "00");
		
		Call<JsonObject> call = querys.getUltraSrtNcst(parameter.getMap());
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
			}
		});
		
		return call;
	}
	
	/**
	 * 5 Days Of Daily Forecast
	 */
	public Call<JsonObject> get5DaysOfDailyForecast(FiveDaysOfDailyForecastsParameter fiveDaysOfDailyForecastsParameter,
			JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ULTRA_SRT_FCST);
		//basetime설정
		if (calendar.get(Calendar.MINUTE) < 45) {
			calendar.add(Calendar.HOUR_OF_DAY, -1);
		}
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd", Locale.US);
		SimpleDateFormat HH = new SimpleDateFormat("HH", Locale.US);
		
		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "30");
		
		Call<JsonObject> call = Objects.requireNonNull(querys).getUltraSrtFcst(parameter.getMap());
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
			}
		});
		return call;
	}
	
	/**
	 * 12 Hours of Hourly Forecasts
	 */
	public Call<JsonObject> get12HoursOfHourlyForecasts(TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter,
			JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.VILAGE_FCST);
		//basetime설정
		final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		final int currentMinute = calendar.get(Calendar.MINUTE);
		int i = currentHour >= 0 && currentHour <= 2 ? 7 : currentHour / 3 - 1;
		int baseHour = 0;
		
		if (currentMinute > 10 && (currentHour - 2) % 3 == 0) {
			// ex)1411인 경우
			baseHour = 3 * ((currentHour - 2) / 3) + 2;
			i = 0;
		} else {
			baseHour = 3 * i + 2;
		}
		
		if (i == 7) {
			calendar.add(Calendar.DAY_OF_YEAR, -1);
			calendar.set(Calendar.HOUR_OF_DAY, 23);
		} else {
			calendar.set(Calendar.HOUR_OF_DAY, baseHour);
		}
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd", Locale.US);
		SimpleDateFormat HH = new SimpleDateFormat("HH", Locale.US);
		
		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "00");
		
		Call<JsonObject> call = Objects.requireNonNull(querys).getVilageFcst(parameter.getMap());
		
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
			}
		});
		return call;
	}
	
	
	/**
	 * GeoPosition Search
	 */
	public Call<JsonObject> getGeoPositionSearch(GeoPositionSearchParameter geoPositionSearchParameter, JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MID_LAND_FCST);
		
		Call<JsonObject> call = Objects.requireNonNull(querys).getMidLandFcst(parameter.getMap());
		
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
			}
		});
		return call;
	}
	
	
	public void getKmaForecasts(UltraSrtNcstParameter ultraSrtNcstParameter, UltraSrtFcstParameter ultraSrtFcstParameter,
			VilageFcstParameter vilageFcstParameter, MidLandParameter midLandParameter, MidTaParameter midTaParameter, Calendar calendar,
			MultipleJsonDownloader multipleJsonDownloader) {
		Call<JsonObject> ultraSrtNcstCall = getUltraSrtNcstData(ultraSrtNcstParameter, (Calendar) calendar.clone(),
				new KmaVilageFcstCallback() {
					@Override
					public void onResponseSuccessful(Response<JsonObject> response) {
						multipleJsonDownloader.processResult(response);
					}
					
					@Override
					public void onResponseFailed(Exception e) {
						multipleJsonDownloader.processResult(e);
					}
				});
		
		
		RetrofitCallListManager.CallObj newCallObj = RetrofitCallListManager.newCalls();
		newCallObj.add(ultraSrtNcstCall);
		newCallObj.add(ultraSrtFcstCall);
		newCallObj.add(vilageFcstCall);
		newCallObj.add(midTaFcstCall);
		newCallObj.add(midLandFcstCall);
	}
}
