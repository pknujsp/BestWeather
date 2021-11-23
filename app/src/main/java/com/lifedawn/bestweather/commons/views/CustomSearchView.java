package com.lifedawn.bestweather.commons.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.ViewSearchBinding;

import java.util.Objects;

public class CustomSearchView extends FrameLayout {
	private ViewSearchBinding binding;
	private CustomEditText.OnEditTextQueryListener onEditTextQueryListener;

	public CustomSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public CustomSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	public CustomSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(attrs);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

	}

	private void init(AttributeSet attrs) {
		int backBtnVisibility = 0;
		int searchBtnVisibility = 0;
		boolean clickable = false;
		boolean focusable = false;
		String hint = null;

		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomSearchView, 0, 0);
		try {
			backBtnVisibility = a.getInt(R.styleable.CustomSearchView_backBtnVisibility, View.VISIBLE);
			searchBtnVisibility = a.getInt(R.styleable.CustomSearchView_searchBtnVisibility, View.VISIBLE);
			clickable = a.getBoolean(R.styleable.CustomSearchView_clickable, true);
			focusable = a.getBoolean(R.styleable.CustomSearchView_focusable, true);
			hint = a.getString(R.styleable.CustomSearchView_hint);
		} finally {
			a.recycle();
		}
		setWillNotDraw(false);
		binding = ViewSearchBinding.inflate(LayoutInflater.from(getContext()));
		addView(binding.getRoot());

		binding.back.setVisibility(backBtnVisibility);
		binding.search.setVisibility(searchBtnVisibility);
		binding.getRoot().setClickable(clickable);
		binding.getRoot().setFocusable(focusable);
		binding.edittext.setHint(hint);

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

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void setOnTouchListener(OnTouchListener l) {
		super.setOnTouchListener(l);
	}

	public void setOnEditTextQueryListener(CustomEditText.OnEditTextQueryListener onEditTextQueryListener) {
		this.onEditTextQueryListener = onEditTextQueryListener;
		binding.edittext.setOnEditTextQueryListener(onEditTextQueryListener);
	}

	public void setOnBackClickListener(View.OnClickListener onBackClickListener) {
		binding.back.setOnClickListener(onBackClickListener);
	}

	public void setOnClickedListener(View.OnClickListener onClickedListener) {
		binding.getRoot().setOnClickListener(onClickedListener);
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
