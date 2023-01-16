package com.lifedawn.bestweather.commons.views

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.lifedawn.bestweather.databinding.ProgressViewBinding
import java.util.*

object ProgressDialog {
    private val dialogStack = Stack<AlertDialog>()
    @JvmStatic
    fun show(activity: Activity, msg: String?, cancelOnClickListener: View.OnClickListener?) {
        if (!activity.isFinishing && activity.isDestroyed) {
            return
        } else if (dialogStack.size > 0) return
        val binding = ProgressViewBinding.inflate(activity.layoutInflater)
        binding.progressMsg.text = msg
        val dialog = AlertDialog.Builder(activity).setCancelable(false).setView(binding.root).create()
        clearDialogs()
        dialog.show()
        val window = dialog.window
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window!!.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        }
        if (cancelOnClickListener == null) {
            binding.cancelBtn.visibility = View.GONE
        } else {
            binding.cancelBtn.setOnClickListener { v: View? ->
                cancelOnClickListener.onClick(v)
                dialog.dismiss()
            }
        }
        dialogStack.push(dialog)
    }

    @JvmStatic
    fun clearDialogs() {
        for (i in dialogStack.indices) {
            val dialog = dialogStack.pop()
            if (dialog.window != null) {
                dialog.dismiss()
            }
        }
        dialogStack.clear()
    }
}