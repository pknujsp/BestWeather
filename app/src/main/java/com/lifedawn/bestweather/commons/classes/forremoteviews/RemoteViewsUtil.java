package com.lifedawn.bestweather.commons.classes.forremoteviews;

import android.app.PendingIntent;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.IntentUtil;
import com.lifedawn.bestweather.commons.constants.IntentRequestCodes;

public class RemoteViewsUtil {

	public enum ErrorType {
		UNAVAILABLE_NETWORK, FAILED_LOAD_WEATHER_DATA, GPS_OFF, DENIED_GPS_PERMISSIONS, DENIED_BACKGROUND_LOCATION_PERMISSION
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
		remoteViews.setViewVisibility(R.id.refreshBtn, View.VISIBLE);

		String btn2Text = null;

		switch (errorType) {
			case GPS_OFF:
				remoteViews.setTextViewText(R.id.warning, context.getString(R.string.request_to_make_gps_on));
				remoteViews.setOnClickPendingIntent(R.id.btn2, PendingIntent.getActivity(context, IntentRequestCodes.GPS.requestCode,
						IntentUtil.getLocationSettingsIntent(), PendingIntent.FLAG_IMMUTABLE));
				btn2Text = context.getString(R.string.enable_gps);
				break;
			case FAILED_LOAD_WEATHER_DATA:
				remoteViews.setTextViewText(R.id.warning, context.getString(R.string.update_failed));
				break;
			case DENIED_GPS_PERMISSIONS:
				remoteViews.setTextViewText(R.id.warning, context.getString(R.string.message_needs_location_permission));
				remoteViews.setOnClickPendingIntent(R.id.btn2, PendingIntent.getActivity(context, IntentRequestCodes.PERMISSION.requestCode,
						IntentUtil.getAppSettingsIntent(context), PendingIntent.FLAG_IMMUTABLE));
				btn2Text = context.getString(R.string.check_permission);
				break;
			case UNAVAILABLE_NETWORK:
				remoteViews.setTextViewText(R.id.warning, context.getString(R.string.need_to_connect_network));
				break;
			case DENIED_BACKGROUND_LOCATION_PERMISSION:
				remoteViews.setTextViewText(R.id.warning, context.getString(R.string.uncheckedAllowAllTheTime));
				remoteViews.setOnClickPendingIntent(R.id.btn2,
						PendingIntent.getActivity(context, IntentRequestCodes.PERMISSION.requestCode,
								IntentUtil.getAppSettingsIntent(context),
								PendingIntent.FLAG_IMMUTABLE));
				btn2Text = context.getString(R.string.check_permission);
				break;
		}

		if (btn2Text != null) {
			remoteViews.setTextViewText(R.id.btn2, btn2Text);
			remoteViews.setViewVisibility(R.id.btn2, View.VISIBLE);
		} else {
			remoteViews.setViewVisibility(R.id.btn2, View.GONE);
		}
	}


}
