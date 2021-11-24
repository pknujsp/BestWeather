package com.lifedawn.bestweather.findaddress;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.views.CustomEditText;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.FragmentFindAddressBinding;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FindAddressFragment extends Fragment {
	private FragmentFindAddressBinding binding;
	private FoundAddressesAdapter addressesAdapter;
	private WeatherViewModel weatherViewModel;
	private boolean selectedAddress = false;
	private FavoriteAddressDto newFavoriteAddressDto;
	private String fragmentRequestKey;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

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
		getParentFragmentManager().setFragmentResultListener(getString(R.string.key_from_intro_to_find_address), this,
				new FragmentResultListener() {
					@Override
					public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
						fragmentRequestKey = getString(R.string.key_back_from_find_address_to_intro);
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
		binding.progressResultView.setContentView(binding.addressList);
		binding.progressResultView.onFailedProcessingData(getString(R.string.title_empty_locations));

		binding.toolbar.fragmentTitle.setText(R.string.find_address);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		addressesAdapter = new FoundAddressesAdapter();
		addressesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				if (addressesAdapter.getItemCount() == 0) {
					binding.progressResultView.onFailedProcessingData(getString(R.string.title_empty_locations));
				} else {
					binding.progressResultView.onSuccessfulProcessingData();
				}
			}
		});
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

		binding.searchView.setOnEditTextQueryListener(new CustomEditText.OnEditTextQueryListener() {
			@Override
			public void onTextChange(String newText) {

				Geocoding.reverseGeocoding(getContext(), executorService, newText, new Geocoding.ReverseGeocodingCallback() {
					@Override
					public void onReverseGeocodingResult(List<Address> addressList) {
						if (getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Log.e("address", newText);
									addressesAdapter.setAddressList(addressList);
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

	@Override
	public void onDestroy() {
		Bundle bundle = new Bundle();
		bundle.putBoolean(getString(R.string.bundle_key_selected_address_dto), selectedAddress);
		if (selectedAddress) {
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