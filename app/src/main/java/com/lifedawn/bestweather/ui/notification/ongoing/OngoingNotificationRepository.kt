package com.lifedawn.bestweather.ui.notification.ongoing

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback

class OngoingNotificationRepository private constructor(private val context: Context) {
    private val key: String = "ongoing_notification"
    fun getOngoingNotificationDto(callback: DbQueryCallback<OngoingNotificationDto?>) {
        MyApplication.getExecutorService().execute(object : Runnable {
            public override fun run() {
                val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                    context
                )
                val json: String? = sharedPreferences.getString(key, "")
                if (json!!.isEmpty()) callback.processResult(null) else {
                    val dto: OngoingNotificationDto = Gson().fromJson<OngoingNotificationDto>(json, OngoingNotificationDto::class.java)
                    callback.processResult(dto)
                }
            }
        })
    }

    fun save(ongoingNotificationDto: OngoingNotificationDto?, callback: BackgroundWorkCallback?) {
        MyApplication.getExecutorService().execute(object : Runnable {
            public override fun run() {
                val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                    context
                )
                sharedPreferences.edit().putString(key, Gson().toJson(ongoingNotificationDto)).commit()
                if (callback != null) {
                    callback.onFinished()
                }
            }
        })
    }

    fun remove() {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            context
        )
        sharedPreferences.edit().remove(key).apply()
    }

    companion object {
        var iNSTANCE: OngoingNotificationRepository? = null
            private set

        fun initialize(context: Context) {
            if (iNSTANCE == null) iNSTANCE = OngoingNotificationRepository(context)
        }
    }
}