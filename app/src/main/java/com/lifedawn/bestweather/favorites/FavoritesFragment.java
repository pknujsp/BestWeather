package com.lifedawn.bestweather.favorites;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.AlarmSettingsFragment;
import com.lifedawn.bestweather.commons.classes.IntentUtil;
import com.lifedawn.bestweather.commons.classes.RecyclerViewItemDecoration;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentFavoritesBinding;
import com.lifedawn.bestweather.databinding.ViewSearchBinding;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.main.MainTransactionFragment;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.always.AlwaysNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.daily.fragment.DailyNotificationSettingsFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;
import com.lifedawn.bestweather.widget.ConfigureWidgetActivity;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

public class FavoritesFragment extends Fragment {
	private FragmentFavoritesBinding binding;
	private FavoriteAddressesAdapter adapter;
	private WeatherViewModel weatherViewModel;
	private OnResultFragmentListener onResultFragmentListener;

	private boolean enableCurrentLocation;
	private boolean refresh;
	private boolean clickedItem;
	private String requestFragment;

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
			if (getParentFragmentManager().getBackStackEntryCount() >= 2) {
				getParentFragmentManager().popBackStackImmediate();
			} else {
				if (requestFragment.equals(MainTransactionFragment.class.getName())) {
					checkHaveLocations();
				} else if (requestFragment.equals(ConfigureWidgetActivity.class.getName()) ||
						requestFragment.equals(AlwaysNotificationSettingsFragment.class.getName()) ||
						requestFragment.equals(AlarmSettingsFragment.class.getName()) ||
						requestFragment.equals(DailyNotificationSettingsFragment.class.getName())) {
					if (!clickedItem) {
						Bundle bundle = new Bundle();
						bundle.putSerializable(BundleKey.SelectedAddressDto.name(), null);
						onResultFragmentListener.onResultFragment(bundle);
					}
					getParentFragmentManager().popBackStack();
				}
			}

		}
	};

	public FavoritesFragment setOnResultFragmentListener(OnResultFragmentListener onResultFragmentListener) {
		this.onResultFragmentListener = onResultFragmentListener;
		return this;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		getActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);

		Bundle bundle = getArguments();
		requestFragment = bundle.getString(BundleKey.RequestFragment.name());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentFavoritesBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		binding.progressResultView.setContentView(binding.favoriteAddressList);

		binding.toolbar.fragmentTitle.setText(R.string.favorite);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressedCallback.handleOnBackPressed();
			}
		});

		binding.searchview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*
				dialog = new Dialog(getActivity());
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

				final Window window = dialog.getWindow();

				WindowManager.LayoutParams windowLayoutParams = window.getAttributes();
				windowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
				windowLayoutParams.x = binding.searchview.getLeft();
				windowLayoutParams.y = binding.searchview.getTop();

				//window.setLayout(binding.searchview.getWidth(), 300);
				//window.setGravity(Gravity.CENTER_HORIZONTAL);
				//window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
				//window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				//imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

				ViewSearchBinding searchViewBinding = ViewSearchBinding.inflate(getLayoutInflater());
				dialog.setContentView(searchViewBinding.getRoot());
				findDialogViews(dialog, searchViewBinding);
				dialog.show();

				 */

				FindAddressFragment findAddressFragment = new FindAddressFragment();
				Bundle bundle = new Bundle();
				bundle.putString(BundleKey.RequestFragment.name(), FavoritesFragment.class.getName());
				findAddressFragment.setArguments(bundle);

				findAddressFragment.setOnResultFragmentListener(new OnResultFragmentListener() {
					@Override
					public void onResultFragment(Bundle result) {

					}
				});

				getParentFragmentManager().beginTransaction().hide(FavoritesFragment.this).add(R.id.fragment_container, findAddressFragment,
						getString(R.string.tag_find_address_fragment)).addToBackStack(
						getString(R.string.tag_find_address_fragment)).commit();
			}
		});

		binding.favoriteAddressList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		binding.favoriteAddressList.addItemDecoration(new RecyclerViewItemDecoration(getContext()));
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
					binding.progressResultView.onFailed(getString(R.string.empty_favorite_addresses));
				} else {
					binding.progressResultView.onSuccessful();
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
		final boolean useCurrentLocation = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(
				getString(R.string.pref_key_use_current_location), true);

		Bundle bundle = new Bundle();
		bundle.putSerializable(BundleKey.newFavoriteAddressDtoList.name(), (Serializable) adapter.getFavoriteAddressDtoList());
		bundle.putString(BundleKey.LastFragment.name(), FavoritesFragment.class.getName());

		if (!haveFavorites && !useCurrentLocation) {
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
									getString(R.string.pref_key_use_current_location), true)
									.putString(getString(R.string.pref_key_last_selected_location_type), LocationType.CurrentLocation.name()).apply();
							enableCurrentLocation = true;

							if (checkPermissionAndGpsOn()) {
								bundle.putBoolean(BundleKey.ChangedUseCurrentLocation.name(), enableCurrentLocation);
								bundle.putBoolean(BundleKey.Refresh.name(), refresh);

								onResultFragmentListener.onResultFragment(bundle);
								getParentFragmentManager().popBackStack();
							}
						}
					}).setNegativeButton(R.string.add_favorite, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
							.putString(getString(R.string.pref_key_last_selected_location_type), LocationType.SelectedAddress.name()).apply();
					binding.searchview.callOnClick();
				}
			}).setNeutralButton(R.string.close_app, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					getActivity().finish();
				}
			}).create().show();

		} else if (haveFavorites) {
			onResultFragmentListener.onResultFragment(bundle);
			getParentFragmentManager().popBackStack();
		} else {
			if (checkPermissionAndGpsOn()) {
				if (PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_key_last_current_location_latitude), "0.0").equals("0.0")) {
					refresh = true;
					bundle.putBoolean(BundleKey.ChangedUseCurrentLocation.name(), enableCurrentLocation);
					bundle.putBoolean(BundleKey.Refresh.name(), refresh);
				}
				onResultFragmentListener.onResultFragment(bundle);
				getParentFragmentManager().popBackStack();
			}
		}
	}

	private boolean checkPermissionAndGpsOn() {
		LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
		final boolean isPermissionGranted =
				getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
		final boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (isPermissionGranted && isGpsEnabled) {
			return true;
		} else if (!isPermissionGranted) {
			Toast.makeText(getContext(), R.string.message_needs_location_permission, Toast.LENGTH_SHORT).show();

			// 다시 묻지 않음을 선택했는지 확인
			final boolean neverAskAgain = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(
					getString(R.string.pref_key_never_ask_again_permission_for_access_location), false);

			if (neverAskAgain) {
				startActivity(IntentUtil.getAppSettingsIntent(getActivity()));
			} else {
				ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
			}
			return false;
		} else {
			Toast.makeText(getContext(), R.string.request_to_make_gps_on, Toast.LENGTH_SHORT).show();
			startActivity(IntentUtil.getLocationSettingsIntent());
			return false;
		}
	}

	private void onClickedItem(FavoriteAddressDto favoriteAddressDto) {
		if (requestFragment.equals(ConfigureWidgetActivity.class.getName()) ||
				requestFragment.equals(AlwaysNotificationSettingsFragment.class.getName()) ||
				requestFragment.equals(AlarmSettingsFragment.class.getName()) ||
				requestFragment.equals(DailyNotificationSettingsFragment.class.getName())) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(BundleKey.SelectedAddressDto.name(), favoriteAddressDto);
			onResultFragmentListener.onResultFragment(bundle);
			onBackPressedCallback.handleOnBackPressed();
		}
	}
}