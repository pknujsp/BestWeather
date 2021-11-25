package com.lifedawn.bestweather.intro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentIntroBinding;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.main.MainTransactionFragment;

import org.jetbrains.annotations.NotNull;


public class IntroFragment extends Fragment {
	private FragmentIntroBinding binding;
	private Gps gps;
	private SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gps = new Gps(requestOnGpsLauncher, requestLocationPermissionLauncher,
				moveToAppDetailSettingsLauncher);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentIntroBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.startApp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				gps.runGps(requireActivity(), locationCallback);
			}
		});
	}

	private final ActivityResultLauncher<Intent> requestOnGpsLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					//gps 사용확인 화면에서 나온뒤 현재 위치 다시 파악
					LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
					boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

					if (isGpsEnabled) {
						binding.startApp.callOnClick();
					} else {
						locationCallback.onFailed(Gps.LocationCallback.Fail.DISABLED_GPS);
					}
				}
			});

	private final ActivityResultLauncher<Intent> moveToAppDetailSettingsLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					if (ContextCompat.checkSelfPermission(getContext(),
							Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
						sharedPreferences.edit().putBoolean(
								getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false).apply();
						binding.startApp.callOnClick();
					} else {
						locationCallback.onFailed(Gps.LocationCallback.Fail.REJECT_PERMISSION);
					}

				}
			});

	private final ActivityResultLauncher<String> requestLocationPermissionLauncher = registerForActivityResult(
			new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
				@Override
				public void onActivityResult(Boolean isGranted) {
					//gps사용 권한
					//허가남 : 현재 위치 다시 파악
					//거부됨 : 작업 취소
					//계속 거부 체크됨 : 작업 취소
					if (isGranted) {
						sharedPreferences.edit().putBoolean(
								getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false).apply();
						binding.startApp.callOnClick();
					} else {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
							sharedPreferences.edit().putBoolean(
									getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), true).apply();
						}
						locationCallback.onFailed(Gps.LocationCallback.Fail.REJECT_PERMISSION);
					}
				}
			});

	private final Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
		@Override
		public void onSuccessful(Location location) {
			//현재 위치 파악 성공
			Double latitude = location.getLatitude();
			Double longitude = location.getLongitude();

			sharedPreferences.edit().putString(getString(R.string.pref_key_last_current_location_latitude), latitude.toString()).putString(
					getString(R.string.pref_key_last_current_location_longitude), longitude.toString()).putString(
					getString(R.string.pref_key_last_selected_location_type), LocationType.CurrentLocation.name()).putBoolean(
					getString(R.string.pref_key_show_intro), false).apply();

			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, mainTransactionFragment,
					mainTransactionFragment.getTag()).commit();
		}

		@Override
		public void onFailed(Fail fail) {
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
								LocationType.SelectedAddress.name()).putBoolean(getString(R.string.pref_key_show_intro), false).apply();

						MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
						getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, mainTransactionFragment,
								mainTransactionFragment.getTag()).commit();
					} else {

					}
				}
			});

			getParentFragmentManager().beginTransaction().hide(IntroFragment.this).add(R.id.fragment_container, findAddressFragment,
					getString(R.string.tag_find_address_fragment)).addToBackStack(getString(R.string.tag_find_address_fragment)).commit();
		}
	};
}