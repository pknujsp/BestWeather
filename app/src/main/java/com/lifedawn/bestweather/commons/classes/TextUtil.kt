package com.lifedawn.bestweather.commons.classes

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan

object TextUtil {
    @JvmStatic
    fun getUnderLineColorText(string: String, targetString: String, color: Int): SpannableString {
        val spannableString = SpannableString(string)
        val targetStartIndex = string.indexOf(targetString)
        val targetEndIndex = targetStartIndex + targetString.length
        spannableString.setSpan(ForegroundColorSpan(color), targetStartIndex, targetEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), targetStartIndex, targetEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }
}