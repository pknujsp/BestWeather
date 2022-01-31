package com.lifedawn.bestweather.alert;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentAlertBinding;

public class AlertFragment extends Fragment {
	private FragmentAlertBinding binding;
	private View.OnClickListener btnOnClickListener;

	public enum Constant {
		DRAWABLE_ID, MESSAGE_TEXT, BTN_TEXT
	}

	public void setBtnOnClickListener(View.OnClickListener btnOnClickListener) {
		this.btnOnClickListener = btnOnClickListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAlertBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnOnClickListener != null) {
					btnOnClickListener.onClick(v);
				}
			}
		});

		Bundle bundle = getArguments();
		final int drawableId = bundle.getInt(Constant.DRAWABLE_ID.name());
		final String messageText = bundle.getString(Constant.MESSAGE_TEXT.name());
		final String btnText = bundle.getString(Constant.BTN_TEXT.name());

		binding.alertImageView.setImageResource(drawableId);
		binding.textView.setText(messageText);
		binding.btn.setText(btnText);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}