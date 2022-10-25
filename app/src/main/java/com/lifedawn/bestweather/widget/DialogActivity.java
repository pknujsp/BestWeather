package com.lifedawn.bestweather.widget;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
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
	private int appWidgetId;
	private AbstractWidgetCreator widgetCreator;
	private AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		binding = ActivityDialogBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		Bundle bundle = getIntent().getExtras();
		appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

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

		final Class<?> widgetProviderClass = widgetCreator.widgetProviderClass();
		final View dialogView = getLayoutInflater().inflate(R.layout.view_widget_dialog, null);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		final Long refreshInterval = sharedPreferences.getLong(getString(R.string.pref_key_widget_refresh_interval), 0L);
		String refreshIntervalText = null;

		if (refreshInterval > 0) {
			String[] autoRefreshIntervalsValue = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
			String[] autoRefreshIntervalsText = getResources().getStringArray(R.array.AutoRefreshIntervals);

			for (int i = 0; i < autoRefreshIntervalsValue.length; i++) {
				if (refreshInterval.toString().equals(autoRefreshIntervalsValue[i])) {
					refreshIntervalText = autoRefreshIntervalsText[i];
					break;
				}
			}
		} else {
			refreshIntervalText = getString(R.string.disable_auto_refresh);
		}

		((TextView) dialogView.findViewById(R.id.auto_refresh_interval)).setText(refreshIntervalText);
		((TextView) dialogView.findViewById(R.id.widgetTypeName)).setText(appWidgetProviderInfo.loadLabel(getPackageManager()));

		((TextView) dialogView.findViewById(R.id.openAppBtn)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				alertDialog.dismiss();

				startActivity(intent);
				finish();
			}
		});

		((TextView) dialogView.findViewById(R.id.updateBtn)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PendingIntent pendingIntent = widgetCreator.getRefreshPendingIntent();
				try {
					pendingIntent.send();
				} catch (PendingIntent.CanceledException e) {
					e.printStackTrace();
				}

				alertDialog.dismiss();
				finish();
			}
		});

		((TextView) dialogView.findViewById(R.id.cancelBtn)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				finish();
			}
		});


		widgetCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (DialogActivity.this.isFinishing() || DialogActivity.this.isDestroyed()) {
							return;
						}

						alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(DialogActivity.this,
								R.style.Theme_AppCompat_Light_Dialog))
								.setCancelable(true)
								.setOnCancelListener(new DialogInterface.OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										dialog.dismiss();
										finish();
									}
								})
								.setView(dialogView)
								.create();

						alertDialog.show();
						alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					}
				});
			}

			@Override
			public void onResultNoData() {

			}
		});

	}
}