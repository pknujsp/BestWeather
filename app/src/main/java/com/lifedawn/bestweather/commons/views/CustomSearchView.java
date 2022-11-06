package com.lifedawn.bestweather.commons.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.ViewSearchBinding;
import com.lifedawn.bestweather.utils.DeviceUtils;

public class CustomSearchView extends FrameLayout {
	private ViewSearchBinding binding;
	private CustomEditText.OnEditTextQueryListener onEditTextQueryListener;
	int backBtnVisibility = 0;
	int searchBtnVisibility = 0;
	boolean enabled = false;
	boolean showStroke = true;
	String hint = null;

	public CustomSearchView(Context context) {
		super(context);
		init(context, null);
	}

	public CustomSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public CustomSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public CustomSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomSearchView, 0, 0);
		try {
			backBtnVisibility = a.getInt(R.styleable.CustomSearchView_backBtnVisibility, View.VISIBLE);
			searchBtnVisibility = a.getInt(R.styleable.CustomSearchView_searchBtnVisibility, View.VISIBLE);
			hint = a.getString(R.styleable.CustomSearchView_hint);
			enabled = a.getBoolean(R.styleable.CustomSearchView_enabled, true);
			showStroke = a.getBoolean(R.styleable.CustomSearchView_showStroke, true);
		} finally {
			a.recycle();
		}

		binding = ViewSearchBinding.inflate(LayoutInflater.from(context), this, true);

		if (!showStroke) {
			binding.getRoot().setBackground(ContextCompat.getDrawable(context, R.drawable.searchview_background_no_stroke));
		}

		binding.back.setVisibility(backBtnVisibility);
		binding.search.setVisibility(searchBtnVisibility);
		binding.edittext.setHint(hint);
		binding.edittext.setEnabled(enabled);

		if (!enabled) {
			binding.search.setVisibility(GONE);
		}

		binding.search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onEditTextQueryListener != null) {
					onEditTextQueryListener.onTextSubmit(binding.edittext.getText().length() > 0 ?
							binding.edittext.getText().toString() : "");
				}
			}
		});

		binding.edittext.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					binding.search.callOnClick();
					return true;
				}
				return false;
			}
		});

	}

	public void requestFocusEditText() {
		binding.edittext.requestFocus();
		DeviceUtils.Companion.showKeyboard(getContext(), binding.edittext);
	}

	public void clearFocusEditText() {
		binding.edittext.setText(null);
		binding.edittext.clearFocus();
	}


	public void setBackgroundTint(int color) {
		binding.getRoot().setBackgroundTintList(ColorStateList.valueOf(color));
	}

	public void setOnClickListener(@Nullable OnClickListener l) {
		binding.getRoot().setOnClickListener(l);
	}

	public void setEditTextOnClickListener(OnClickListener l) {
		binding.edittext.setOnClickListener(l);
	}

	public void setEditTextOnFocusListener(OnFocusChangeListener onFocusListener) {
		binding.edittext.setOnFocusChangeListener(onFocusListener);
	}

	public void callOnClickEditText() {
		binding.edittext.requestFocusFromTouch();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

	}


	public void setOnEditTextQueryListener(CustomEditText.OnEditTextQueryListener onEditTextQueryListener) {
		this.onEditTextQueryListener = onEditTextQueryListener;
		binding.edittext.setOnEditTextQueryListener(onEditTextQueryListener);
	}

	public void setOnBackClickListener(View.OnClickListener onBackClickListener) {
		binding.back.setOnClickListener(onBackClickListener);
	}

	public void setQuery(String query, boolean submit) {
		binding.edittext.setText(query);
		if (submit) {
			binding.search.callOnClick();
		}
	}

	public String getQuery() {
		return binding.edittext.getText().length() > 0 ? binding.edittext.getText().toString() : "";
	}
}
