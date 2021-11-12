package com.lifedawn.bestweather.widget;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Window;
import android.view.WindowManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.databinding.ActivityDialogBinding;
import com.lifedawn.bestweather.main.MainActivity;

public class DialogActivity extends Activity {
	private ActivityDialogBinding binding;
	private Class<?> widgetClassName;
	private int appWidgetId;
	private int widgetLayoutId;
	private LocationType locationType;
	private String addressName;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		binding = DataBindingUtil.setContentView(this, R.layout.activity_dialog);

		Bundle bundle = getIntent().getExtras();
		widgetClassName = (Class<?>) bundle.getSerializable(getString(R.string.bundle_key_widgetname));
		appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		widgetLayoutId = bundle.getInt(getString(R.string.bundle_key_widget_layout_id));
		addressName = bundle.getString(getString(R.string.bundle_key_address_name));
		locationType = (LocationType) bundle.getSerializable(getString(R.string.bundle_key_location_type));

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
							Intent refreshIntent = new Intent(getApplicationContext(), widgetClassName);
							refreshIntent.setAction(getString(R.string.ACTION_REFRESH));
							Bundle refreshBundle = new Bundle();
							refreshBundle.putSerializable(getString(R.string.bundle_key_widgetname), widgetClassName);
							refreshBundle.putInt(getString(R.string.bundle_key_widget_layout_id), widgetLayoutId);

							refreshIntent.putExtras(refreshBundle);
							PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, refreshIntent, 0);
							try {
								pendingIntent.send();
							} catch (PendingIntent.CanceledException e) {
								e.printStackTrace();
							}
						} else if (which == 3) {
							//현재 위치 업데이트
						}
						dialog.dismiss();
						finish();
					}
				}).create();

		dialog.show();
	}
}