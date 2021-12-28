package com.lifedawn.bestweather.notification.daily.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.RecyclerViewItemDecoration;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.interfaces.OnCheckedSwitchInListListener;
import com.lifedawn.bestweather.commons.interfaces.OnClickedListViewItemListener;
import com.lifedawn.bestweather.databinding.FragmentDailyPushNotificationListBinding;
import com.lifedawn.bestweather.databinding.ViewDailyPushNotificationItemBinding;
import com.lifedawn.bestweather.notification.daily.DailyNotiHelper;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class DailyPushNotificationListFragment extends Fragment {
	private FragmentDailyPushNotificationListBinding binding;
	private NotificationListAdapter adapter;
	private DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a hh:mm");
	private DailyPushNotificationRepository repository;
	private DailyNotiHelper dailyNotiHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = new DailyPushNotificationRepository(getContext());
		dailyNotiHelper = new DailyNotiHelper(getContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentDailyPushNotificationListBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.daily_notification);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.notificationList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		binding.notificationList.addItemDecoration(new RecyclerViewItemDecoration(getContext()));
		adapter = new NotificationListAdapter(getContext(), new OnClickedListViewItemListener<DailyPushNotificationDto>() {
			@Override
			public void onClickedItem(DailyPushNotificationDto e) {
				DailyNotificationSettingsFragment settingsFragment = new DailyNotificationSettingsFragment();
				String tag = DailyNotificationSettingsFragment.class.getName();

				Bundle bundle = new Bundle();
				bundle.putSerializable("dto", e);
				bundle.putBoolean(BundleKey.NewSession.name(), false);
				settingsFragment.setArguments(bundle);

				getParentFragmentManager().beginTransaction().hide(DailyPushNotificationListFragment.this).add(R.id.fragment_container,
						settingsFragment, tag)
						.addToBackStack(tag).commit();
			}
		}, new OnCheckedSwitchInListListener<DailyPushNotificationDto>() {
			@Override
			public void onCheckedSwitch(DailyPushNotificationDto dailyPushNotificationDto, boolean isChecked) {
				dailyPushNotificationDto.setEnabled(isChecked);
				repository.update(dailyPushNotificationDto, new DbQueryCallback<DailyPushNotificationDto>() {
					@Override
					public void onResultSuccessful(DailyPushNotificationDto result) {
						dailyNotiHelper.modifyPushNotification(result);
					}

					@Override
					public void onResultNoData() {

					}
				});

				if (isChecked) {
					String text =
							LocalTime.parse(dailyPushNotificationDto.getAlarmClock()).format(hoursFormatter) + ", " + getString(R.string.registeredDailyNotification);
					Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
				}
			}
		});
		binding.notificationList.setAdapter(adapter);

		binding.addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DailyNotificationSettingsFragment settingsFragment = new DailyNotificationSettingsFragment();
				String tag = DailyNotificationSettingsFragment.class.getName();

				Bundle bundle = new Bundle();
				bundle.putBoolean(BundleKey.NewSession.name(), true);
				settingsFragment.setArguments(bundle);

				getParentFragmentManager().beginTransaction().hide(DailyPushNotificationListFragment.this).add(R.id.fragment_container,
						settingsFragment, tag).addToBackStack(tag).commit();
			}
		});
		loadList();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			loadList();
		}
	}

	private void loadList() {
		repository.getAll(new DbQueryCallback<List<DailyPushNotificationDto>>() {
			@Override
			public void onResultSuccessful(List<DailyPushNotificationDto> result) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							adapter.setNotificationList(result);
							adapter.notifyDataSetChanged();
						}
					});
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	public static class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder> {
		private List<DailyPushNotificationDto> notificationList = new ArrayList<>();
		private Context context;
		private OnClickedListViewItemListener<DailyPushNotificationDto> onClickedItemListener;
		private OnCheckedSwitchInListListener<DailyPushNotificationDto> onCheckedSwitchInListListener;
		private final DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a hh:mm");

		public NotificationListAdapter(Context context, OnClickedListViewItemListener<DailyPushNotificationDto> onClickedItemListener,
		                               OnCheckedSwitchInListListener<DailyPushNotificationDto> onCheckedSwitchInListListener) {
			this.onClickedItemListener = onClickedItemListener;
			this.context = context;
			this.onCheckedSwitchInListListener = onCheckedSwitchInListListener;
		}

		public void setNotificationList(List<DailyPushNotificationDto> notificationList) {
			this.notificationList = notificationList;
		}

		@NonNull
		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_daily_push_notification_item, null, false));
		}

		@Override
		public void onBindViewHolder(@NonNull @NotNull DailyPushNotificationListFragment.NotificationListAdapter.ViewHolder holder, int position) {
			holder.onBind();
		}

		@Override
		public int getItemCount() {
			return notificationList.size();
		}

		private class ViewHolder extends RecyclerView.ViewHolder {
			private ViewDailyPushNotificationItemBinding binding;

			public ViewHolder(@NonNull @NotNull View itemView) {
				super(itemView);
				binding = ViewDailyPushNotificationItemBinding.bind(itemView);
			}

			public void onBind() {
				DailyPushNotificationDto dto = notificationList.get(getAdapterPosition());
				binding.hours.setText(LocalTime.parse(dto.getAlarmClock()).format(hoursFormatter));
				binding.notiSwitch.setChecked(dto.isEnabled());

				binding.location.setVisibility(dto.getLocationType() == LocationType.SelectedAddress ? View.VISIBLE : View.GONE);
				binding.location.setText(dto.getAddressName());

				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedItemListener.onClickedItem(notificationList.get(getAdapterPosition()));
					}
				});
				binding.notiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						onCheckedSwitchInListListener.onCheckedSwitch(notificationList.get(getAdapterPosition()), isChecked);
					}
				});
			}
		}
	}
}