package com.lifedawn.bestweather.alarm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentAlarmListBinding;
import com.lifedawn.bestweather.databinding.ViewAlarmItemBinding;
import com.lifedawn.bestweather.notification.NotificationFragment;

import org.jetbrains.annotations.NotNull;


public class AlarmListFragment extends Fragment {
	private FragmentAlarmListBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		binding.addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmSettingsFragment alarmSettingsFragment = new AlarmSettingsFragment();
				String tag = AlarmSettingsFragment.class.getName();

				getParentFragmentManager().beginTransaction().hide(AlarmListFragment.this).add(R.id.fragment_container,
						alarmSettingsFragment, tag)
						.addToBackStack(tag).commit();
			}
		});
	}

	public static class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

		@NonNull
		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_alarm_item, null, false));
		}

		@Override
		public void onBindViewHolder(@NonNull @NotNull AlarmListFragment.AlarmListAdapter.ViewHolder holder, int position) {

		}

		@Override
		public int getItemCount() {
			return 0;
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			private ViewAlarmItemBinding binding;

			public ViewHolder(@NonNull @NotNull View itemView) {
				super(itemView);
				binding = ViewAlarmItemBinding.bind(itemView);
			}

			public void onBind() {

			}
		}
	}
}