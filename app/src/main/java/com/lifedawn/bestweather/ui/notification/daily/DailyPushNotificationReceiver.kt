package com.lifedawn.bestweather.ui.notification.daily

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.work.*
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.ui.notification.daily.work.DailyNotificationListenableWorker

class DailyPushNotificationReceiver constructor() : BroadcastReceiver() {
    public override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.getAction()
        if (action != null) {
            val arguments: Bundle? = intent.getExtras()
            val id: Int = arguments!!.getInt(BundleKey.dtoId.name)
            val data: Data = Data.Builder()
                .putString(
                    "DailyPushNotificationType", arguments.getString(
                        "DailyPushNotificationType"
                    )
                )
                .putInt(BundleKey.dtoId.name, id)
                .putString("action", action)
                .build()
            val tag: String = DailyNotificationListenableWorker::class.java.getName() + action
            val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(DailyNotificationListenableWorker::class.java)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(data)
                .addTag(tag)
                .build()
            val workManager: WorkManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request)
        }
    }
}