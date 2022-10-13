package com.lifedawn.bestweather.alert;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.views.HeaderbarStyle;
import com.lifedawn.bestweather.databinding.FragmentAlertBinding;
import com.lifedawn.bestweather.main.MyApplication;

import java.util.List;

public class AlertFragment extends Fragment {
	private FragmentAlertBinding binding;
	private List<BtnObj> btnObjList;
	private Bundle bundle;

	private View.OnClickListener menuOnClickListener;

	public AlertFragment setMenuOnClickListener(View.OnClickListener menuOnClickListener) {
		this.menuOnClickListener = menuOnClickListener;
		return this;
	}

	public enum Constant {
		DRAWABLE_ID, MESSAGE
	}

	public void setBtnObjList(List<BtnObj> btnObjList) {
		this.btnObjList = btnObjList;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAlertBinding.inflate(inflater);

		binding.mainToolbar.gps.setVisibility(View.GONE);
		binding.mainToolbar.find.setVisibility(View.GONE);
		binding.mainToolbar.refresh.setVisibility(View.GONE);

		final int statusBarHeight = MyApplication.getStatusBarHeight();

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.mainToolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = statusBarHeight;
		binding.mainToolbar.getRoot().setLayoutParams(layoutParams);

		binding.mainToolbar.openNavigationDrawer.setOnClickListener(menuOnClickListener);

		return binding.getRoot();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final int drawableId = bundle.getInt(Constant.DRAWABLE_ID.name());
		final String message = bundle.getString(Constant.MESSAGE.name());

		binding.alertImageView.setImageResource(drawableId);
		binding.textView.setText(message);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());

		for (BtnObj btnObj : btnObjList) {
			Button button = new Button(getContext());
			button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_btn_background));
			button.setTextColor(Color.WHITE);
			button.setLayoutParams(layoutParams);
			button.setText(btnObj.text);
			button.setOnClickListener(btnObj.onClickListener);

			binding.btnList.addView(button);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public static class BtnObj {
		final View.OnClickListener onClickListener;
		final String text;

		public BtnObj(View.OnClickListener onClickListener, String text) {
			this.onClickListener = onClickListener;
			this.text = text;
		}
	}
}