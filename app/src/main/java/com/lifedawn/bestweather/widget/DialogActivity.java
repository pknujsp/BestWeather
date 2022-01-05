package com.lifedawn.bestweather.widget;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
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
import com.lifedawn.bestweather.widget.creator.EighthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.EleventhWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FirstWidgetCreator;
import com.lifedawn.bestweather.widget.creator.NinthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.SecondWidgetCreator;
import com.lifedawn.bestweather.widget.creator.SeventhWidgetCreator;
import com.lifedawn.bestweather.widget.creator.SixthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.TenthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.ThirdWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FourthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FifthWidgetCreator;
import com.lifedawn.bestweather.widget.widgetprovider.EighthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.EleventhWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FirstWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.NinthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.SecondWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.SeventhWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.SixthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.TenthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.ThirdWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FourthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FifthWidgetProvider;

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
		final AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		ComponentName componentName = appWidgetProviderInfo.provider;
		final String providerClassName = componentName.getClassName();

		if (providerClassName.equals(FirstWidgetProvider.class.getName())) {
			widgetCreator = new FirstWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (providerClassName.equals(SecondWidgetProvider.class.getName())) {
			widgetCreator = new SecondWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (providerClassName.equals(ThirdWidgetProvider.class.getName())) {
			widgetCreator = new ThirdWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (providerClassName.equals(FourthWidgetProvider.class.getName())) {
			widgetCreator = new FourthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (providerClassName.equals(FifthWidgetProvider.class.getName())) {
			widgetCreator = new FifthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (providerClassName.equals(SixthWidgetProvider.class.getName())) {
			widgetCreator = new SixthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (providerClassName.equals(SeventhWidgetProvider.class.getName())) {
			widgetCreator = new SeventhWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (providerClassName.equals(EighthWidgetProvider.class.getName())) {
			widgetCreator = new EighthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (providerClassName.equals(NinthWidgetProvider.class.getName())) {
			widgetCreator = new NinthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (providerClassName.equals(TenthWidgetProvider.class.getName())) {
			widgetCreator = new TenthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (providerClassName.equals(EleventhWidgetProvider.class.getName())) {
			widgetCreator = new EleventhWidgetCreator(getApplicationContext(), null, appWidgetId);
		}

		try {
			widgetClass = Class.forName(providerClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		widgetCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						locationType = result.getLocationType();

						String[] listItems = new String[]{getString(R.string.open_app), getString(R.string.cancel), getString(R.string.refresh)};

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
											refreshIntent.putExtras(bundle);

											PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), appWidgetId, refreshIntent,
													PendingIntent.FLAG_CANCEL_CURRENT);
											try {
												pendingIntent.send();
											} catch (PendingIntent.CanceledException e) {
												e.printStackTrace();
											}
										} else if (which == 3) {
											/*
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

											 */
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