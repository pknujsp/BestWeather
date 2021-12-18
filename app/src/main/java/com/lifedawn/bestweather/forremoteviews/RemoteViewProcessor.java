package com.lifedawn.bestweather.forremoteviews;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;

public class RemoteViewProcessor {

	public enum ErrorType {
		UNAVAILABLE_NETWORK, FAILED_LOAD_WEATHER_DATA, GPS_OFF, GPS_PERMISSION_REJECTED
	}

	public static void onBeginProcess(RemoteViews remoteViews) {
		remoteViews.setViewVisibility(R.id.progressbar, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.valuesLayout, View.GONE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);
	}

	public static void onSuccessfulProcess(RemoteViews remoteViews) {
		remoteViews.setViewVisibility(R.id.valuesLayout, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);
		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
	}

	public static void onErrorProcess(RemoteViews remoteViews, Context context, ErrorType errorType) {
		remoteViews.setViewVisibility(R.id.warning_layout, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.valuesLayout, View.GONE);
		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);

		switch (errorType) {
			case GPS_OFF:
				remoteViews.setTextViewText(R.id.warning, context.getString(R.string.request_to_make_gps_on));
				remoteViews.setTextViewText(R.id.warning_process_btn, context.getString(R.string.enable_gps));
				remoteViews.setViewVisibility(R.id.warning_process_btn, View.VISIBLE);
				break;
			case FAILED_LOAD_WEATHER_DATA:
				remoteViews.setTextViewText(R.id.warning, context.getString(R.string.update_failed));
				remoteViews.setTextViewText(R.id.warning_process_btn, context.getString(R.string.again));
				remoteViews.setViewVisibility(R.id.warning_process_btn, View.VISIBLE);
				break;
			case GPS_PERMISSION_REJECTED:
				remoteViews.setTextViewText(R.id.warning, context.getString(R.string.message_needs_location_permission));
				remoteViews.setTextViewText(R.id.warning_process_btn, context.getString(R.string.check_permission));
				remoteViews.setViewVisibility(R.id.warning_process_btn, View.VISIBLE);
				break;
			case UNAVAILABLE_NETWORK:
				remoteViews.setTextViewText(R.id.warning, context.getString(R.string.need_to_connect_network));
				remoteViews.setTextViewText(R.id.warning_process_btn, context.getString(R.string.again));
				remoteViews.setViewVisibility(R.id.warning_process_btn, View.VISIBLE);
				break;
		}

	}


}
