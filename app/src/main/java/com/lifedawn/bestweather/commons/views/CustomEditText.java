package com.lifedawn.bestweather.commons.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.lifedawn.bestweather.R;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomEditText extends AppCompatEditText implements TextWatcher, View.OnTouchListener {
	private Drawable closeDrawable;
	private OnEditTextQueryListener onEditTextQueryListener;
	private Timer timer = new Timer();

	public CustomEditText(@NonNull @NotNull Context context) {
		super(context);
		init();
	}

	public CustomEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CustomEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, android.R.attr.editTextStyle);
		init();
	}

	private void init() {
		setInputType(InputType.TYPE_CLASS_TEXT);
		closeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_close_24);
		DrawableCompat.setTintList(closeDrawable, getHintTextColors());

		final int btnSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f, getResources().getDisplayMetrics());
		closeDrawable.setBounds(0, 0, btnSize, btnSize);
		setClearBtnVisibility(false);

		addTextChangedListener(this);
		setOnTouchListener(this);
	}

	@Override
	public void setOnKeyListener(OnKeyListener l) {
		super.setOnKeyListener(l);
	}

	private void setClearBtnVisibility(boolean visible) {
		closeDrawable.setVisible(visible, false);
		setCompoundDrawables(null, null, visible ? closeDrawable : null, null);
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		if (isFocused()) {
			setClearBtnVisibility(charSequence.length() > 0);
		}
	}

	private final int DELAY = 500;

	@Override
	public void afterTextChanged(Editable editable) {
		timer.cancel();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (onEditTextQueryListener != null) {
					Log.e("afterTextChanged", editable.toString());
					onEditTextQueryListener.onTextChange(editable.toString());
				}
			}
		}, DELAY);
	}


	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		final int x = (int) motionEvent.getX();

		if (closeDrawable.isVisible() && x >= getWidth() - getPaddingRight() - closeDrawable.getIntrinsicWidth()) {
			setText("");
			setError(null);
		}
		return false;
	}

	public CustomEditText setOnEditTextQueryListener(OnEditTextQueryListener onEditTextQueryListener) {
		this.onEditTextQueryListener = onEditTextQueryListener;
		return this;
	}

	public interface OnEditTextQueryListener {
		void onTextChange(String newText);

		void onTextSubmit(String text);
	}
}