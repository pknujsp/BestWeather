package com.lifedawn.bestweather.notification.daily;

import android.content.Context;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.daily.viewcreator.FifthDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FirstDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FourthDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.SecondDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.ThirdDailyNotificationViewCreator;

public enum DailyPushNotificationType {
	First(0), Second(1), Third(2), Fourth(3), Fifth(4);

	private final int index;

	DailyPushNotificationType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public static DailyPushNotificationType valueOf(int index) {
		if (index < First.index || index > Fifth.index) {
			return null;
		}

		if (index == First.index) {
			return First;
		} else if (index == Second.index) {
			return Second;
		} else if (index == Third.index) {
			return Third;
		} else if (index == Fourth.index) {
			return Fourth;
		} else {
			return Fifth;
		}
	}

	public static String getNotificationName(DailyPushNotificationType type, Context context) {
		switch (type) {
			case First:
				//시간별 예보
				return context.getString(R.string.FirstDailyPushNotification);
			case Second:
				//현재날씨
				return context.getString(R.string.SecondDailyPushNotification);
			case Third:
				//일별 예보
				return context.getString(R.string.ThirdDailyPushNotification);
			case Fourth:
				//현재 대기질
				return context.getString(R.string.FourthDailyPushNotification);
			default:
				//대기질 예보
				return context.getString(R.string.FifthDailyPushNotification);
		}
	}
}
