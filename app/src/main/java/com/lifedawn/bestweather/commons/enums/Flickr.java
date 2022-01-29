package com.lifedawn.bestweather.commons.enums;

public class Flickr {
	public enum Time {
		sunrise("sunrise"), sunset("sunrise"), day("day"), night("night");

		private final String text;

		Time(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	public enum Weather {
		clear("clear"), partlyCloudy("partly_cloudy"), mostlyCloudy("mostly_cloudy"), overcast("overcast"), rain("rain"), snow("snow");

		private final String text;

		Weather(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}
}
