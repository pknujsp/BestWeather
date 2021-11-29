package com.lifedawn.bestweather.forremoteviews;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.dto.CurrentConditionsObj;
import com.lifedawn.bestweather.forremoteviews.dto.HeaderObj;
import com.lifedawn.bestweather.forremoteviews.dto.WeatherJsonObj;
import com.lifedawn.bestweather.notification.always.AlwaysNotiViewCreator;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
