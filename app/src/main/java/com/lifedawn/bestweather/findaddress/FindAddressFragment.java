package com.lifedawn.bestweather.findaddress;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentFindAddressBinding;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.repository.FavoriteAddressRepository;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class FindAddressFragment extends Fragment {
	private FragmentFindAddressBinding binding;
	private FoundAddressesAdapter addressesAdapter;
	private boolean selectedAddress = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
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
				FavoriteAddressDto favoriteAddressDto = new FavoriteAddressDto();
				favoriteAddressDto.setCountryName(address.getCountryName());
				favoriteAddressDto.setCountryCode(address.getCountryCode());
				favoriteAddressDto.setAddress(address.getAddressLine(0));
				favoriteAddressDto.setLatitude(String.valueOf(address.getLatitude()));
				favoriteAddressDto.setLongitude(String.valueOf(address.getLongitude()));

				FavoriteAddressRepository repository = new FavoriteAddressRepository(getContext());
				repository.add(favoriteAddressDto, new DbQueryCallback<Long>() {
					@Override
					public void onResultSuccessful(Long result) {
						if (getActivity() != null) {
							selectedAddress = true;
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
		});

		binding.addressList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		binding.addressList.setAdapter(addressesAdapter);

		Geocoder geocoder = new Geocoder(getContext());

		binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				binding.customProgressView.onStartedProcessingData();

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							List<Address> addressList = geocoder.getFromLocationName(query, 20);
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
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();

				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
	}

	public boolean isSelectedAddress() {
		return selectedAddress;
	}
}