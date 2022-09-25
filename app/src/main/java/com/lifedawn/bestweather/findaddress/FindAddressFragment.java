package com.lifedawn.bestweather.findaddress;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.views.CustomEditText;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.FragmentFindAddressBinding;
import com.lifedawn.bestweather.findaddress.map.BottomSheetType;
import com.lifedawn.bestweather.findaddress.map.IBottomSheetState;
import com.lifedawn.bestweather.findaddress.map.MapFragment;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;


public class FindAddressFragment extends Fragment {
	private FragmentFindAddressBinding binding;
	private FoundAddressesAdapter addressesAdapter;
	private ExecutorService executorService;
	private FusedLocation fusedLocation;
	private NetworkStatus networkStatus;
	private LocationLifeCycleObserver locationLifeCycleObserver;
	private Bundle bundle;
	private OnAddressListListener onAddressListListener;
	private OnListListener onListListener;
	private FoundAddressesAdapter.OnClickedAddressListener onClickedAddressListener;
	private IBottomSheetState iBottomSheetState;
	private WeatherViewModel weatherViewModel;

	public FindAddressFragment setiBottomSheetState(IBottomSheetState iBottomSheetState) {
		this.iBottomSheetState = iBottomSheetState;
		return this;
	}

	public FindAddressFragment setOnClickedAddressListener(FoundAddressesAdapter.OnClickedAddressListener onClickedAddressListener) {
		this.onClickedAddressListener = onClickedAddressListener;
		return this;
	}

	public FindAddressFragment setOnListListener(OnListListener onListListener) {
		this.onListListener = onListListener;
		return this;
	}

	public FindAddressFragment setOnAddressListListener(OnAddressListListener onAddressListListener) {
		this.onAddressListListener = onAddressListListener;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		executorService = MyApplication.getExecutorService();
		bundle = getArguments() != null ? getArguments() : savedInstanceState;

		fusedLocation = FusedLocation.getInstance(getContext());
		networkStatus = NetworkStatus.getInstance(getContext());
		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);
		weatherViewModel = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);
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

		binding.progressResultView.setContentView(binding.addressList);
		binding.progressResultView.onFailed(getString(R.string.title_empty_locations));

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


		binding.searchView.setOnEditTextQueryListener(new CustomEditText.OnEditTextQueryListener() {
			@Override
			public void onTextChange(String newText) {
				MainThreadWorker.runOnUiThread(new Runnable() {
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


		weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				Set<String> favoriteAddressSet = new HashSet<>();

				for (FavoriteAddressDto favoriteAddressDto : result) {
					favoriteAddressSet.add(favoriteAddressDto.getLatitude() + favoriteAddressDto.getLongitude());
				}

				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
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
								onClickedAddressListener.onClickedAddress(address);
							}
						});

						addressesAdapter.setOnAddressListListener(new OnAddressListListener() {
							@Override
							public void onSearchedAddressList(List<Address> addressList) {
								onAddressListListener.onSearchedAddressList(addressList);
							}
						});

						addressesAdapter.setFavoriteAddressSet(favoriteAddressSet);

						addressesAdapter.setOnListListener(new OnListListener() {
							@Override
							public void onPOIItemSelectedByList(int position) {
								iBottomSheetState.setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_COLLAPSED);
								getParentFragmentManager().beginTransaction().hide(FindAddressFragment.this).addToBackStack(null).commit();
								onListListener.onPOIItemSelectedByList(position);
							}

							@Override
							public void onPOIItemSelectedByBottomSheet(int position, MapFragment.MarkerType markerType) {

							}
						});

						binding.addressList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
						binding.addressList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
						binding.addressList.setAdapter(addressesAdapter);
					}
				});
			}

			@Override
			public void onResultNoData() {

			}
		});

	}

	@Override
	public void onStart() {
		super.onStart();
		binding.searchView.requestFocusEditText();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (hidden) {

		} else {
			iBottomSheetState.setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED);
			iBottomSheetState.setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_EXPANDED);
		}
	}

	@Override
	public void onDestroy() {
		ProgressDialog.clearDialogs();
		getLifecycle().removeObserver(locationLifeCycleObserver);
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
										onClickedAddressListener.onClickedAddress(addressList.get(0));
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

	public interface OnAddressListListener {
		void onSearchedAddressList(List<Address> addressList);
	}

	public interface OnListListener {
		void onPOIItemSelectedByList(int position);

		void onPOIItemSelectedByBottomSheet(int position, MapFragment.MarkerType markerType);
	}
}