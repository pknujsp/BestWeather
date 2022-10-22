package com.lifedawn.bestweather.commons.views;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ColorStateListDrawable;
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
import com.lifedawn.bestweather.widget.DialogActivity;

import java.util.Stack;

public class ProgressDialog {
	private final static Stack<AlertDialog> dialogStack = new Stack<>();

	private ProgressDialog() {
	}

	public static AlertDialog show(Activity activity, String msg, @Nullable View.OnClickListener cancelOnClickListener) {
		if (!activity.isFinishing() && activity.isDestroyed()) {
			return null;
		}

		clearDialogs();

		View progressView = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.progress_view, null);
		((TextView) progressView.findViewById(R.id.progress_msg)).setText(msg);

		AlertDialog dialog = new AlertDialog.Builder(activity).setCancelable(false).setView(progressView).create();
		dialog.show();

		Window window = dialog.getWindow();
		if (window != null) {
			window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
			layoutParams.copyFrom(dialog.getWindow().getAttributes());
			layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
		}

		if (cancelOnClickListener == null) {
			progressView.findViewById(R.id.cancel_btn).setVisibility(View.GONE);
		} else {
			progressView.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cancelOnClickListener.onClick(v);
					dialog.dismiss();
				}
			});
		}

		dialogStack.push(dialog);
		return dialog;
	}

	public static void clearDialogs() {
		for (int i = 0; i < dialogStack.size(); i++) {
			AlertDialog dialog = dialogStack.pop();

			if (dialog.getWindow() != null) {
				dialog.dismiss();
			}
		}

		dialogStack.clear();
	}
}
