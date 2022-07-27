package com.lifedawn.bestweather.findaddress;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.commons.views.CustomEditText;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.FragmentFindAddressBinding;
import com.lifedawn.bestweather.findaddress.map.MapFragment;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.responses.google.placesearch.GooglePlaceSearchResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import retrofit2.Response;


public class FindAddressFragment extends Fragment {
	private FragmentFindAddressBinding binding;
	private FoundAddressesAdapter addressesAdapter;
	private WeatherViewModel weatherViewModel;
	private boolean selectedAddress = false;
	private FavoriteAddressDto newFavoriteAddressDto;
	private String requestFragment;
	private OnResultFragmentListener onResultFragmentListener;
	private ExecutorService executorService;
	private FusedLocation fusedLocation;
	private NetworkStatus networkStatus;
	private LocationLifeCycleObserver locationLifeCycleObserver;
	private Bundle bundle;

	public FindAddressFragment setOnResultFragmentListener(OnResultFragmentListener onResultFragmentListener) {
		this.onResultFragmentListener = onResultFragmentListener;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		executorService = MyApplication.getExecutorService();

		bundle = getArguments() != null ? getArguments() : savedInstanceState;
		requestFragment = bundle.getString(BundleKey.RequestFragment.name());
		fusedLocation = FusedLocation.getInstance(getContext());
		networkStatus = NetworkStatus.getInstance(getContext());
		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentFindAddressBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		binding.progressResultView.setContentView(binding.addressList);
		binding.progressResultView.onFailed(getString(R.string.title_empty_locations));

		binding.toolbar.fragmentTitle.setText(R.string.find_address);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.currentLocationBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (networkStatus.networkAvailable()) {
					ProgressDialog.show(requireActivity(), getString(R.string.msg_finding_current_location), new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							fusedLocation.cancel(myLocationCallback);
						}
					});
					fusedLocation.findCurrentLocation(myLocationCallback, false);
				} else {
					Toast.makeText(getContext(), R.string.disconnected_network, Toast.LENGTH_SHORT).show();
				}
			}
		});

		binding.mapBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MapFragment mapFragment = new MapFragment();
				getParentFragmentManager().beginTransaction().hide(FindAddressFragment.this)
						.add(R.id.fragment_container, mapFragment, MapFragment.class.getName())
						.addToBackStack(MapFragment.class.getName()).commitAllowingStateLoss();
			}
		});

		addressesAdapter = new FoundAddressesAdapter();
		addressesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				if (addressesAdapter.getItemCount() == 0) {
					binding.progressResultView.onFailed(getString(R.string.title_empty_locations));
				} else {
					binding.progressResultView.onSuccessful();
				}
			}
		});
		addressesAdapter.setOnClickedAddressListener(new FoundAddressesAdapter.OnClickedAddressListener() {
			@Override
			public void onClickedAddress(Address address) {
				onClickedNewLocation(address);
			}
		});

		binding.addressList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		binding.addressList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
		binding.addressList.setAdapter(addressesAdapter);

		binding.searchView.setOnEditTextQueryListener(new CustomEditText.OnEditTextQueryListener() {
			@Override
			public void onTextChange(String newText) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						binding.progressResultView.onStarted();
					}
				});

				Geocoding.reverseGeocoding(getContext(), executorService, newText, new Geocoding.ReverseGeocodingCallback() {
					@Override
					public void onReverseGeocodingResult(List<Address> addressList) {
						if (getActivity() != null) {
							MainThreadWorker.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Log.e("address", newText);
									addressesAdapter.setItemList(addressList);
									addressesAdapter.getFilter().filter(newText);
								}
							});
						}
					}
				});

			}

			@Override
			public void onTextSubmit(String text) {
				if (text.isEmpty()) {
					Toast.makeText(getContext(), R.string.empty_search_query, Toast.LENGTH_SHORT).show();
				}
			}
		});


	}

	private void onClickedNewLocation(Address address) {
		final FavoriteAddressDto favoriteAddressDto = new FavoriteAddressDto();
		favoriteAddressDto.setCountryName(address.getCountryName());
		favoriteAddressDto.setCountryCode(address.getCountryCode());
		favoriteAddressDto.setAddress(address.getAddressLine(0));
		favoriteAddressDto.setLatitude(String.valueOf(address.getLatitude()));
		favoriteAddressDto.setLongitude(String.valueOf(address.getLongitude()));

		weatherViewModel.contains(favoriteAddressDto.getLatitude(), favoriteAddressDto.getLongitude(),
				new DbQueryCallback<Boolean>() {
					@Override
					public void onResultSuccessful(Boolean contains) {
						if (contains) {
							if (getActivity() != null) {
								selectedAddress = false;
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(getContext(), R.string.duplicate_address, Toast.LENGTH_SHORT).show();
									}
								});
							}
						} else {
							weatherViewModel.add(favoriteAddressDto, new DbQueryCallback<Long>() {
								@Override
								public void onResultSuccessful(Long id) {
									selectedAddress = true;
									favoriteAddressDto.setId(id.intValue());
									newFavoriteAddressDto = favoriteAddressDto;

									if (getActivity() != null) {
										MainThreadWorker.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(getContext(),
														address.getAddressLine(0), Toast.LENGTH_SHORT).show();
												getParentFragmentManager().popBackStack();
											}
										});
									}
								}

								@Override
								public void onResultNoData() {

								}
							});
						}
					}

					@Override
					public void onResultNoData() {

					}
				});
	}

	@Override
	public void onDestroy() {
		ProgressDialog.clearDialogs();
		getLifecycle().removeObserver(locationLifeCycleObserver);

		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.LastFragment.name(), FindAddressFragment.class.getName());
		bundle.putBoolean(BundleKey.SelectedAddressDto.name(), selectedAddress);
		if (selectedAddress) {
			PreferenceManager.getDefaultSharedPreferences(getContext())
					.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id), newFavoriteAddressDto.getId()).commit();
			bundle.putInt(BundleKey.newFavoriteAddressDtoId.name(), newFavoriteAddressDto.getId());
		}

		onResultFragmentListener.onResultFragment(bundle);
		super.onDestroy();
	}

	private final FusedLocation.MyLocationCallback myLocationCallback = new FusedLocation.MyLocationCallback() {
		@Override
		public void onSuccessful(LocationResult locationResult) {
			final Location location = getBestLocation(locationResult);
			Geocoding.geocoding(getContext(), location.getLatitude(), location.getLongitude(),
					new Geocoding.GeocodingCallback() {
						@Override
						public void onGeocodingResult(List<Address> addressList) {
							MainThreadWorker.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ProgressDialog.clearDialogs();


									if (addressList.size() > 0) {
										onClickedNewLocation(addressList.get(0));
									} else {
										Toast.makeText(getContext(), R.string.failedFindingLocation, Toast.LENGTH_SHORT).show();
									}
								}
							});
						}
					});
		}

		@Override
		public void onFailed(Fail fail) {
			ProgressDialog.clearDialogs();

			if (fail == Fail.DISABLED_GPS) {
				fusedLocation.onDisabledGps(requireActivity(), locationLifeCycleObserver, new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (fusedLocation.isOnGps()) {
							binding.currentLocationBtn.callOnClick();
						}
					}
				});
			} else if (fail == Fail.DENIED_LOCATION_PERMISSIONS) {
				fusedLocation.onRejectPermissions(requireActivity(), locationLifeCycleObserver, new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (fusedLocation.checkDefaultPermissions()) {
							binding.currentLocationBtn.callOnClick();
						}
					}
				}, new ActivityResultCallback<Map<String, Boolean>>() {
					@Override
					public void onActivityResult(Map<String, Boolean> result) {
						if (!result.containsValue(false)) {
							binding.currentLocationBtn.callOnClick();
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

	public boolean isSelectedAddress() {
		return selectedAddress;
	}

	public FavoriteAddressDto getNewFavoriteAddressDto() {
		return newFavoriteAddressDto;
	}
}