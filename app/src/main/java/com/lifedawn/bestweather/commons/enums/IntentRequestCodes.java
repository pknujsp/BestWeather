package com.lifedawn.bestweather.commons.enums;

import java.io.Serializable;

public enum IntentRequestCodes implements Serializable {
	ONGOING_NOTIFICATION_MANUALLY_REFRESH(10), DAILY_NOTIFICATION(50000), WIDGET_MANUALLY_REFRESH(30), WIDGET_AUTO_REFRESH(40),
	ONGOING_NOTIFICATION_AUTO_REFRESH(50), GPS(60), PERMISSION(70), CLICK_WIDGET(80);

	public final int requestCode;


	IntentRequestCodes(int requestCode) {
		this.requestCode = requestCode;
	}
}
