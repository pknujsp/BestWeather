package com.lifedawn.bestweather.commons.constants

import java.io.Serializable

enum class IntentRequestCodes(val requestCode: Int) : Serializable {
    ONGOING_NOTIFICATION_MANUALLY_REFRESH(10), DAILY_NOTIFICATION(50000), WIDGET_MANUALLY_REFRESH(30), WIDGET_AUTO_REFRESH(40),
    ONGOING_NOTIFICATION_AUTO_REFRESH(50), GPS(60), PERMISSION(70), CLICK_WIDGET(80);
}