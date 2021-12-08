package com.lifedawn.bestweather.alarm;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.interfaces.OnCheckedSwitchInListListener;
import com.lifedawn.bestweather.commons.interfaces.OnClickedListViewItemListener;
import com.lifedawn.bestweather.databinding.FragmentAlarmListBinding;
import com.lifedawn.bestweather.databinding.ViewAlarmItemBinding;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.repository.AlarmRepository;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;


public class AlarmListFragment extends Fragment {
	private FragmentAlarmListBinding binding;
	private AlarmRepository alarmRepository;
	private AlarmListAdapter alarmListAdapter;
	private DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a hh:mm");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		alarmRepository = new AlarmRepository(getContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAlarmListBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.alarm);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.alarmList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		binding.alarmList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
		alarmListAdapter = new AlarmListAdapter(getContext(), new OnClickedListViewItemListener<AlarmDto>() {
			@Override
			public void onClickedItem(AlarmDto e) {
				AlarmSettingsFragment alarmSettingsFragment = new AlarmSettingsFragment();
				String tag = AlarmSettingsFragment.class.getName();

				Bundle bundle = new Bundle();
				bundle.putBoolean(BundleKey.addAlarmSession.name(), false);
				bundle.putInt(BundleKey.dtoId.name(), e.getId());
				alarmSettingsFragment.setArguments(bundle);

				getParentFragmentManager().beginTransaction().hide(AlarmListFragment.this).add(R.id.fragment_container,
						alarmSettingsFragment, tag)
						.addToBackStack(tag).commit();
			}
		}, new OnCheckedSwitchInListListener<AlarmDto>() {
			@Override
			public void onCheckedSwitch(AlarmDto alarmDto, boolean isChecked) {
				alarmDto.setEnabled(isChecked ? 1 : 0);
				alarmRepository.update(alarmDto, new DbQueryCallback<AlarmDto>() {
					@Override
					public void onResultSuccessful(AlarmDto result) {
						AlarmUtil.modifyAlarm(getContext(), alarmDto);
					}

					@Override
					public void onResultNoData() {

					}
				});

				if (isChecked) {
					String text = LocalTime.parse(alarmDto.getAlarmTime()).format(hoursFormatter) + ", " + getString(R.string.registeredAlarm);
					Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
				}
			}
		});
		binding.alarmList.setAdapter(alarmListAdapter);

		binding.addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmSettingsFragment alarmSettingsFragment = new AlarmSettingsFragment();
				String tag = AlarmSettingsFragment.class.getName();

				Bundle bundle = new Bundle();
				bundle.putBoolean(BundleKey.addAlarmSession.name(), true);
				alarmSettingsFragment.setArguments(bundle);

				getParentFragmentManager().beginTransaction().hide(AlarmListFragment.this).add(R.id.fragment_container,
						alarmSettingsFragment, tag)
						.addToBackStack(tag).commit();
			}
		});
		loadAlarmList();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			loadAlarmList();
		}
	}

	private void loadAlarmList() {
		alarmRepository.getAll(new DbQueryCallback<List<AlarmDto>>() {
			@Override
			public void onResultSuccessful(List<AlarmDto> result) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							alarmListAdapter.setAlarmDtoList(result);
							alarmListAdapter.notifyDataSetChanged();
						}
					});
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	public static class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {
		private List<AlarmDto> alarmDtoList = new ArrayList<>();
		private Context context;
		private OnClickedListViewItemListener<AlarmDto> onClickedItemListener;
		private OnCheckedSwitchInListListener<AlarmDto> onCheckedSwitchInListListener;
		private DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a hh:mm");

		public AlarmListAdapter(Context context, OnClickedListViewItemListener<AlarmDto> onClickedItemListener,
		                        OnCheckedSwitchInListListener<AlarmDto> onCheckedSwitchInListListener) {
			this.onClickedItemListener = onClickedItemListener;
			this.context = context;
			this.onCheckedSwitchInListListener = onCheckedSwitchInListListener;
		}

		public void setAlarmDtoList(List<AlarmDto> alarmDtoList) {
			this.alarmDtoList = alarmDtoList;
		}

		@NonNull
		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_alarm_item, null, false));
		}

		@Override
		public void onBindViewHolder(@NonNull @NotNull AlarmListFragment.AlarmListAdapter.ViewHolder holder, int position) {
			holder.onBind();
		}

		@Override
		public int getItemCount() {
			return alarmDtoList.size();
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			private ViewAlarmItemBinding binding;

			public ViewHolder(@NonNull @NotNull View itemView) {
				super(itemView);
				binding = ViewAlarmItemBinding.bind(itemView);
			}

			public void onBind() {
				AlarmDto alarmDto = alarmDtoList.get(getAdapterPosition());
				binding.alarmSwitch.setChecked(alarmDto.getEnabled() == 1);
				binding.hours.setText(LocalTime.parse(alarmDto.getAlarmTime()).format(hoursFormatter));

				Set<Integer> days = AlarmUtil.parseDays(alarmDto.getAlarmDays());

				for (Integer d : days) {
					if (d == Calendar.SUNDAY) {
						binding.sun.setTextColor(ContextCompat.getColor(context, R.color.alarmEnabledDayHighlightColor));
						binding.sun.setTypeface(Typeface.DEFAULT_BOLD);
					} else if (d == Calendar.MONDAY) {
						binding.mon.setTextColor(ContextCompat.getColor(context, R.color.alarmEnabledDayHighlightColor));
						binding.mon.setTypeface(Typeface.DEFAULT_BOLD);
					} else if (d == Calendar.TUESDAY) {
						binding.tue.setTextColor(ContextCompat.getColor(context, R.color.alarmEnabledDayHighlightColor));
						binding.tue.setTypeface(Typeface.DEFAULT_BOLD);
					} else if (d == Calendar.WEDNESDAY) {
						binding.wed.setTextColor(ContextCompat.getColor(context, R.color.alarmEnabledDayHighlightColor));
						binding.wed.setTypeface(Typeface.DEFAULT_BOLD);
					} else if (d == Calendar.THURSDAY) {
						binding.thu.setTextColor(ContextCompat.getColor(context, R.color.alarmEnabledDayHighlightColor));
						binding.thu.setTypeface(Typeface.DEFAULT_BOLD);
					} else if (d == Calendar.FRIDAY) {
						binding.fri.setTextColor(ContextCompat.getColor(context, R.color.alarmEnabledDayHighlightColor));
						binding.fri.setTypeface(Typeface.DEFAULT_BOLD);
					} else if (d == Calendar.SATURDAY) {
						binding.sat.setTextColor(ContextCompat.getColor(context, R.color.alarmEnabledDayHighlightColor));
						binding.sat.setTypeface(Typeface.DEFAULT_BOLD);
					}
				}

				binding.location.setVisibility(alarmDto.getAddedLocation() == 1 ? View.VISIBLE : View.GONE);
				binding.location.setText(alarmDto.getLocationAddressName());

				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedItemListener.onClickedItem(alarmDtoList.get(getAdapterPosition()));
					}
				});
				binding.alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						onCheckedSwitchInListListener.onCheckedSwitch(alarmDtoList.get(getAdapterPosition()), isChecked);
					}
				});
			}
		}
	}
}