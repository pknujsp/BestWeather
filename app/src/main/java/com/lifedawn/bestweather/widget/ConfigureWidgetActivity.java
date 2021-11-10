package com.lifedawn.bestweather.widget;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.ActivityConfigureWidgetBinding;

public class ConfigureWidgetActivity extends AppCompatActivity {
	private ActivityConfigureWidgetBinding binding;
	private int appWidgetId;
	private int layoutId;
	private RemoteViews remoteViews;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_configure_widget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

		Bundle bundle = getIntent().getExtras();

		if (bundle != null) {
			appWidgetId = bundle.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;

		remoteViews = new RemoteViews(this.getPackageName(), layoutId);
		PendingIntent onClickPendingIntent = null;

		switch (layoutId) {
			case R.layout.current_first:
				onClickPendingIntent = CurrentFirst.getClickedPendingIntent(getApplicationContext());
				break;
			default:
				break;
		}

		remoteViews.setOnClickPendingIntent(R.id.content_container,
				onClickPendingIntent);

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

		binding.check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				remoteViews.setTextViewText(R.id.address, binding.edittext.getText().toString());
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

				Intent resultIntent = new Intent();
				resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				setResult(RESULT_OK, resultIntent);
				finish();
			}
		});

	}
}