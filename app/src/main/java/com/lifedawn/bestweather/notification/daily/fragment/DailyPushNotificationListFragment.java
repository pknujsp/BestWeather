package com.lifedawn.bestweather.notification.daily.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.lifedawn.bestweather.notification.daily.viewmodel.DailyNotificationViewModel;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class DailyPushNotificationListFragment extends Fragment {
	private FragmentDailyPushNotificationListBinding binding;
	private NotificationListAdapter adapter;
	private final DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a h:mm");
	private DailyPushNotificationRepository repository;
	private DailyNotificationHelper dailyNotificationHelper;
	private DailyNotificationViewModel dailyNotificationViewModel;
	private final Comparator<DailyPushNotificationDto> sortComparator = new Comparator<DailyPushNotificationDto>() {
		@Override
		public int compare(DailyPushNotificationDto o1, DailyPushNotificationDto o2) {
			return LocalTime.parse(o1.getAlarmClock()).compareTo(LocalTime.parse(o2.getAlarmClock()));
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = DailyPushNotificationRepository.getINSTANCE();
		dailyNotificationHelper = new DailyNotificationHelper(requireContext().getApplicationContext());
		dailyNotificationViewModel = new ViewModelProvider(this).get(DailyNotificationViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentDailyPushNotificationListBinding.inflate(inflater);

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		binding.progressResultView.setContentView(binding.notificationList);
		binding.toolbar.fragmentTitle.setText(R.string.daily_notification);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStack();
			}
		});

		binding.notificationList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);


		binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);

				binding.notificationList.addItemDecoration(new RecyclerViewItemDecoration(requireContext().getApplicationContext(), true,
						binding.getRoot().getHeight() - binding.addBtn.getTop() - binding.addBtn.getHeight() / 2));
			}
		});


		adapter = new NotificationListAdapter(requireContext().getApplicationContext(), new OnClickedListViewItemListener<DailyPushNotificationDto>() {
			@Override
			public void onClickedItem(DailyPushNotificationDto e) {
				DailyNotificationSettingsFragment settingsFragment = new DailyNotificationSettingsFragment();
				String tag = DailyNotificationSettingsFragment.class.getName();

				Bundle bundle = new Bundle();
				bundle.putSerializable("dto", e);
				bundle.putBoolean(BundleKey.NewSession.name(), false);
				settingsFragment.setArguments(bundle);

				getParentFragmentManager().beginTransaction().hide(DailyPushNotificationListFragment.this).add(R.id.fragment_container,
						settingsFragment, tag).addToBackStack(tag).setPrimaryNavigationFragment(settingsFragment).commitAllowingStateLoss();
			}
		}, new OnCheckedSwitchInListListener<DailyPushNotificationDto>() {
			@Override
			public void onCheckedSwitch(DailyPushNotificationDto dailyPushNotificationDto, boolean isChecked) {
				dailyPushNotificationDto.setEnabled(isChecked);
				repository.update(dailyPushNotificationDto, null);
				dailyNotificationHelper.modifyPushNotification(dailyPushNotificationDto);

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
										repository.delete(e, null);
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

		dailyNotificationViewModel.listLiveData.observe(getViewLifecycleOwner(), new Observer<List<DailyPushNotificationDto>>() {
			@Override
			public void onChanged(List<DailyPushNotificationDto> result) {
				Collections.sort(result, sortComparator);
				adapter.setNotificationList(result);
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}


	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
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
			return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_daily_push_notification_item, parent, false));
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
			private boolean init = true;

			public ViewHolder(@NonNull @NotNull View itemView) {
				super(itemView);
				binding = ViewDailyPushNotificationItemBinding.bind(itemView);

				binding.notiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (init) {
							return;
						}
						onCheckedSwitchInListListener.onCheckedSwitch(notificationList.get(getBindingAdapterPosition()), isChecked);
					}

				});

				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedItemListener.onClickedItem(notificationList.get(getBindingAdapterPosition()));
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
										onClickedPopupMenuItemListener.onClickedItem(notificationList.get(getBindingAdapterPosition()), getBindingAdapterPosition());
								}
								return true;
							}
						});

						popupMenu.show();
					}
				});
			}

			public void onBind() {
				init = true;
				final int pos = getBindingAdapterPosition();
				DailyPushNotificationDto dto = notificationList.get(pos);

				binding.hours.setText(LocalTime.parse(dto.getAlarmClock()).format(hoursFormatter));
				binding.notiSwitch.setChecked(dto.isEnabled());

				binding.notificationType.setText(DailyPushNotificationType.getNotificationName(dto.getNotificationType(), context));

				String addressName = dto.getLocationType() == LocationType.SelectedAddress ? dto.getAddressName() :
						context.getString(R.string.current_location);
				binding.location.setText(addressName);
				init = false;
			}

		}
	}
}