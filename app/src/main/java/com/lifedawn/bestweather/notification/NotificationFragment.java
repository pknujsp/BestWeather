package com.lifedawn.bestweather.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.IntentUtil;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.databinding.FragmentNotificationBinding;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.model.OngoingNotificationDto;
import com.lifedawn.bestweather.notification.ongoing.OngoingNotificationHelper;
import com.lifedawn.bestweather.notification.ongoing.OngoingNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.daily.fragment.DailyPushNotificationListFragment;
import com.lifedawn.bestweather.notification.ongoing.OngoingNotificationViewModel;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;

import org.jetbrains.annotations.NotNull;


public class NotificationFragment extends Fragment {
	private FragmentNotificationBinding binding;
	private OngoingNotificationViewModel ongoingNotificationViewModel;
	private boolean initializing = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ongoingNotificationViewModel = new ViewModelProvider(requireActivity()).get(OngoingNotificationViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentNotificationBinding.inflate(inflater, container, false);

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		binding.ongoing.title.setText(R.string.always_notification);
		binding.ongoing.editBtn.setText(R.string.settings);
		binding.toolbar.fragmentTitle.setText(R.string.notification);

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStack();
			}
		});

		binding.ongoing.editBtn.setOnClickListener(v -> {
			addOngoingNotificationSettingsFragment();
		});

		binding.ongoing.funcStateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				binding.ongoing.editBtn.setVisibility(isChecked ? View.VISIBLE : View.GONE);

				if (initializing) {
					initializing = false;
					return;
				}

				if (isChecked) {
					ongoingNotificationViewModel.getOngoingNotificationDto(new DbQueryCallback<OngoingNotificationDto>() {
						@Override
						public void onResultSuccessful(OngoingNotificationDto ongoingNotificationDto) {
							MainThreadWorker.runOnUiThread(() -> {
								// 저장된 알림 데이터가 있으면 알림 표시
								Context context = requireContext().getApplicationContext();
								if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
									ongoingNotificationDto.setOn(true);

									OngoingNotificationHelper helper = new OngoingNotificationHelper(context);
									PendingIntent pendingIntent =
											helper.createManualPendingIntent(getString(R.string.com_lifedawn_bestweather_action_REFRESH),
													PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

									try {
										pendingIntent.send();
									} catch (PendingIntent.CanceledException e) {
										e.printStackTrace();
									}
								} else {
									initializing = true;
									binding.ongoing.funcStateSwitch.setChecked(false);
									ongoingNotificationDto.setOn(false);

									Toast.makeText(getContext(), R.string.disabledNotification, Toast.LENGTH_SHORT).show();
									startActivity(IntentUtil.getNotificationSettingsIntent(getActivity()));
								}

								ongoingNotificationViewModel.save(ongoingNotificationDto, null);
							});
						}

						@Override
						public void onResultNoData() {
							// 저장된 알림 데이터가 없으면 알림 세부설정 화면 열기
							MainThreadWorker.runOnUiThread(NotificationFragment.this::addOngoingNotificationSettingsFragment);
						}
					});
				} else {
					NotificationHelper notificationHelper = new NotificationHelper(requireContext().getApplicationContext());
					notificationHelper.cancelNotification(NotificationType.Ongoing.getNotificationId());

					ongoingNotificationViewModel.getOngoingNotificationDto(new DbQueryCallback<OngoingNotificationDto>() {
						@Override
						public void onResultSuccessful(OngoingNotificationDto result) {
							result.setOn(false);
							ongoingNotificationViewModel.save(result, null);
						}

						@Override
						public void onResultNoData() {

						}
					});
				}
			}
		});

		binding.daily.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DailyPushNotificationListFragment listFragment = new DailyPushNotificationListFragment();
				String tag = DailyPushNotificationListFragment.class.getName();

				getParentFragmentManager().beginTransaction().hide(NotificationFragment.this).add(R.id.fragment_container,
								listFragment, tag)
						.addToBackStack(tag).commitAllowingStateLoss();
			}
		});

		loadOngoingNotificationState();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (!hidden) {
			loadOngoingNotificationState();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void loadOngoingNotificationState() {
		initializing = true;

		ongoingNotificationViewModel.getOngoingNotificationDto(new DbQueryCallback<OngoingNotificationDto>() {
			@Override
			public void onResultSuccessful(OngoingNotificationDto result) {
				MainThreadWorker.runOnUiThread(() -> {
					binding.ongoing.funcStateSwitch.setChecked(result.isOn());
					initializing = false;
				});
			}

			@Override
			public void onResultNoData() {
				MainThreadWorker.runOnUiThread(() -> {
					binding.ongoing.funcStateSwitch.setChecked(false);
					initializing = false;
				});
			}
		});
	}

	private void addOngoingNotificationSettingsFragment() {
		OngoingNotificationSettingsFragment ongoingNotificationSettingsFragment = new OngoingNotificationSettingsFragment();
		final String tag = OngoingNotificationSettingsFragment.class.getName();

		getParentFragmentManager().beginTransaction().hide(NotificationFragment.this).add(R.id.fragment_container,
				ongoingNotificationSettingsFragment,
				tag).addToBackStack(tag).commitAllowingStateLoss();
	}
}