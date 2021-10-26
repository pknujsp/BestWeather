package com.lifedawn.bestweather.favorites;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
	
	private boolean enableCurrentLocation;
	
	private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentAttached(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f,
				@NonNull @NotNull Context context) {
			super.onFragmentAttached(fm, f, context);
		}
		
		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f,
				@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);
		}
		
		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);
			
		}
	};
	
	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			checkHaveLocations();
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		getActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
				checkHaveLocations();
			}
		});
		
		binding.addFavorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FindAddressFragment findAddressFragment = new FindAddressFragment();
				getParentFragmentManager().beginTransaction().hide(FavoritesFragment.this).add(R.id.fragment_container, findAddressFragment,
						getString(R.string.tag_find_address_fragment)).addToBackStack(
						getString(R.string.tag_find_address_fragment)).commit();
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
	
	private void checkHaveLocations() {
		final boolean haveFavorites = adapter.getItemCount() > 0;
		final boolean usingCurrentLocation = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(
				getString(R.string.pref_key_use_current_location), true);
		
		if (!haveFavorites && !usingCurrentLocation) {
			//즐겨찾기가 비었고, 현재 위치 사용이 꺼져있음
			//현재 위치 사용 여부 다이얼로그 표시
			//확인 : 현재 위치의 날씨 데이터로드, 취소 : 앱 종료
			new MaterialAlertDialogBuilder(getActivity()).setTitle(R.string.pref_title_use_current_location).setMessage(
					R.string.msg_request_enable_current_location_because_empty_locations).setPositiveButton(R.string.use,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(
									getString(R.string.pref_key_use_current_location), true).apply();
							enableCurrentLocation = true;
							getParentFragmentManager().popBackStack();
							dialogInterface.dismiss();
						}
					}).setNegativeButton(R.string.close_app, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					getActivity().finish();
				}
			}).setNeutralButton(R.string.add_favorite, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					binding.addFavorite.callOnClick();
				}
			}).create().show();
			
		} else {
			getParentFragmentManager().popBackStack();
		}
	}
	
	public boolean isEnableCurrentLocation() {
		return enableCurrentLocation;
	}
	
	public List<FavoriteAddressDto> getFavoriteAddressDtoList() {
		return adapter.getFavoriteAddressDtoList();
	}
	
}