package com.lifedawn.bestweather.weathers.dataprocessing.response.parser;

import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaCurrentConditions;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaDailyForecast;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KmaWebParser {
	private KmaWebParser() {
	}

	public static KmaCurrentConditions parseCurrentConditions(Document document, String baseDateTime) {
		//기온(5.8) tmp, 체감(체감 5.8℃) chill, 어제와 기온이 같아요 w-txt, 습도(40) lbl ic-hm val
		//바람(북서 1.1) lbl ic-wind val, 1시간 강수량(-) lbl rn-hr1 ic-rn val
		//발효중인 특보 cmp-impact-fct
		final Elements rootElements = document.getElementsByClass("cmp-cur-weather");
		final Elements wrap1 = rootElements.select("ul.wrap-1");
		final Elements wrap2 = rootElements.select("ul.wrap-2.no-underline");
		final Elements wIconwTemp = wrap1.select(".w-icon.w-temp");
		final Element li = wIconwTemp.get(0);
		final Elements spans = li.getElementsByTag("span");
		String pty = spans.get(1).text();
		//4.8℃ 최저-최고-
		String temp = spans.get(3).textNodes().get(0).text().replace(" ", "");

		//1일전 기온
		String yesterdayTemp = wrap1.select("li.w-txt").text().replace(" ", "");
		if (yesterdayTemp.contains("℃")) {
			String t = yesterdayTemp.replace("어제보다", "").replace("높아요", "")
					.replace("낮아요", "").replace("℃", "");
			Double currentTempVal = Double.parseDouble(temp);
			Double yesterdayTempVal = Double.parseDouble(t);
			if (yesterdayTemp.contains("높아요")) {
				yesterdayTempVal = currentTempVal - yesterdayTempVal;
			} else if (yesterdayTemp.contains("낮아요")) {
				yesterdayTempVal = currentTempVal + yesterdayTempVal;
			}
			yesterdayTemp = yesterdayTempVal.toString();
		} else {
			yesterdayTemp = temp;
		}

		//체감(4.8℃)
		String chill = spans.select(".chill").text();
		chill = chill.substring(3, chill.length() - 2).replace(" ", "");

		// 43 % 동 1.1 m/s - mm
		Elements spans2 = wrap2.select("span.val");
		String humidity = spans2.get(0).text().replace(" ", "");
		String windDirection = null;
		String windSpeed = null;
		String wind = spans2.get(1).text();

		if (!wind.equals("-")) {
			String[] spWind = wind.split(" ");
			windDirection = spWind[0].replace(" ", "");
			windSpeed = spWind[1].replace(" ", "");
		}

		String precipitationVolume = spans2.get(2).text().replace(" ", "");
		if (precipitationVolume.contains("-")) {
			precipitationVolume = "0.0mm";
		}

		KmaCurrentConditions kmaCurrentConditions = new KmaCurrentConditions();
		kmaCurrentConditions.setTemp(temp).setFeelsLikeTemp(chill).setHumidity(humidity).setPty(pty)
				.setWindDirection(windDirection).setWindSpeed(windSpeed).setPrecipitationVolume(precipitationVolume)
				.setBaseDateTime(baseDateTime).setYesterdayTemp(yesterdayTemp);
		return kmaCurrentConditions;
	}

	public static List<KmaHourlyForecast> parseHourlyForecasts(Document document) {
		final Elements elements = document.getElementsByClass("slide-wrap");
		//오늘, 내일, 모레, 글피, 그글피
		final Elements slides = elements.select("div.slide");

		List<KmaHourlyForecast> kmaHourlyForecasts = new ArrayList<>();

		ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		LocalDate localDate = null;
		LocalTime localTime = null;
		String date = null;
		String time = null;
		String weatherDescription = null;
		String temp = null;
		String feelsLikeTemp = null;
		String pop = null;
		String windDirection = null;
		String windSpeed = null;
		String humidity = null;
		boolean hasShower = false;

		final String hour24 = "24:00";
		final String degree = "℃";
		final String mm = "mm";
		final String cm = "cm";
		final String lessThan1mm = "~1mm";
		final String lessThan1cm = "~1cm";
		final String rainDrop = "빗방울";
		final String snowBlizzard = "눈날림";

		for (Element slide : slides) {
			Elements uls = slide.getElementsByClass("item-wrap").select("ul");

			if (slide.hasClass("slide day-ten")) {
				break;
			}

			for (Element ul : uls) {
				KmaHourlyForecast kmaHourlyForecast = new KmaHourlyForecast();

				Elements lis = ul.getElementsByTag("li");
				date = ul.attr("data-date");
				localDate = LocalDate.parse(date);

				time = ul.attr("data-time");
				if (time.equals(hour24)) {
					time = "00:00";
					localDate = localDate.plusDays(1);
				}
				localTime = LocalTime.parse(time);
				localTime = localTime.withMinute(0).withSecond(0).withNano(0);
				zonedDateTime = ZonedDateTime.of(localDate, localTime, zonedDateTime.getZone());

				if (ul.hasAttr("data-sonagi")) {
					hasShower = ul.attr("data-sonagi").equals("1");
				}
				weatherDescription = lis.get(1).getElementsByTag("span").get(1).text();
				temp = lis.get(2).getElementsByTag("span").get(1).childNode(0).toString().replace(degree, "");
				feelsLikeTemp = lis.get(3).getElementsByTag("span").get(1).text().replace(degree, "");
                /*
                강우+강설
                <li class="pcp snow-exists">
                <span class="hid">강수량: </span>
                <span>~1<span class="unit">mm</span><br/>~1<span class="unit">cm</span></span>  ~1mm~1cm
                </li>

                강수(현재 시간대에는 강설이 없으나, 다른 시간대에 강설이 있는 경우)
                <li class="pcp snow-exists">
                <span class="hid">강수량: </span>
                <span>~1<span class="unit">mm</span><br/>-</span>   ~1mm-
                </li>

                강수
                <li class="pcp ">
                <span class="hid">강수량: </span>
                <span>~1<span class="unit">mm</span></span>     ~1mm
                </li>

                눈날림
                <li class="pcp vs-txt-rn">
                <span class="hid">강수량: </span>
                <span>눈날림<span class="unit">mm</span></span>     눈날림mm
                </li>

                빗방울+눈날림
                <li class="pcp vs-txt-rn">
                <span class="hid">강수량: </span>
                <span>빗방울<br>눈날림<span class="unit">mm</span></span>    빗방울눈날림mm
                </li>

                강수없음
                <li class="pcp snow-exists">
                <span class="hid">강수량: </span>
                <span>-<br/>-</span>
                </li>

                <li class="pcp ">
                <span class="hid">강수량: </span>
                <span>-</span>
                </li>

                <li class="pcp">
                <span class="hid">강수량: </span>
                <span>1시간 단위 강수량(적설포함)은 모레까지 제공합니다.</span>
                </li>


                ~1mm~1cm
                5mm~1cm
                15mm15cm
                ~1mm-
                -~1cm
                ~1mm
                ~1cm
                10mm
                10cm
                눈날림mm
                빗방울눈날림mm
                 */
				final String pcpText = lis.get(4).getElementsByTag("span").get(1).text();
				int index = 0;

				if (pcpText.contains(mm) || pcpText.contains(cm)) {
					if (pcpText.contains(rainDrop)) {
						kmaHourlyForecast.setHasRain(true).setRainVolume(rainDrop);
					}
					if (pcpText.contains(snowBlizzard)) {
						kmaHourlyForecast.setHasSnow(true).setSnowVolume(snowBlizzard);
					}

					if (pcpText.contains(lessThan1mm)) {
						kmaHourlyForecast.setHasRain(true).setRainVolume(lessThan1mm);
					} else if (pcpText.contains(mm) && !kmaHourlyForecast.isHasRain()) {
						index = pcpText.indexOf(mm);
						String subStr = pcpText.substring(0, index);
						if (!subStr.contains(rainDrop) && !subStr.contains(snowBlizzard)) {
							kmaHourlyForecast.setHasRain(true).setRainVolume(subStr + mm);
						}
					}

					if (pcpText.contains(lessThan1cm)) {
						kmaHourlyForecast.setHasSnow(true).setSnowVolume(lessThan1cm);
					} else if (pcpText.contains(cm) && !kmaHourlyForecast.isHasSnow()) {
						index = pcpText.indexOf(cm);
						int firstIndex = 0;
						if (pcpText.contains(mm)) {
							firstIndex = pcpText.indexOf(mm) + 2;
						}
						String subStr = pcpText.substring(firstIndex, index);
						if (!subStr.contains(rainDrop) && !subStr.contains(snowBlizzard)) {
							kmaHourlyForecast.setHasSnow(true).setSnowVolume(subStr + cm);
						}
					}
				}

				pop = lis.get(5).getElementsByTag("span").get(1).text();
				windDirection = lis.get(6).getElementsByTag("span").get(1).text();
				if (lis.get(6).getElementsByTag("span").size() >= 3) {
					windSpeed = lis.get(6).getElementsByTag("span").get(2).text();
				} else {
					windDirection = null;
					windSpeed = null;
				}
				humidity = lis.get(7).getElementsByTag("span").get(1).text();

				kmaHourlyForecast.setHour(zonedDateTime).setWeatherDescription(weatherDescription)
						.setTemp(temp).setFeelsLikeTemp(feelsLikeTemp)
						.setPop(pop).setWindDirection(windDirection).setWindSpeed(windSpeed).setHumidity(humidity)
						.setHasShower(hasShower);
				kmaHourlyForecasts.add(kmaHourlyForecast);

			}
		}

		return kmaHourlyForecasts;
	}

	public static List<KmaDailyForecast> parseDailyForecasts(Document document) {
		final Elements elements = document.getElementsByClass("slide-wrap");
		//이후 10일
		final Elements slides = elements.select("div.slide.day-ten div.daily");

		ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		zonedDateTime = zonedDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
		LocalDate localDate = null;
		String date = null;
		String weatherDescription = null;
		String minTemp = null;
		String maxTemp = null;
		String pop = null;
		List<KmaDailyForecast> kmaDailyForecasts = new ArrayList<>();

		for (Element daily : slides) {
			Elements uls = daily.getElementsByClass("item-wrap").select("ul");
			KmaDailyForecast kmaDailyForecast = new KmaDailyForecast();

			date = daily.attr("data-date");
			localDate = LocalDate.parse(date);
			zonedDateTime = zonedDateTime.withYear(localDate.getYear()).withMonth(localDate.getMonthValue())
					.withDayOfMonth(localDate.getDayOfMonth());

			if (uls.size() == 2) {
				//am, pm
				Elements amLis = uls.get(0).getElementsByTag("li");
				Elements pmLis = uls.get(1).getElementsByTag("li");

				weatherDescription = amLis.get(1).getElementsByTag("span").get(1).text();
				minTemp = amLis.get(2).getElementsByTag("span").get(1).text();
				minTemp = minTemp.substring(3, minTemp.length() - 1);
				pop = amLis.get(3).getElementsByTag("span").get(1).text();

				KmaDailyForecast.Values am = new KmaDailyForecast.Values();
				am.setWeatherDescription(weatherDescription).setPop(pop);

				weatherDescription = pmLis.get(1).getElementsByTag("span").get(1).text();
				maxTemp = pmLis.get(2).getElementsByTag("span").get(1).text();
				maxTemp = maxTemp.substring(3, maxTemp.length() - 1);
				pop = pmLis.get(3).getElementsByTag("span").get(1).text();

				KmaDailyForecast.Values pm = new KmaDailyForecast.Values();
				pm.setWeatherDescription(weatherDescription).setPop(pop);

				kmaDailyForecast.setAmValues(am).setPmValues(pm);
			} else {
				//single
				kmaDailyForecast.setSingle(true);
				Elements lis = uls.get(0).getElementsByTag("li");

				weatherDescription = lis.get(1).getElementsByTag("span").get(1).text();
				String[] temps = lis.get(2).getElementsByTag("span").get(1).text().split(" / ");
				minTemp = temps[0].substring(3, temps[0].length() - 1);
				maxTemp = temps[1].substring(3, temps[1].length() - 1);
				pop = lis.get(3).getElementsByTag("span").get(1).text();

				KmaDailyForecast.Values single = new KmaDailyForecast.Values();
				single.setPop(pop).setWeatherDescription(weatherDescription);
				kmaDailyForecast.setSingleValues(single);
			}


			kmaDailyForecast.setMinTemp(minTemp).setMaxTemp(maxTemp).setDate(zonedDateTime);

			kmaDailyForecasts.add(kmaDailyForecast);
		}

		return kmaDailyForecasts;
	}

	public static List<KmaDailyForecast> makeExtendedDailyForecasts(List<KmaHourlyForecast> hourlyForecasts, List<KmaDailyForecast> dailyForecasts) {
		final ZoneId krZoneId = dailyForecasts.get(0).getDate().getZone();
		final ZonedDateTime firstDateTimeOfDaily = ZonedDateTime.of(dailyForecasts.get(0).getDate().toLocalDateTime(),
				krZoneId);

		ZonedDateTime criteriaDateTime = ZonedDateTime.now(krZoneId);
		criteriaDateTime = criteriaDateTime.withHour(23);
		criteriaDateTime = criteriaDateTime.withMinute(59);

		int beginIdx = 0;
		for (; beginIdx < hourlyForecasts.size(); beginIdx++) {
			if (criteriaDateTime.isBefore(hourlyForecasts.get(beginIdx).getHour())) {
				break;
			}
		}
		Integer minTemp = Integer.MAX_VALUE;
		Integer maxTemp = Integer.MIN_VALUE;
		int hours = 0;
		String amSky = null;
		String pmSky = null;
		String amPop = null;
		String pmPop = null;
		ZonedDateTime dateTime = null;

		int temp = 0;

		for (; beginIdx < hourlyForecasts.size(); beginIdx++) {
			if (firstDateTimeOfDaily.getDayOfYear() == hourlyForecasts.get(beginIdx).getHour().getDayOfYear()) {
				if (hourlyForecasts.get(beginIdx).getHour().getHour() == 1) {
					break;
				}
			}
			hours = hourlyForecasts.get(beginIdx).getHour().getHour();

			if (hours == 0 && minTemp != Integer.MAX_VALUE) {
				dateTime = ZonedDateTime.of(hourlyForecasts.get(beginIdx).getHour().toLocalDateTime(),
						hourlyForecasts.get(beginIdx).getHour().getZone());
				dateTime = dateTime.minusDays(1);

				KmaDailyForecast kmaDailyForecast = new KmaDailyForecast();
				kmaDailyForecast.setAmValues(new KmaDailyForecast.Values()).setPmValues(new KmaDailyForecast.Values())
						.setDate(dateTime).setMaxTemp(maxTemp.toString()).setMinTemp(minTemp.toString());
				kmaDailyForecast.getAmValues().setPop(amPop).setWeatherDescription(amSky);
				kmaDailyForecast.getPmValues().setPop(pmPop).setWeatherDescription(pmSky);

				dailyForecasts.add(kmaDailyForecast);

				minTemp = Integer.MAX_VALUE;
				maxTemp = Integer.MIN_VALUE;
			} else {
				temp = Integer.parseInt(hourlyForecasts.get(beginIdx).getTemp());

				minTemp = Math.min(minTemp, temp);
				maxTemp = Math.max(maxTemp, temp);

				if (hours == 9) {
					amSky = KmaResponseProcessor.convertHourlyWeatherDescriptionToMid(hourlyForecasts.get(beginIdx).getWeatherDescription());
					amPop = hourlyForecasts.get(beginIdx).getPop();
				} else if (hours == 15) {
					pmSky = KmaResponseProcessor.convertHourlyWeatherDescriptionToMid(hourlyForecasts.get(beginIdx).getWeatherDescription());
					pmPop = hourlyForecasts.get(beginIdx).getPop();
				}

			}
		}

		Collections.sort(dailyForecasts, new Comparator<KmaDailyForecast>() {
			@Override
			public int compare(KmaDailyForecast t1, KmaDailyForecast t2) {
				return t1.getDate().compareTo(t2.getDate());
			}
		});

		return dailyForecasts;
	}
}
