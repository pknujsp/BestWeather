package com.lifedawn.bestweather.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.lifedawn.bestweather.R;

public class NotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {

		}
	}
}
