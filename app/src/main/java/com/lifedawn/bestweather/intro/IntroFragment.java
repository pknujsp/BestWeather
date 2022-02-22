package com.lifedawn.bestweather.intro;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.FragmentIntroBinding;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.main.MainTransactionFragment;
import com.lifedawn.bestweather.main.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class IntroFragment extends Fragment {
	private FragmentIntroBinding binding;
	private FusedLocation fusedLocation;
	private SharedPreferences sharedPreferences;
	private LocationLifeCycleObserver locationLifeCycleObserver;
	private AlertDialog dialog;

	private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof FindAddressFragment) {
				if (((FindAddressFragment) f).isSelectedAddress()) {
					MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
					getParentFragment().getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, mainTransactionFragment,
							mainTransactionFragment.getTag()).commitAllowingStateLoss();
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fusedLocation = FusedLocation.getInstance(getContext());
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);
		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentIntroBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.getRoot().setPadding(0, MyApplication.getStatusBarHeight(), 0, 0);

		binding.useCurrentLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog = ProgressDialog.show(requireActivity(), getString(R.string.msg_finding_current_location), null);
				fusedLocation.findCurrentLocation(locationCallback, false);
			}
		});
		binding.findAddress.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FindAddressFragment findAddressFragment = new FindAddressFragment();
				Bundle bundle = new Bundle();
				bundle.putString(BundleKey.RequestFragment.name(), IntroFragment.class.getName());
				findAddressFragment.setArguments(bundle);

				findAddressFragment.setOnResultFragmentListener(new OnResultFragmentListener() {
					@Override
					public void onResultFragment(Bundle result) {
						final boolean isSelectedNewAddress = result.getBoolean(BundleKey.SelectedAddressDto.name());

						if (isSelectedNewAddress) {
							final int newFavoriteAddressDtoId = result.getInt(BundleKey.newFavoriteAddressDtoId.name());
							sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id),
									newFavoriteAddressDtoId).putString(getString(R.string.pref_key_last_selected_location_type),
									LocationType.SelectedAddress.name()).putBoolean(getString(R.string.pref_key_show_intro), false).commit();
						} else {

						}
					}
				});

				getParentFragmentManager().beginTransaction().hide(IntroFragment.this).add(R.id.fragment_container, findAddressFragment,
						getString(R.string.tag_find_address_fragment)).addToBackStack(getString(R.string.tag_find_address_fragment)).commit();
			}
		});
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
			final Location location = locationResult.getLocations().get(0);
			final Double latitude = location.getLatitude();
			final Double longitude = location.getLongitude();

			sharedPreferences.edit().putString(getString(R.string.pref_key_last_current_location_latitude), latitude.toString()).putString(
					getString(R.string.pref_key_last_current_location_longitude), longitude.toString()).putString(
					getString(R.string.pref_key_last_selected_location_type), LocationType.CurrentLocation.name()).putBoolean(
					getString(R.string.pref_key_show_intro), false).commit();

			dialog.dismiss();

			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			getParentFragment().getParentFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, mainTransactionFragment,
							mainTransactionFragment.getTag()).commitAllowingStateLoss();
		}

		@Override
		public void onFailed(Fail fail) {
			dialog.dismiss();

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
				Toast.makeText(getContext(), R.string.failedFindingLocation, Toast.LENGTH_SHORT).show();
			}

		}
	};


}