package com.lifedawn.bestweather.findaddress;

import android.location.Address;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.commons.views.CustomEditText;
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
	private String requestFragment;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	private OnResultFragmentListener onResultFragmentListener;

	public FindAddressFragment setOnResultFragmentListener(OnResultFragmentListener onResultFragmentListener) {
		this.onResultFragmentListener = onResultFragmentListener;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);

		Bundle bundle = getArguments();
		requestFragment = bundle.getString(BundleKey.RequestFragment.name());
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
		binding.progressResultView.onFailed(getString(R.string.title_empty_locations));

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
					binding.progressResultView.onFailed(getString(R.string.title_empty_locations));
				} else {
					binding.progressResultView.onSuccessful();
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
		bundle.putString(BundleKey.LastFragment.name(), FindAddressFragment.class.getName());
		bundle.putBoolean(BundleKey.SelectedAddressDto.name(), selectedAddress);
		if (selectedAddress) {
			bundle.putInt(BundleKey.newFavoriteAddressDtoId.name(), newFavoriteAddressDto.getId());
		}

		onResultFragmentListener.onResultFragment(bundle);
		super.onDestroy();
	}

	public boolean isSelectedAddress() {
		return selectedAddress;
	}

	public FavoriteAddressDto getNewFavoriteAddressDto() {
		return newFavoriteAddressDto;
	}
}