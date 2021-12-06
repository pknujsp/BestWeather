package com.lifedawn.bestweather.alarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.alarmnotifications.AlarmOnFragment;
import com.lifedawn.bestweather.databinding.ActivityAlarmBinding;

public class AlarmActivity extends AppCompatActivity {
	private ActivityAlarmBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_alarm);

		AlarmOnFragment alarmOnFragment = new AlarmOnFragment();
		alarmOnFragment.setArguments(getIntent().getExtras());
		getSupportFragmentManager().beginTransaction()
				.add(binding.fragmentContainer.getId(), alarmOnFragment, AlarmOnFragment.class.getName()).commit();
	}
}