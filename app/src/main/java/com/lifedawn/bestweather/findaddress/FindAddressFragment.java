package com.lifedawn.bestweather.findaddress;

import android.location.Address;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.databinding.FragmentFindAddressBinding;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class FindAddressFragment extends Fragment {
	private FragmentFindAddressBinding binding;
	private FoundAddressesAdapter addressesAdapter;
	private WeatherViewModel weatherViewModel;
	private boolean selectedAddress = false;
	private FavoriteAddressDto newFavoriteAddressDto;
	private String fragmentRequestKey;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		getParentFragmentManager().setFragmentResultListener(getString(R.string.key_from_main_to_find_address), this,
				new FragmentResultListener() {
					@Override
					public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
						fragmentRequestKey = getString(R.string.key_back_from_find_address_to_main);
					}
				});
		getParentFragmentManager().setFragmentResultListener(getString(R.string.key_from_favorite_to_find_address), this,
				new FragmentResultListener() {
					@Override
					public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
						fragmentRequestKey = getString(R.string.key_back_from_find_address_to_favorite);
					}
				});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentFindAddressBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.customProgressView.setContentView(binding.addressList);
		binding.customProgressView.onSuccessfulProcessingData();

		binding.toolbar.fragmentTitle.setText(R.string.find_address);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		addressesAdapter = new FoundAddressesAdapter();
		addressesAdapter.setOnClickedAddressListener(new FoundAddressesAdapter.OnClickedAddressListener() {
			@Override
			public void onClickedAddress(Address address) {
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
										public void onResultSuccessful(Long result) {
											selectedAddress = true;
											favoriteAddressDto.setId(result.intValue());
											newFavoriteAddressDto = favoriteAddressDto;
											if (getActivity() != null) {
												getActivity().runOnUiThread(new Runnable() {
													@Override
													public void run() {
														getParentFragmentManager().popBackStackImmediate();
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
		});

		binding.addressList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		binding.addressList.setAdapter(addressesAdapter);

		binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				binding.customProgressView.onStartedProcessingData(getString(R.string.finding_address));

				Geocoding.reverseGeocoding(getContext(), query, new Geocoding.ReverseGeocodingCallback() {
					@Override
					public void onReverseGeocodingResult(List<Address> addressList) {
						addressesAdapter.setAddressList(addressList);

						if (getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									addressesAdapter.notifyDataSetChanged();

									if (addressList.isEmpty()) {
										binding.customProgressView.onFailedProcessingData(getString(R.string.not_search_result));
									} else {
										binding.customProgressView.onSuccessfulProcessingData();
									}
								}
							});
						}
					}
				});

				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
	}

	@Override
	public void onDestroy() {
		Bundle bundle = new Bundle();
		bundle.putBoolean(getString(R.string.bundle_key_selected_address), isSelectedAddress());
		if (isSelectedAddress()) {
			bundle.putInt(getString(R.string.bundle_key_new_favorite_address_dto_id), newFavoriteAddressDto.getId());
		}
		getParentFragmentManager().setFragmentResult(fragmentRequestKey, bundle);
		super.onDestroy();
	}

	public boolean isSelectedAddress() {
		return selectedAddress;
	}

	public FavoriteAddressDto getNewFavoriteAddressDto() {
		return newFavoriteAddressDto;
	}
}