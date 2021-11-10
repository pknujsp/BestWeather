package com.lifedawn.bestweather.widget;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Window;
import android.view.WindowManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.databinding.ActivityDialogBinding;
import com.lifedawn.bestweather.main.MainActivity;

public class DialogActivity extends Activity {
	private ActivityDialogBinding binding;
	private Gps gps = new Gps();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		binding = DataBindingUtil.setContentView(this, R.layout.activity_dialog);

		AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light_Dialog))
				.setTitle("address")
				.setCancelable(false)
				.setPositiveButton(R.string.open_app, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();

						Intent intent = new Intent(getApplicationContext(), MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				}).create();

		dialog.show();
	}
}