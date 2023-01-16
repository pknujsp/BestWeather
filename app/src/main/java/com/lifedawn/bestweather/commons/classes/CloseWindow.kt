package com.lifedawn.bestweather.commons.classes

import android.app.Activity
import android.widget.Toast
import com.lifedawn.bestweather.R
import java.util.*

class CloseWindow(private val onBackKeyDoubleClickedListener: OnBackKeyDoubleClickedListener) {
    private var firstPressedTime = 0L
    private val DURATION = 2000L
    private var toast: Toast? = null
    fun clicked(activity: Activity?) {
        if (firstPressedTime > 0L) {
            val secondPressedTime = System.currentTimeMillis()
            if (secondPressedTime - firstPressedTime < DURATION) {
                /*
                    activity.moveTaskToBack(true); // 태스크를 백그라운드로 이동
                    activity.finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
                    android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
                     */
                onBackKeyDoubleClickedListener.onDoubleClicked()
            }
        } else {
            firstPressedTime = System.currentTimeMillis()
            if (toast == null) {
                toast = Toast.makeText(activity, R.string.message_request_double_click_for_close, Toast.LENGTH_SHORT)
                toast.setDuration(DURATION.toInt())
            }
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    firstPressedTime = 0L
                }
            }, DURATION)
            toast!!.show()
        }
    }

    interface OnBackKeyDoubleClickedListener {
        fun onDoubleClicked()
    }
}