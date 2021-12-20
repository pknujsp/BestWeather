package com.lifedawn.bestweather.weathers.dataprocessing.util;

public class WeatherUtil {
	private WeatherUtil() {
	}

	public static double calcFeelsLikeTemperature(double celsiusTemperature, double kmPerHWindSpeed, double humidity) {
		if (celsiusTemperature < 11.0) {
				/*
			- 겨울 체감온도 = 13.12 + 0.6215T - 11.37 V0.16 + 0.3965 V0.16T
			* T : 기온(°C), V : 풍속(km/h)
			 */
			if (kmPerHWindSpeed <= 4.68) {
				return celsiusTemperature;
			} else {
				return 13.12 + (0.6215 * celsiusTemperature) - (11.37 * Math.pow(kmPerHWindSpeed, 0.16)) + (0.3965 * Math.pow(kmPerHWindSpeed
						, 0.16) * celsiusTemperature);
			}
		} else {
			/*
			- 여름 체감온도 = -0.2442 + 0.55399Tw + 0.45535Ta – 0.0022Tw2 + 0.00278TwTa + 3.5
			* Tw = Ta * ATAN[0.151977(RH+8.313659)1/2] + ATAN(Ta+RH) - ATAN(RH-1.67633) + 0.00391838 * RH * 3/2 * ATAN(0.023101RH) - 4.686035
			** Ta : 기온(°C), Tw : 습구온도(Stull의 추정식** 이용), RH : 상대습도(%)
			 */
			final double tw =
					(celsiusTemperature * Math.atan(Math.abs(0.151977 * Math.pow(humidity + 8.313659, 0.5)))) + Math.atan(celsiusTemperature + humidity)
							- Math.atan(humidity - 1.67633) + (0.00391838 * Math.pow(humidity, 1.5) * Math.atan(0.023101 * humidity)) - 4.686035;

			return -0.2442 + (0.55399 * tw) + (0.45535 * celsiusTemperature) - (0.0022 * Math.pow(tw, 2)) + (0.00278 * tw * celsiusTemperature) + 3.5;
		}
	}
}
