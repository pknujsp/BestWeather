package com.lifedawn.bestweather.commons.classes;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;

public class TextUtil {
	private TextUtil() {
	}

	public static SpannableString getUnderLineColorText(String string, String targetString, int color) {
		SpannableString spannableString = new SpannableString(string);
		int targetStartIndex = string.indexOf(targetString);
		int targetEndIndex = targetStartIndex + targetString.length();
		spannableString.setSpan(new ForegroundColorSpan(color), targetStartIndex, targetEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannableString.setSpan(new UnderlineSpan(), targetStartIndex, targetEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return spannableString;
	}


}
