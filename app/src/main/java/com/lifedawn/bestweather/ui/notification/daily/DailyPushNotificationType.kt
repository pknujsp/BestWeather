package com.lifedawn.bestweather.ui.notification.daily

import android.content.Context
import com.lifedawn.bestweather.R

enum class DailyPushNotificationType(val index: Int) {
    First(0), Second(1), Third(2), Fourth(3), Fifth(4);

    companion object {
        fun valueOf(index: Int): DailyPushNotificationType? {
            if (index < First.index || index > Fifth.index) {
                return null
            }
            if (index == First.index) {
                return First
            } else if (index == Second.index) {
                return Second
            } else if (index == Third.index) {
                return Third
            } else if (index == Fourth.index) {
                return Fourth
            } else {
                return Fifth
            }
        }

        fun getNotificationName(type: DailyPushNotificationType?, context: Context): String {
            when (type) {
                First ->                //시간별 예보
                    return context.getString(R.string.FirstDailyPushNotification)
                Second ->                //현재날씨
                    return context.getString(R.string.SecondDailyPushNotification)
                Third ->                //일별 예보
                    return context.getString(R.string.ThirdDailyPushNotification)
                Fourth ->                //현재 대기질
                    return context.getString(R.string.FourthDailyPushNotification)
                else ->                //대기질 예보
                    return context.getString(R.string.FifthDailyPushNotification)
            }
        }
    }
}