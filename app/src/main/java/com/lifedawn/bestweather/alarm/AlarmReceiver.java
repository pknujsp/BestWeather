package com.lifedawn.bestweather.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.repository.AlarmRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmReceiver extends BroadcastReceiver {
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	public void onReceive(Context context, Intent intent) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				final String action = intent.getAction();
				AlarmRepository alarmRepository = new AlarmRepository(context);

				if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_ALARM))) {


				} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
					alarmRepository.getAll(new DbQueryCallback<List<AlarmDto>>() {
						@Override
						public void onResultSuccessful(List<AlarmDto> result) {
							List<AlarmDto> alarmDtos = result;
							for (AlarmDto alarmDto : alarmDtos) {
								AlarmUtil.registerAlarm(context, alarmDto);
							}
						}

						@Override
						public void onResultNoData() {

						}
					});
				}
			}
		});

	}
}