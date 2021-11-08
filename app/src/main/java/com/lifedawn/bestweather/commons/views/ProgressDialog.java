package com.lifedawn.bestweather.commons.views;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;

public class ProgressDialog {
	private ProgressDialog() {
	}

	public static AlertDialog show(Activity activity, String msg, @Nullable View.OnClickListener cancelOnClickListener) {
		View progressView = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.progress_view, null);
		((TextView) progressView.findViewById(R.id.progress_msg)).setText(msg);

		AlertDialog dialog = new AlertDialog.Builder(activity).setCancelable(false).setView(progressView).create();
		dialog.show();

		if (cancelOnClickListener == null) {
			progressView.findViewById(R.id.cancel_btn).setVisibility(View.GONE);
		} else {
			progressView.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					cancelOnClickListener.onClick(v);
				}
			});
		}


		Window window = dialog.getWindow();
		if (window != null) {
			WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
			layoutParams.copyFrom(dialog.getWindow().getAttributes());
			layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
			layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
			dialog.getWindow().setAttributes(layoutParams);
		}

		return dialog;
	}
}
