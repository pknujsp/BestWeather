package com.lifedawn.bestweather.alarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.WindowManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.alarmnotifications.AlarmOnFragment;
import com.lifedawn.bestweather.databinding.ActivityAlarmBinding;

public class AlarmActivity extends AppCompatActivity {
	private ActivityAlarmBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_alarm);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
						WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

		AlarmOnFragment alarmOnFragment = new AlarmOnFragment();
		alarmOnFragment.setArguments(getIntent().getExtras());
		getSupportFragmentManager().beginTransaction()
				.add(binding.fragmentContainer.getId(), alarmOnFragment, AlarmOnFragment.class.getName()).commit();
	}
}