package com.lifedawn.bestweather.notification.daily.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.RecyclerViewItemDecoration;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.interfaces.OnCheckedSwitchInListListener;
import com.lifedawn.bestweather.commons.interfaces.OnClickedListViewItemListener;
import com.lifedawn.bestweather.commons.interfaces.OnClickedPopupMenuItemListener;
import com.lifedawn.bestweather.databinding.FragmentDailyPushNotificationListBinding;
import com.lifedawn.bestweather.databinding.ViewDailyPushNotificationItemBinding;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.daily.DailyNotificationHelper;
import com.lifedawn.bestweather.notification.daily.DailyPushNotificationType;
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
	private DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a h:mm");
	private DailyPushNotificationRepository repository;
	private DailyNotificationHelper dailyNotificationHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = new DailyPushNotificationRepository(getContext());
		dailyNotificationHelper = new DailyNotificationHelper(getContext());
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

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		binding.progressResultView.setContentView(binding.notificationList);
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
						settingsFragment, tag).addToBackStack(tag).commit();
			}
		}, new OnCheckedSwitchInListListener<DailyPushNotificationDto>() {
			@Override
			public void onCheckedSwitch(DailyPushNotificationDto dailyPushNotificationDto, boolean isChecked) {
				dailyPushNotificationDto.setEnabled(isChecked);
				dailyNotificationHelper.modifyPushNotification(dailyPushNotificationDto);

				repository.update(dailyPushNotificationDto, null);
				String text = LocalTime.parse(dailyPushNotificationDto.getAlarmClock()).format(hoursFormatter)
						+ ", ";

				if (isChecked) {
					text += getString(R.string.registeredDailyNotification);
				} else {
					text += getString(R.string.unregisteredDailyNotification);
				}
				Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
			}
		}, new OnClickedPopupMenuItemListener<DailyPushNotificationDto>() {
			@Override
			public void onClickedItem(DailyPushNotificationDto e, int position) {
				switch (position) {
					case 0:
						new MaterialAlertDialogBuilder(requireActivity())
								.setTitle(R.string.removeNotification)
								.setMessage(R.string.will_you_delete_the_notification)
								.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dailyNotificationHelper.disablePushNotification(e);
										repository.delete(e, new DbQueryCallback<Boolean>() {
											@Override
											public void onResultSuccessful(Boolean result) {
												loadList();
											}

											@Override
											public void onResultNoData() {

											}
										});
										dialog.dismiss();
									}
								}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).create().show();
						break;
				}
			}
		});
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				if (adapter.getItemCount() > 0) {
					binding.progressResultView.onSuccessful();
				} else {
					binding.progressResultView.onFailed(getString(R.string.empty_daily_notifications));
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
		private OnClickedPopupMenuItemListener<DailyPushNotificationDto> onClickedPopupMenuItemListener;
		private final DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a hh:mm");

		public NotificationListAdapter(Context context, OnClickedListViewItemListener<DailyPushNotificationDto> onClickedItemListener,
		                               OnCheckedSwitchInListListener<DailyPushNotificationDto> onCheckedSwitchInListListener,
		                               OnClickedPopupMenuItemListener<DailyPushNotificationDto> onClickedPopupMenuItemListener) {
			this.onClickedItemListener = onClickedItemListener;
			this.context = context;
			this.onCheckedSwitchInListListener = onCheckedSwitchInListListener;
			this.onClickedPopupMenuItemListener = onClickedPopupMenuItemListener;
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
				binding.notificationType.setText(DailyPushNotificationType.getNotificationName(dto.getNotificationType(), context));

				String addressName = dto.getLocationType() == LocationType.SelectedAddress ? dto.getAddressName() :
						context.getString(R.string.current_location);
				binding.location.setText(addressName);

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
				binding.control.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						PopupMenu popupMenu = new PopupMenu(context, binding.control, Gravity.BOTTOM);

						popupMenu.getMenuInflater().inflate(R.menu.menu_of_daily_notification_item, popupMenu.getMenu());
						popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
							@SuppressLint("NonConstantResourceId")
							@Override
							public boolean onMenuItemClick(MenuItem menuItem) {
								switch (menuItem.getItemId()) {
									default:
										onClickedPopupMenuItemListener.onClickedItem(dto, 0);
								}
								return true;
							}
						});

						popupMenu.show();
					}
				});

			}

		}
	}
}