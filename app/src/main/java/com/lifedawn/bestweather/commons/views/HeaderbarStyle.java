package com.lifedawn.bestweather.commons.views;

import android.app.Activity;
import android.view.View;

public class HeaderbarStyle {
	public enum Style {
		Black, White
	}

	private HeaderbarStyle() {
	}

	public static void setStyle(Style style, Activity activity) {

		if (activity != null) {
			int newValue = 0;
			if (style == Style.Black) {
				// 상단바 블랙으로
				newValue = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			} else {
				// 상단바 하양으로
				newValue = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			}
			if (activity.getWindow().getDecorView().getSystemUiVisibility() != newValue)
				activity.getWindow().getDecorView().setSystemUiVisibility(newValue);
		}
	}
}
