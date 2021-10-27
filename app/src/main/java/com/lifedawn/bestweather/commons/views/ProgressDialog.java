package com.lifedawn.bestweather.commons.views;

import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;

public class ProgressDialog {
	private ProgressDialog() {
	}

	public static AlertDialog show(Activity activity, String msg) {
		View progressView = activity.getLayoutInflater().inflate(R.layout.progress_view, null);
		((TextView) progressView.findViewById(R.id.progress_msg)).setText(msg);

		AlertDialog dialog = new MaterialAlertDialogBuilder(activity).setCancelable(false).setView(progressView).create();
		dialog.show();

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
