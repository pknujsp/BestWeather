package com.lifedawn.bestweather.favorites;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentFavoritesBinding;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.repository.FavoriteAddressRepository;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FavoritesFragment extends Fragment {
	private FragmentFavoritesBinding binding;
	private FavoriteAddressesAdapter adapter;
	private WeatherViewModel weatherViewModel;

	private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentAttached(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @NonNull @NotNull Context context) {
			super.onFragmentAttached(fm, f, context);
		}

		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);
		}

		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof FindAddressFragment) {
				if (((FindAddressFragment) f).isSelectedAddress()) {
					getParentFragmentManager().popBackStack();
				} else {

				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentFavoritesBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.customProgressView.setContentView(binding.favoriteAddressList);

		binding.toolbar.fragmentTitle.setText(R.string.favorite);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.addFavorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FindAddressFragment findAddressFragment = new FindAddressFragment();
				getParentFragmentManager().beginTransaction().hide(FavoritesFragment.this)
						.add(R.id.fragment_container, findAddressFragment, getString(R.string.tag_find_address_fragment))
						.addToBackStack(getString(R.string.tag_find_address_fragment)).commit();
			}
		});

		binding.favoriteAddressList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		adapter = new FavoriteAddressesAdapter();
		adapter.setOnDeleteListener(new FavoriteAddressesAdapter.OnDeleteListener() {
			@Override
			public void onClickedDelete(FavoriteAddressDto favoriteAddressDto, int position) {
				weatherViewModel.delete(favoriteAddressDto);
			}
		});

		binding.favoriteAddressList.setAdapter(adapter);
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();

				if (adapter.getItemCount() == 0) {
					binding.customProgressView.onFailedProcessingData(getString(R.string.empty_favorite_addresses));
				} else {
					binding.customProgressView.onSuccessfulProcessingData();
				}
			}
		});
		weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				adapter.setFavoriteAddressDtoList(result);
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							adapter.notifyDataSetChanged();
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
		getParentFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		super.onDestroy();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
				@Override
				public void onResultSuccessful(List<FavoriteAddressDto> result) {
					adapter.setFavoriteAddressDtoList(result);
					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								adapter.notifyDataSetChanged();
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
}