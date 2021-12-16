package com.lifedawn.bestweather.widget;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.databinding.ActivityDialogBinding;
import com.lifedawn.bestweather.main.MainActivity;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;
import com.lifedawn.bestweather.widget.creator.CurrentWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FirstSimpleWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FullWidgetCreator;
import com.lifedawn.bestweather.widget.creator.SecSimpleWidgetCreator;
import com.lifedawn.bestweather.widget.creator.ThirdSimpleWidgetCreator;
import com.lifedawn.bestweather.widget.widgetprovider.CurrentWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FirstSimpleWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FullWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.SecSimpleWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.ThirdSimpleWidgetProvider;

public class DialogActivity extends Activity {
	private ActivityDialogBinding binding;
	private Class<?> widgetClass;
	private int appWidgetId;
	private RemoteViews remoteViews;
	private LocationType locationType;
	private AbstractWidgetCreator widgetCreator;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		binding = DataBindingUtil.setContentView(this, R.layout.activity_dialog);

		Bundle bundle = getIntent().getExtras();
		appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		remoteViews = bundle.getParcelable(WidgetNotiConstants.WidgetAttributes.REMOTE_VIEWS.name());

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		int widgetLayoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;

		if (widgetLayoutId == R.layout.widget_current) {
			widgetCreator = new CurrentWidgetCreator(getApplicationContext(), null, appWidgetId);
			widgetClass = CurrentWidgetProvider.class;
		} else if (widgetLayoutId == R.layout.widget_full) {
			widgetCreator = new FullWidgetCreator(getApplicationContext(), null, appWidgetId);
			widgetClass = FullWidgetProvider.class;
		} else if (widgetLayoutId == R.layout.widget_simple) {
			widgetCreator = new FirstSimpleWidgetCreator(getApplicationContext(), null, appWidgetId);
			widgetClass = FirstSimpleWidgetProvider.class;
		} else if (widgetLayoutId == R.layout.widget_simple2) {
			widgetCreator = new SecSimpleWidgetCreator(getApplicationContext(), null, appWidgetId);
			widgetClass = SecSimpleWidgetProvider.class;
		} else if (widgetLayoutId == R.layout.widget_simple3) {
			widgetCreator = new ThirdSimpleWidgetCreator(getApplicationContext(), null, appWidgetId);
			widgetClass = ThirdSimpleWidgetProvider.class;
		}

		widgetCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						locationType = LocationType.valueOf(result.getLocationType());

						String[] listItems = null;
						if (locationType == LocationType.CurrentLocation) {
							listItems = new String[]{getString(R.string.open_app),
									getString(R.string.cancel), getString(R.string.refresh), getString(R.string.refresh_current_location)};
						} else {
							listItems = new String[]{getString(R.string.open_app), getString(R.string.cancel), getString(R.string.refresh)};
						}

						new AlertDialog.Builder(new ContextThemeWrapper(DialogActivity.this, R.style.Theme_AppCompat_Light_Dialog))
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
											refreshIntent.setAction(getString(R.string.com_lifedawn_bestweather_action_REFRESH));

											Bundle bundle = new Bundle();
											bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
											bundle.putParcelable(WidgetNotiConstants.WidgetAttributes.REMOTE_VIEWS.name(), remoteViews);
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
											refreshCurrentLocationIntent.setAction(getString(R.string.com_lifedawn_bestweather_action_REFRESH_CURRENT_LOCATION));
											Bundle bundle = new Bundle();
											bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
											bundle.putParcelable(WidgetNotiConstants.WidgetAttributes.REMOTE_VIEWS.name(), remoteViews);
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
								}).create().show();
					}
				});
			}

			@Override
			public void onResultNoData() {

			}
		});


	}
}