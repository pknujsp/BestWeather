package com.lifedawn.bestweather.forremoteviews;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.forremoteviews.dto.WeatherJsonObj;

import java.time.ZoneId;

public class RemoteViewProcessor {

	public static void onBeginProcess(RemoteViews remoteViews) {
		remoteViews.setViewVisibility(R.id.progressbar, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);
	}

	public static void onSuccessfulProcess(RemoteViews remoteViews) {
		remoteViews.setViewVisibility(R.id.content_container, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);
		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
	}

	public static void onErrorProcess(RemoteViews remoteViews, String errorMsg, String btnMsg) {
		remoteViews.setViewVisibility(R.id.warning_layout, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);
		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
		remoteViews.setTextViewText(R.id.warning, errorMsg);
		remoteViews.setTextViewText(R.id.warning_process_btn, btnMsg);
	}


}
