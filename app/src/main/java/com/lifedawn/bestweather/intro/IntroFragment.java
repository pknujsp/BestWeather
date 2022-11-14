package com.lifedawn.bestweather.intro;

import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.FragmentIntroBinding;
import com.lifedawn.bestweather.findaddress.map.MapFragment;
import com.lifedawn.bestweather.main.InitViewModel;
import com.lifedawn.bestweather.main.MainTransactionFragment;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;


public class IntroFragment extends Fragment {
	private FragmentIntroBinding binding;
	private InitViewModel initViewModel;
	private FusedLocation fusedLocation;
	private LocationLifeCycleObserver locationLifeCycleObserver;

	private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof MapFragment) {

			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViewModel = new ViewModelProvider(requireActivity()).get(InitViewModel.class);

		fusedLocation = new FusedLocation(requireContext().getApplicationContext());
		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);
		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentIntroBinding.inflate(inflater, container, false);
		binding.getRoot().setPadding(0, MyApplication.getStatusBarHeight(), 0, 0);

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.closeBtn.setOnClickListener(v -> {
			requireActivity().finish();
		});

		binding.useCurrentLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ProgressDialog.show(requireActivity(), getString(R.string.msg_finding_current_location), null);
				fusedLocation.findCurrentLocation(locationCallback, false);
			}
		});

		binding.findAddress.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MapFragment mapFragment = new MapFragment();
				Bundle bundle = new Bundle();
				bundle.putString(BundleKey.RequestFragment.name(), IntroFragment.class.getName());
				mapFragment.setArguments(bundle);

				mapFragment.setOnResultFavoriteListener(new MapFragment.OnResultFavoriteListener() {
					@Override
					public void onAddedNewAddress(FavoriteAddressDto newFavoriteAddressDto, List<FavoriteAddressDto> favoriteAddressDtoList, boolean removed) {
						getParentFragmentManager().popBackStackImmediate();
						final int newFavoriteAddressDtoId = newFavoriteAddressDto.getId();

						PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id),
								newFavoriteAddressDtoId).putString(getString(R.string.pref_key_last_selected_location_type),
								LocationType.SelectedAddress.name()).putBoolean(getString(R.string.pref_key_show_intro), false).commit();

						MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
						getParentFragment().getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, mainTransactionFragment,
								mainTransactionFragment.getTag()).commitAllowingStateLoss();
					}

					@Override
					public void onResult(List<FavoriteAddressDto> favoriteAddressDtoList) {

					}

					@Override
					public void onClickedAddress(@Nullable FavoriteAddressDto favoriteAddressDto) {

					}
				});

				getParentFragmentManager().beginTransaction().hide(IntroFragment.this).add(R.id.fragment_container, mapFragment,
						MapFragment.class.getName()).addToBackStack(MapFragment.class.getName()).setPrimaryNavigationFragment(mapFragment).commitAllowingStateLoss();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		initViewModel.ready = true;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	@Override
	public void onDestroy() {
		getParentFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		getLifecycle().removeObserver(locationLifeCycleObserver);

		super.onDestroy();
	}

	private final FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
		@Override
		public void onSuccessful(LocationResult locationResult) {
			//현재 위치 파악 성공
			PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext())
					.edit().putBoolean(getString(R.string.pref_key_show_intro), false).commit();

			ProgressDialog.clearDialogs();

			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			getParentFragment().getParentFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, mainTransactionFragment,
							mainTransactionFragment.getTag()).commitAllowingStateLoss();
		}

		@Override
		public void onFailed(Fail fail) {
			ProgressDialog.clearDialogs();

			if (fail == Fail.DISABLED_GPS) {
				fusedLocation.onDisabledGps(requireActivity(), locationLifeCycleObserver, new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (fusedLocation.isOnGps()) {
							binding.useCurrentLocation.callOnClick();
						}
					}
				});
			} else if (fail == Fail.DENIED_LOCATION_PERMISSIONS) {
				fusedLocation.onRejectPermissions(requireActivity(), locationLifeCycleObserver, new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (fusedLocation.checkDefaultPermissions()) {
							binding.useCurrentLocation.callOnClick();
						}
					}
				}, new ActivityResultCallback<Map<String, Boolean>>() {
					@Override
					public void onActivityResult(Map<String, Boolean> result) {
						if (!result.containsValue(false)) {
							binding.useCurrentLocation.callOnClick();
						} else {

						}
					}
				});

			} else {
				//검색 실패
				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getContext(), R.string.failedFindingLocation, Toast.LENGTH_SHORT).show();
					}
				});
			}

		}
	};


}