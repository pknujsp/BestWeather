package com.lifedawn.bestweather.favorites;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.views.CustomEditText;
import com.lifedawn.bestweather.commons.views.CustomSearchView;
import com.lifedawn.bestweather.databinding.FragmentFavoritesBinding;
import com.lifedawn.bestweather.databinding.ViewSearchBinding;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

public class FavoritesFragment extends Fragment {
	private FragmentFavoritesBinding binding;
	private FavoriteAddressesAdapter adapter;
	private WeatherViewModel weatherViewModel;
	private FavoriteAddressesAdapter.OnClickedAddressListener onClickedAddressListener;

	private boolean enableCurrentLocation;
	private boolean refresh;
	private boolean clickedItem;
	private String fragmentRequestKey;

	private Dialog dialog;

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
			if (fragmentRequestKey.equals(getString(R.string.key_from_main_to_favorite))) {
				checkHaveLocations();
			} else if (fragmentRequestKey.equals(getString(R.string.key_from_widget_config_main_to_favorites))) {
				if (!clickedItem) {
					Bundle bundle = new Bundle();
					bundle.putSerializable(getString(R.string.bundle_key_selected_address_dto), null);
					getParentFragmentManager().setFragmentResult(getString(R.string.key_back_from_favorites_to_widget_config_main), bundle);
				}
				getParentFragmentManager().popBackStack();
			}
		}
	};

	public FavoritesFragment setOnClickedAddressListener(FavoriteAddressesAdapter.OnClickedAddressListener onClickedAddressListener) {
		this.onClickedAddressListener = onClickedAddressListener;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		getActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);

		fragmentRequestKey = getString(R.string.key_from_main_to_favorite);

		getParentFragmentManager().setFragmentResultListener(getString(R.string.key_back_from_find_address_to_favorite), this,
				new FragmentResultListener() {
					@Override
					public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {

					}
				});
		getParentFragmentManager().setFragmentResultListener(getString(R.string.key_from_widget_config_main_to_favorites), this,
				new FragmentResultListener() {
					@Override
					public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
						fragmentRequestKey = requestKey;
					}
				});

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentFavoritesBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.progressResultView.setContentView(binding.favoriteAddressList);

		binding.toolbar.fragmentTitle.setText(R.string.favorite);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressedCallback.handleOnBackPressed();
			}
		});

		/*
		binding.addFavorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FindAddressFragment findAddressFragment = new FindAddressFragment();
				getParentFragmentManager().setFragmentResult(getString(R.string.key_from_favorite_to_find_address), new Bundle());
				getParentFragmentManager().beginTransaction().hide(FavoritesFragment.this).add(R.id.fragment_container, findAddressFragment,
						getString(R.string.tag_find_address_fragment)).addToBackStack(
						getString(R.string.tag_find_address_fragment)).commit();
			}
		});

		 */


		binding.searchview.setOnClickedListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog = new Dialog(getActivity());
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

				ViewSearchBinding binding = ViewSearchBinding.inflate(getLayoutInflater());

				dialog.setContentView(binding.getRoot());
				final Window window = dialog.getWindow();
				window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
				window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
				window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				//imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				findDialogViews(dialog, binding);
				dialog.show();
			}
		});


		binding.favoriteAddressList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		adapter = new FavoriteAddressesAdapter();
		adapter.setOnClickedAddressListener(new FavoriteAddressesAdapter.OnClickedAddressListener() {
			@Override
			public void onClickedDelete(FavoriteAddressDto favoriteAddressDto, int position) {
				weatherViewModel.delete(favoriteAddressDto);
			}

			@Override
			public void onClicked(FavoriteAddressDto favoriteAddressDto) {
				clickedItem = true;
				onClickedItem(favoriteAddressDto);
			}
		});

		binding.favoriteAddressList.setAdapter(adapter);
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();

				if (adapter.getItemCount() == 0) {
					binding.progressResultView.onFailedProcessingData(getString(R.string.empty_favorite_addresses));
				} else {
					binding.progressResultView.onSuccessfulProcessingData();
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

	private void findDialogViews(final Dialog dialog, ViewSearchBinding binding) {
		binding.back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
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

		Bundle bundle = new Bundle();
		bundle.putSerializable(getString(R.string.bundle_key_new_favorite_address_list), (Serializable) adapter.getFavoriteAddressDtoList());

		if (!haveFavorites && !usingCurrentLocation) {
			//즐겨찾기가 비었고, 현재 위치 사용이 꺼져있음
			//현재 위치 사용 여부 다이얼로그 표시
			//확인 : 현재 위치의 날씨 데이터로드, 취소 : 앱 종료
			new MaterialAlertDialogBuilder(getActivity()).setTitle(R.string.title_empty_locations).setMessage(
					R.string.msg_empty_locations).setPositiveButton(R.string.use,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.dismiss();
							PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(
									getString(R.string.pref_key_use_current_location), true).apply();
							enableCurrentLocation = true;
							bundle.putBoolean(getString(R.string.bundle_key_changed_use_current_location), enableCurrentLocation);
							bundle.putBoolean(getString(R.string.bundle_key_refresh), refresh);
							getParentFragmentManager().popBackStack();

							getParentFragmentManager().setFragmentResult(getString(R.string.key_back_from_favorite_to_main), bundle);
						}
					}).setNegativeButton(R.string.add_favorite, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					//binding.addFavorite.callOnClick();
				}
			}).setNeutralButton(R.string.close_app, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					getActivity().finish();
				}
			}).create().show();

		} else {
			if (PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_key_last_current_location_latitude), "").isEmpty()) {
				refresh = true;
				bundle.putBoolean(getString(R.string.bundle_key_changed_use_current_location), enableCurrentLocation);
				bundle.putBoolean(getString(R.string.bundle_key_refresh), refresh);
			}

			getParentFragmentManager().popBackStack();
			getParentFragmentManager().setFragmentResult(getString(R.string.key_back_from_favorite_to_main), bundle);
		}
	}

	private void onClickedItem(FavoriteAddressDto favoriteAddressDto) {
		if (fragmentRequestKey.equals(getString(R.string.key_from_widget_config_main_to_favorites))) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(getString(R.string.bundle_key_selected_address_dto), favoriteAddressDto);
			getParentFragmentManager().setFragmentResult(getString(R.string.key_back_from_favorites_to_widget_config_main), bundle);
			onBackPressedCallback.handleOnBackPressed();
		}
	}
}