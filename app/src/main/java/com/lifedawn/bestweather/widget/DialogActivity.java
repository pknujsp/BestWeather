package com.lifedawn.bestweather.widget;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.databinding.ActivityDialogBinding;
import com.lifedawn.bestweather.main.MainActivity;

public class DialogActivity extends Activity {
	private ActivityDialogBinding binding;
	private Class<?> widgetClass;
	private int appWidgetId;
	private RemoteViews remoteViews;
	private LocationType locationType;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		binding = DataBindingUtil.setContentView(this, R.layout.activity_dialog);

		Bundle bundle = getIntent().getExtras();
		widgetClass = (Class<?>) bundle.getSerializable(ConfigureWidgetActivity.WidgetAttributes.WIDGET_CLASS.name());
		appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		remoteViews = bundle.getParcelable(ConfigureWidgetActivity.WidgetAttributes.REMOTE_VIEWS.name());

		SharedPreferences sharedPreferences =
				getSharedPreferences(ConfigureWidgetActivity.WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId, MODE_PRIVATE);
		locationType = LocationType.valueOf(sharedPreferences.getString(ConfigureWidgetActivity.WidgetAttributes.LOCATION_TYPE.name(),
				LocationType.CurrentLocation.name()));

		String[] listItems = null;
		if (locationType == LocationType.CurrentLocation) {
			listItems = new String[]{getString(R.string.open_app),
					getString(R.string.cancel), getString(R.string.refresh), getString(R.string.refresh_current_location)};
		} else {
			listItems = new String[]{getString(R.string.open_app), getString(R.string.cancel), getString(R.string.refresh)};
		}

		AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light_Dialog))
				.setTitle(getString(R.string.widget_control))
				.setCancelable(false)
				.setItems(listItems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							Intent intent = new Intent(getApplicationContext(), MainActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);


						} else if (which == 1) {

						} else if (which == 2) {
							Intent refreshIntent = new Intent(getApplicationContext(), widgetClass);
							refreshIntent.setAction(getString(R.string.ACTION_REFRESH));

							Bundle bundle = new Bundle();
							bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
							bundle.putParcelable(ConfigureWidgetActivity.WidgetAttributes.REMOTE_VIEWS.name(), remoteViews);
							refreshIntent.putExtras(bundle);

							PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), appWidgetId, refreshIntent,
									PendingIntent.FLAG_CANCEL_CURRENT);
							try {
								pendingIntent.send();
							} catch (PendingIntent.CanceledException e) {
								e.printStackTrace();
							}
						} else if (which == 3) {
							//현재 위치 업데이트
							Intent refreshCurrentLocationIntent = new Intent(getApplicationContext(), widgetClass);
							refreshCurrentLocationIntent.setAction(getString(R.string.ACTION_REFRESH_CURRENT_LOCATION));
							Bundle bundle = new Bundle();
							bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
							bundle.putParcelable(ConfigureWidgetActivity.WidgetAttributes.REMOTE_VIEWS.name(), remoteViews);
							refreshCurrentLocationIntent.putExtras(bundle);

							PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), appWidgetId,
									refreshCurrentLocationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
							try {
								pendingIntent.send();
							} catch (PendingIntent.CanceledException e) {
								e.printStackTrace();
							}
						}
						dialog.dismiss();
						finish();
					}
				}).create();

		dialog.show();
	}
}