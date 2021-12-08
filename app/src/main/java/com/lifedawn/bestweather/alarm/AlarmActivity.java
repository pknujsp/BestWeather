package com.lifedawn.bestweather.alarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.alarmnotifications.AlarmOnFragment;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.databinding.ActivityAlarmBinding;

import java.util.Timer;
import java.util.TimerTask;

public class AlarmActivity extends AppCompatActivity {
	private ActivityAlarmBinding binding;
	private EndAlarmReceiver endAlarmReceiver = new EndAlarmReceiver();
	private AlarmOnFragment alarmOnFragment;
	private int alarmDtoId;
	private final Timer timer = new Timer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
						WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		super.onCreate(savedInstanceState);

		binding = DataBindingUtil.setContentView(this, R.layout.activity_alarm);
		alarmDtoId = getIntent().getExtras().getInt(BundleKey.dtoId.name());

		alarmOnFragment = new AlarmOnFragment();
		alarmOnFragment.setArguments(getIntent().getExtras());
		getSupportFragmentManager().beginTransaction()
				.add(binding.fragmentContainer.getId(), alarmOnFragment, AlarmOnFragment.class.getName()).commit();

		IntentFilter intentFilter = new IntentFilter(getString(R.string.com_lifedawn_bestweather_action_END_ALARM));
		registerReceiver(endAlarmReceiver, intentFilter);

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				PendingIntent pendingIntent = AlarmReceiver.getEndAlarmPendingIntent(getApplicationContext(), alarmDtoId);
				try {
					pendingIntent.send();
				} catch (PendingIntent.CanceledException e) {
					e.printStackTrace();
				}
			}
		}, 1000 * 60 * 3);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		timer.cancel();
		unregisterReceiver(endAlarmReceiver);
	}

	private class EndAlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			finishAndRemoveTask();
		}
	}
}