package com.lifedawn.bestweather.commons.views;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.lifedawn.bestweather.databinding.ProgressViewBinding;

import java.util.Stack;

public class ProgressDialog {
	private final static Stack<AlertDialog> dialogStack = new Stack<>();

	private ProgressDialog() {
	}

	public static void show(Activity activity, String msg, @Nullable View.OnClickListener cancelOnClickListener) {
		if (!activity.isFinishing() && activity.isDestroyed()) {
			return;
		} else if (dialogStack.size() > 0)
			return;

		ProgressViewBinding binding = ProgressViewBinding.inflate(activity.getLayoutInflater());
		binding.progressMsg.setText(msg);

		AlertDialog dialog = new AlertDialog.Builder(activity).setCancelable(false).setView(binding.getRoot()).create();

		clearDialogs();
		dialog.show();

		Window window = dialog.getWindow();
		if (window != null) {
			window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
			layoutParams.copyFrom(dialog.getWindow().getAttributes());
			layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
		}

		if (cancelOnClickListener == null) {
			binding.cancelBtn.setVisibility(View.GONE);
		} else {
			binding.cancelBtn.setOnClickListener(v -> {
				cancelOnClickListener.onClick(v);
				dialog.dismiss();
			});
		}

		dialogStack.push(dialog);
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
