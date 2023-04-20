package com.lifedawn.bestweather.ui.notification.ongoing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.lifedawn.bestweather.ui.notification.ongoing.work.OngoingNotificationListenableWorker

class OngoingNotificationReceiver constructor() : BroadcastReceiver() {
    public override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.getAction()
        if (action != null) {
            val data: Data = Data.Builder().putString("action", action).build()
            val tag: String = "ongoing_notification" + action
            val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(OngoingNotificationListenableWorker::class.java)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(data)
                .addTag(tag)
                .build()
            val workManager: WorkManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request)
        }
    }
}