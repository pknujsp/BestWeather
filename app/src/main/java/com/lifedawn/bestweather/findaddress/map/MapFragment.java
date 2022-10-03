package com.lifedawn.bestweather.findaddress.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.AlarmSettingsFragment;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.IntentUtil;
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.commons.views.CustomEditText;
import com.lifedawn.bestweather.databinding.FragmentMapBinding;
import com.lifedawn.bestweather.favorites.FavoriteAddressesAdapter;
import com.lifedawn.bestweather.favorites.SimpleFavoritesFragment;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.findaddress.FoundAddressesAdapter;
import com.lifedawn.bestweather.findaddress.map.adapter.FavoriteLocationItemViewPagerAdapter;
import com.lifedawn.bestweather.findaddress.map.adapter.LocationItemViewPagerAdapter;
import com.lifedawn.bestweather.findaddress.map.interfaces.OnClickedLocationBtnListener;
import com.lifedawn.bestweather.findaddress.map.interfaces.OnClickedScrollBtnListener;
import com.lifedawn.bestweather.main.MainTransactionFragment;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.daily.fragment.DailyNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.ongoing.OngoingNotificationSettingsFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;
import com.lifedawn.bestweather.widget.ConfigureWidgetActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
		FoundAddressesAdapter.OnClickedAddressListener,
		FindAddressFragment.OnListListener, IBottomSheetState {
	private FragmentMapBinding binding;
	private GoogleMap googleMap;
	private Map<MarkerType, List<Marker>> markerMaps = new HashMap<>();
	private LocationLifeCycleObserver locationLifeCycleObserver;
	private WeatherViewModel weatherViewModel;
	private OnResultFavoriteListener onResultFavoriteListener;
	private Set<String> favoriteAddressSet = new HashSet<>();

	protected final Map<BottomSheetType, BottomSheetBehavior> bottomSheetBehaviorMap = new HashMap<>();
	protected final Map<BottomSheetType, LinearLayout> bottomSheetViewMap = new HashMap<>();
	protected final Map<MarkerType, RecyclerView.Adapter> adapterMap = new HashMap<>();

	private FusedLocation fusedLocation;
	private Integer dp48;

	private ViewPager2 locationItemBottomSheetViewPager;
	private boolean removedLocation;

	private boolean enableCurrentLocation;
	private boolean addedNewLocation;
	private boolean refresh;
	private boolean clickedItem;
	private String requestFragment;
	private boolean clickedHeader;

	private Bundle bundle;
	private boolean needsOnResultCallback = true;

	public boolean isClickedItem() {
		return clickedItem;
	}


	public MapFragment setOnResultFavoriteListener(OnResultFavoriteListener onResultFavoriteListener) {
		this.onResultFavoriteListener = onResultFavoriteListener;
		return this;
	}

	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (!getChildFragmentManager().popBackStackImmediate()) {
				if (requestFragment.equals(MainTransactionFragment.class.getName()) || requestFragment.equals(WeatherFragment.class.getName())) {
					checkHaveLocations();
				} else if (requestFragment.equals(ConfigureWidgetActivity.class.getName()) ||
						requestFragment.equals(OngoingNotificationSettingsFragment.class.getName()) ||
						requestFragment.equals(AlarmSettingsFragment.class.getName()) ||
						requestFragment.equals(DailyNotificationSettingsFragment.class.getName())) {

					if (!clickedItem) {
						weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
							@Override
							public void onResultSuccessful(List<FavoriteAddressDto> result) {
								MainThreadWorker.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										needsOnResultCallback = true;
										getParentFragmentManager().popBackStack();
										onResultFavoriteListener.onResult(result);
									}
								});
							}

							@Override
							public void onResultNoData() {

							}
						});
					} else {
						getParentFragmentManager().popBackStack();
					}

				}
			}

		}
	};


	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
			super.onFragmentAttached(fm, f, context);


		}

		@Override
		public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentStarted(fm, f);


		}

		@Override
		public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentResumed(fm, f);

			if (f instanceof FindAddressFragment) {
				binding.headerLayout.requestFocusEditText();
			}
		}

		@Override
		public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof FindAddressFragment) {
				clickedHeader = false;
				binding.headerLayout.clearFocusEditText();
				removeMarkers(MarkerType.SEARCH);
				collapseAllExpandedBottomSheets();
			} else if (f instanceof SimpleFavoritesFragment) {
				collapseAllExpandedBottomSheets();
			}
		}

	};


	@Override
	public void onClickedAddress(Address address) {
		final FavoriteAddressDto favoriteAddressDto = new FavoriteAddressDto();
		favoriteAddressDto.setCountryName(address.getCountryName());
		favoriteAddressDto.setCountryCode(address.getCountryCode() == null ? "" : address.getCountryCode());
		favoriteAddressDto.setAddress(address.getAddressLine(0));
		favoriteAddressDto.setAdmin(address.getLocality());
		favoriteAddressDto.setLatitude(String.valueOf(address.getLatitude()));
		favoriteAddressDto.setLongitude(String.valueOf(address.getLongitude()));

		weatherViewModel.contains(favoriteAddressDto.getLatitude(), favoriteAddressDto.getLongitude(),
				new DbQueryCallback<Boolean>() {
					@Override
					public void onResultSuccessful(Boolean contains) {
						if (contains) {
							if (getActivity() != null) {
								MainThreadWorker.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(getContext(), R.string.duplicate_address, Toast.LENGTH_SHORT).show();
									}
								});
							}
						} else {
							MainThreadWorker.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									//addedNewLocation = true;
									weatherViewModel.favoriteAddressListLiveData.removeObservers(getViewLifecycleOwner());
								}
							});

							weatherViewModel.add(favoriteAddressDto, new DbQueryCallback<Long>() {
								@Override
								public void onResultSuccessful(Long id) {
									weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
										@Override
										public void onResultSuccessful(List<FavoriteAddressDto> result) {
											if (getActivity() != null) {
												MainThreadWorker.runOnUiThread(new Runnable() {
													@Override
													public void run() {
														Toast.makeText(getContext(), address.getAddressLine(0), Toast.LENGTH_SHORT).show();

														favoriteAddressDto.setId(id.intValue());

														PreferenceManager.getDefaultSharedPreferences(getContext())
																.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id),
																		favoriteAddressDto.getId()).commit();

														getParentFragmentManager().popBackStack();
														needsOnResultCallback = true;
														onResultFavoriteListener.onAddedNewAddress(favoriteAddressDto, result, removedLocation);
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

	public enum MarkerType {
		LONG_CLICK, SEARCH, FAVORITE
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		getActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);

		weatherViewModel = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);
		getChildFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true);

		fusedLocation = FusedLocation.getInstance(getContext());

		bundle = getArguments() != null ? getArguments() : savedInstanceState;
		requestFragment = bundle.getString(BundleKey.RequestFragment.name());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentMapBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		setSearchPlacesBottomSheet();
		setLocationItemsBottomSheet();
		setFavoritesBottomSheet();

		binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);

				final int searchBottomSheetHeight =
						(int) (binding.getRoot().getHeight() - binding.headerLayout.getBottom() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics()));
				LinearLayout locationSearchBottomSheet = bottomSheetViewMap.get(BottomSheetType.SEARCH_LOCATION);
				LinearLayout favoritesBottomSheet = bottomSheetViewMap.get(BottomSheetType.FAVORITES);

				locationSearchBottomSheet.getLayoutParams().height = searchBottomSheetHeight;
				favoritesBottomSheet.getLayoutParams().height = searchBottomSheetHeight;

				locationSearchBottomSheet.requestLayout();
				favoritesBottomSheet.requestLayout();
			}
		});

		binding.headerLayout.setEditTextOnFocusListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (clickedHeader) {
						return;
					}
					clickedHeader = true;
					FragmentManager childFragmentManager = getChildFragmentManager();

					if (childFragmentManager.findFragmentByTag(getString(R.string.tag_find_address_fragment)) != null) {
						return;
					}

					collapseAllExpandedBottomSheets();
					int backStackCount = childFragmentManager.getBackStackEntryCount();

					for (int count = 0; count < backStackCount; count++) {
						childFragmentManager.popBackStack();
					}

					FindAddressFragment findAddressFragment = new FindAddressFragment();
					Bundle bundle = new Bundle();
					bundle.putString(BundleKey.RequestFragment.name(), MapFragment.class.getName());
					findAddressFragment.setArguments(bundle);

					findAddressFragment.setOnAddressListListener(new FindAddressFragment.OnAddressListListener() {
						@Override
						public void onSearchedAddressList(List<Address> addressList) {
							removeMarkers(MarkerType.SEARCH);

							LocationItemViewPagerAdapter adapter = new LocationItemViewPagerAdapter();
							adapterMap.put(MarkerType.SEARCH, adapter);
							adapter.setFavoriteAddressSet(favoriteAddressSet);
							adapter.setAddressList(addressList);
							adapter.setOnClickedLocationBtnListener(new OnClickedLocationBtnListener<Address>() {
								@Override
								public void onSelected(Address e, boolean remove) {
									onClickedAddress(e);
								}
							});

							adapter.setOnClickedScrollBtnListener(new OnClickedScrollBtnListener() {
								@Override
								public void toLeft() {
									if (binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() > 0) {
										binding.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
												binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() - 1, true);
									}
								}

								@Override
								public void toRight() {
									if (binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() < adapter.getItemCount() - 1) {
										binding.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
												binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() + 1, true);
									}
								}
							});

							int i = 0;
							for (Address address : addressList) {
								addMarker(MarkerType.SEARCH, i++, address);
							}

							showMarkers(MarkerType.SEARCH);

						}
					});

					findAddressFragment.setiBottomSheetState(MapFragment.this);
					findAddressFragment.setOnClickedAddressListener(MapFragment.this);
					findAddressFragment.setOnListListener(MapFragment.this);

					binding.headerLayout.setOnEditTextQueryListener(findAddressFragment.getOnEditTextQueryListener());

					childFragmentManager.beginTransaction().
							add(binding.bottomSheetSearchPlace.searchFragmentContainer.getId(), findAddressFragment,
									getString(R.string.tag_find_address_fragment)).addToBackStack(
									getString(R.string.tag_find_address_fragment)).commitAllowingStateLoss();

					setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_EXPANDED);
				}
			}
		});


		binding.favorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				FragmentManager childFragmentManager = getChildFragmentManager();

				if (childFragmentManager.findFragmentByTag(SimpleFavoritesFragment.class.getName()) != null) {
					return;
				}

				collapseAllExpandedBottomSheets();

				int backStackCount = childFragmentManager.getBackStackEntryCount();

				for (int count = 0; count < backStackCount; count++) {
					childFragmentManager.popBackStack();
				}

				SimpleFavoritesFragment simpleFavoritesFragment = new SimpleFavoritesFragment();
				simpleFavoritesFragment.setOnClickedAddressListener(new FavoriteAddressesAdapter.OnClickedAddressListener() {
					@Override
					public void onClickedDelete(FavoriteAddressDto favoriteAddressDto, int position) {
						removedLocation = true;
					}

					@Override
					public void onClicked(FavoriteAddressDto favoriteAddressDto) {

					}
				});

				childFragmentManager.beginTransaction().
						add(binding.favoritesBottomSheet.fragmentContainer.getId(), simpleFavoritesFragment,
								SimpleFavoritesFragment.class.getName()).
						addToBackStack(
								SimpleFavoritesFragment.class.getName()).
						commitAllowingStateLoss();

				setStateOfBottomSheet(BottomSheetType.FAVORITES, BottomSheetBehavior.STATE_EXPANDED);
			}
		});

		binding.headerLayout.setOnBackClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressedCallback.handleOnBackPressed();
			}
		});

		if (requestFragment.equals(MainTransactionFragment.class.getName())) {
			binding.favorite.callOnClick();
		}
	}

	@Override
	public void onDestroy() {
		getLifecycle().removeObserver(locationLifeCycleObserver);
		getChildFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		onBackPressedCallback.remove();

		super.onDestroy();
	}


	@SuppressLint("MissingPermission")
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		this.googleMap = googleMap;

		dp48 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, getResources().getDisplayMetrics());

		googleMap.setPadding(0, MyApplication.getStatusBarHeight() + binding.headerLayout.getBottom() +
				dp48, 0, 0);
		googleMap.getUiSettings().setZoomControlsEnabled(true);
		googleMap.getUiSettings().setRotateGesturesEnabled(false);
		googleMap.setOnMarkerClickListener(this);

		googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(@NonNull LatLng latLng) {
				setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED);
			}
		});

		googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(@NonNull LatLng latLng) {
				onLongClicked(latLng);
			}
		});

		googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
			@Override
			public boolean onMyLocationButtonClick() {
				return false;
			}
		});

		googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
			@Override
			public boolean onMyLocationButtonClick() {
				if (!fusedLocation.isOnGps()) {
					fusedLocation.onDisabledGps(requireActivity(), locationLifeCycleObserver, new ActivityResultCallback<ActivityResult>() {
						@Override
						public void onActivityResult(ActivityResult result) {
						}
					});
				}
				return false;
			}
		});


		try {
			googleMap.setMyLocationEnabled(true);
		} catch (SecurityException e) {
			fusedLocation.onRejectPermissions(requireActivity(), locationLifeCycleObserver, new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					if (fusedLocation.checkDefaultPermissions()) {
						googleMap.setMyLocationEnabled(true);
					}
				}
			}, new ActivityResultCallback<Map<String, Boolean>>() {
				@Override
				public void onActivityResult(Map<String, Boolean> result) {
					//gps사용 권한
					//허가남 : 현재 위치 다시 파악
					//거부됨 : 작업 취소
					//계속 거부 체크됨 : 작업 취소
					if (!result.containsValue(false)) {
						googleMap.setMyLocationEnabled(true);
					} else {
						googleMap.setMyLocationEnabled(false);
					}
				}
			});

		}

		FavoriteLocationItemViewPagerAdapter adapter = new FavoriteLocationItemViewPagerAdapter();
		adapterMap.put(MarkerType.FAVORITE, adapter);
		adapter.setOnClickedLocationBtnListener(new OnClickedLocationBtnListener<FavoriteAddressDto>() {
			@Override
			public void onSelected(FavoriteAddressDto e, boolean remove) {
				if (remove) {
					new MaterialAlertDialogBuilder(getActivity()).
							setTitle(R.string.remove)
							.setMessage(e.getAddress()).
							setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									weatherViewModel.delete(e, new DbQueryCallback<Boolean>() {
										@Override
										public void onResultSuccessful(Boolean result) {
											MainThreadWorker.runOnUiThread(new Runnable() {
												@Override
												public void run() {
													setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED);
													removedLocation = true;
													dialog.dismiss();
												}
											});
										}

										@Override
										public void onResultNoData() {

										}
									});
								}
							}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).create().show();

				}
			}
		});

		adapter.setOnClickedScrollBtnListener(new OnClickedScrollBtnListener() {
			@Override
			public void toLeft() {
				if (binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() > 0) {
					binding.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
							binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() - 1, true);
				}
			}

			@Override
			public void toRight() {
				if (binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() < adapter.getItemCount() - 1) {
					binding.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
							binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() + 1, true);
				}
			}
		});

		weatherViewModel.favoriteAddressListLiveData.observe(getViewLifecycleOwner(), new Observer<List<FavoriteAddressDto>>() {
			@Override
			public void onChanged(List<FavoriteAddressDto> result) {
				if (!addedNewLocation) {
					MainThreadWorker.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							FavoriteLocationItemViewPagerAdapter adapter = (FavoriteLocationItemViewPagerAdapter) adapterMap.get(MarkerType.FAVORITE);
							adapter.setAddressList(result);

							favoriteAddressSet.clear();
							for (FavoriteAddressDto favoriteAddressDto : result) {
								favoriteAddressSet.add(favoriteAddressDto.getLatitude() + favoriteAddressDto.getLongitude());
							}

							removeMarkers(MarkerType.FAVORITE);

							for (int i = 0; i < result.size(); i++) {
								addFavoriteMarker(i, result.get(i));
							}
						}
					});
				}
			}
		});

	}

	private void onLongClicked(LatLng latLng) {
		collapseAllExpandedBottomSheets();
		Geocoding.geocoding(getContext(), latLng.latitude, latLng.longitude, new Geocoding.GeocodingCallback() {
			@Override
			public void onGeocodingResult(List<Address> addressList) {
				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						List<Address> addresses = new ArrayList<>();
						addresses.add(addressList.get(0));

						LocationItemViewPagerAdapter adapter = new LocationItemViewPagerAdapter();
						adapterMap.put(MarkerType.LONG_CLICK, adapter);
						adapter.setAddressList(addresses);
						adapter.setFavoriteAddressSet(favoriteAddressSet);
						adapter.setOnClickedLocationBtnListener(new OnClickedLocationBtnListener<Address>() {
							@Override
							public void onSelected(Address e, boolean remove) {
								onClickedAddress(e);
							}
						});

						adapter.setOnClickedScrollBtnListener(new OnClickedScrollBtnListener() {
							@Override
							public void toLeft() {
								if (binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() > 0) {
									binding.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
											binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() - 1, true);
								}
							}

							@Override
							public void toRight() {
								if (binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() < adapter.getItemCount() - 1) {
									binding.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
											binding.placeslistBottomSheet.placeItemsViewpager.getCurrentItem() + 1, true);
								}
							}
						});

						removeMarkers(MarkerType.LONG_CLICK);
						addMarker(MarkerType.LONG_CLICK, 0, addressList.get(0));
						showMarkers(MarkerType.LONG_CLICK);

						locationItemBottomSheetViewPager.setTag(MarkerType.LONG_CLICK);
						locationItemBottomSheetViewPager.setAdapter(adapterMap.get(MarkerType.LONG_CLICK));
						locationItemBottomSheetViewPager.setCurrentItem(0, false);

						setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_EXPANDED);
					}
				});
			}
		});
	}

	private void addMarker(MarkerType markerType, int position, Address address) {
		Marker marker = googleMap.addMarker(new MarkerOptions()
				.position(new LatLng(address.getLatitude(), address.getLongitude()))
				.title(address.getAddressLine(0)));
		marker.setTag(new MarkerHolder(position, markerType));

		if (!markerMaps.containsKey(markerType)) {
			markerMaps.put(markerType, new ArrayList<>());
		}

		markerMaps.get(markerType).add(marker);
	}

	private void addFavoriteMarker(int position, FavoriteAddressDto favoriteAddressDto) {
		MarkerOptions markerOptions = new MarkerOptions()
				.anchor(0.5f, 0.5f)
				.position(new LatLng(Double.parseDouble(favoriteAddressDto.getLatitude()),
						Double.parseDouble(favoriteAddressDto.getLongitude())))
				.title(favoriteAddressDto.getAddress());

		View view = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_view, null);
		TextView tv_marker = view.findViewById(R.id.marker);
		tv_marker.setText(new String((position + 1) + ""));

		Marker marker = googleMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(),
				view))));

		marker.setTag(new MarkerHolder(position, MarkerType.FAVORITE));

		if (!markerMaps.containsKey(MarkerType.FAVORITE)) {
			markerMaps.put(MarkerType.FAVORITE, new ArrayList<>());
		}

		markerMaps.get(MarkerType.FAVORITE).add(marker);
	}

	private Bitmap createDrawableFromView(Context context, View view) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
		view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
		view.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);

		return bitmap;
	}

	private void removeMarkers(MarkerType markerType) {
		if (markerMaps.containsKey(markerType)) {
			List<Marker> markers = markerMaps.get(markerType);
			for (Marker marker : markers) {
				marker.remove();
			}
			markerMaps.remove(markerType);
		}
	}

	private void showMarkers(MarkerType markerType) {
		if (markerMaps.containsKey(markerType)) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();

			for (Marker marker : markerMaps.get(markerType)) {
				builder.include(marker.getPosition());
			}

			LatLngBounds bounds = builder.build();
			CameraUpdate cameraUpdate = null;

			if (markerMaps.get(markerType).size() == 1) {
				cameraUpdate = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 15);
			} else {
				cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36f,
						getResources().getDisplayMetrics()));
			}

			googleMap.moveCamera(cameraUpdate);
		}
	}

	@Override
	public boolean onMarkerClick(@NonNull Marker marker) {
		MarkerHolder markerHolder = (MarkerHolder) marker.getTag();
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerMaps.get(markerHolder.markerType).get(markerHolder.position).getPosition()));

		locationItemBottomSheetViewPager.setTag(markerHolder.markerType);
		binding.placeslistBottomSheet.placeItemsViewpager.setAdapter(adapterMap.get(markerHolder.markerType));
		locationItemBottomSheetViewPager.setCurrentItem(markerHolder.position, false);
		setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_EXPANDED);

		return true;
	}

	protected void setLocationItemsBottomSheet() {
		LinearLayout locationItemBottomSheet = binding.placeslistBottomSheet.placesBottomsheet;
		locationItemBottomSheetViewPager = binding.placeslistBottomSheet.placeItemsViewpager;

		locationItemBottomSheetViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			MarkerType markerType;

			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				if (getStateOfBottomSheet(BottomSheetType.LOCATION_ITEM) == BottomSheetBehavior.STATE_EXPANDED) {
					markerType = (MarkerType) locationItemBottomSheetViewPager.getTag();
					onPOIItemSelectedByBottomSheet(position, markerType);
				}
			}
		});
		locationItemBottomSheetViewPager.setOffscreenPageLimit(2);

		BottomSheetBehavior locationItemBottomSheetBehavior = BottomSheetBehavior.from(locationItemBottomSheet);
		locationItemBottomSheetBehavior.setDraggable(false);
		locationItemBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			float differenceY;

			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == BottomSheetBehavior.STATE_EXPANDED) {
					//binding.naverMapButtonsLayout.getRoot().setY(binding.getRoot().getHeight() - bottomSheet.getHeight());
				} else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
					//binding.naverMapButtonsLayout.getRoot().setY(binding.getRoot().getHeight() - binding.naverMapButtonsLayout.getRoot
					// ().getHeight());
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				//expanded일때 offset == 1.0, collapsed일때 offset == 0.0
				//offset에 따라서 버튼들이 이동하고, 지도의 좌표가 변경되어야 한다.
				int translationValue = (int) (bottomSheet.getHeight() * slideOffset);

				//expanded일때 offset == 1.0, collapsed일때 offset == 0.0
				//offset에 따라서 버튼들이 이동하고, 지도의 좌표가 변경되어야 한다.
				googleMap.setPadding(0, MyApplication.getStatusBarHeight() + binding.headerLayout.getBottom() +
						dp48, 0, translationValue);
			}
		});

		bottomSheetViewMap.put(BottomSheetType.LOCATION_ITEM, locationItemBottomSheet);
		bottomSheetBehaviorMap.put(BottomSheetType.LOCATION_ITEM, locationItemBottomSheetBehavior);
	}


	private void setSearchPlacesBottomSheet() {
		LinearLayout locationSearchBottomSheet = binding.bottomSheetSearchPlace.searchPlaceBottomsheet;

		BottomSheetBehavior locationSearchBottomSheetBehavior = BottomSheetBehavior.from(locationSearchBottomSheet);
		locationSearchBottomSheetBehavior.setDraggable(false);
		locationSearchBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {

			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {

			}
		});

		locationSearchBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

		bottomSheetViewMap.put(BottomSheetType.SEARCH_LOCATION, locationSearchBottomSheet);
		bottomSheetBehaviorMap.put(BottomSheetType.SEARCH_LOCATION, locationSearchBottomSheetBehavior);
	}

	private void setFavoritesBottomSheet() {
		LinearLayout favoritesBottomSheet = binding.favoritesBottomSheet.favoritesBottomsheet;

		BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(favoritesBottomSheet);
		bottomSheetBehavior.setDraggable(false);
		bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {

			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {

			}
		});

		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

		bottomSheetViewMap.put(BottomSheetType.FAVORITES, favoritesBottomSheet);
		bottomSheetBehaviorMap.put(BottomSheetType.FAVORITES, bottomSheetBehavior);
	}

	@Override
	public void setStateOfBottomSheet(BottomSheetType bottomSheetType, int state) {
		bottomSheetBehaviorMap.get(bottomSheetType).setState(state);
	}

	@Override
	public int getStateOfBottomSheet(BottomSheetType bottomSheetType) {
		return bottomSheetBehaviorMap.get(bottomSheetType).getState();
	}

	public List<BottomSheetBehavior> getBottomSheetBehaviorOfExpanded(BottomSheetBehavior currentBottomSheetBehavior) {
		Set<BottomSheetType> keySet = bottomSheetBehaviorMap.keySet();
		List<BottomSheetBehavior> bottomSheetBehaviors = new ArrayList<>();

		for (BottomSheetType bottomSheetType : keySet) {
			if (bottomSheetBehaviorMap.get(bottomSheetType).getState() == BottomSheetBehavior.STATE_EXPANDED) {

				if (currentBottomSheetBehavior != null) {
					if (!bottomSheetBehaviorMap.get(bottomSheetType).equals(currentBottomSheetBehavior)) {
						bottomSheetBehaviors.add(bottomSheetBehaviorMap.get(bottomSheetType));
					}
				}
			}
		}
		return bottomSheetBehaviors;
	}

	@Override
	public void collapseAllExpandedBottomSheets() {
		Set<BottomSheetType> keySet = bottomSheetBehaviorMap.keySet();

		for (BottomSheetType bottomSheetType : keySet) {
			if (getStateOfBottomSheet(bottomSheetType) == BottomSheetBehavior.STATE_EXPANDED) {
				setStateOfBottomSheet(bottomSheetType, BottomSheetBehavior.STATE_COLLAPSED);
			}
		}

	}

	@Override
	public void onPOIItemSelectedByList(int position) {
		//bottomsheet가 아닌 list에서 아이템을 선택한 경우 호출
		//adapter -> poiitem생성 -> select poiitem -> bottomsheet열고 정보 표시
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerMaps.get(MarkerType.SEARCH).get(position).getPosition()));
		//선택된 마커의 아이템 리스트내 위치 파악 후 뷰 페이저 이동
		locationItemBottomSheetViewPager.setTag(MarkerType.SEARCH);
		locationItemBottomSheetViewPager.setAdapter(adapterMap.get(MarkerType.SEARCH));
		locationItemBottomSheetViewPager.setCurrentItem(position, false);

		setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_EXPANDED);
	}

	@Override
	public void onPOIItemSelectedByBottomSheet(int position, MarkerType markerType) {
		//bottomsheet에서 스크롤 하는 경우 호출
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerMaps.get(markerType).get(position).getPosition()));
	}

	private void processIfNoLocations(@NonNull List<FavoriteAddressDto> result) {
		final boolean haveFavorites = result.size() > 0;
		final boolean useCurrentLocation = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(
				getString(R.string.pref_key_use_current_location), false);

		if (!haveFavorites && !useCurrentLocation) {
			//즐겨찾기가 비었고, 현재 위치 사용이 꺼져있음
			//현재 위치 사용 여부 다이얼로그 표시
			//확인 : 현재 위치의 날씨 데이터로드, 취소 : 앱 종료
			new MaterialAlertDialogBuilder(getActivity()).setTitle(R.string.title_empty_locations).setMessage(
					R.string.msg_empty_locations).setPositiveButton(R.string.use,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(
											getString(R.string.pref_key_use_current_location), true)
									.putString(getString(R.string.pref_key_last_selected_location_type),
											LocationType.CurrentLocation.name()).commit();
							enableCurrentLocation = true;
							dialogInterface.dismiss();

							if (checkPermissionAndGpsOn()) {
								needsOnResultCallback = false;
								getParentFragmentManager().popBackStack();
								onResultFavoriteListener.onResult(result);
							}
						}
					}).setNegativeButton(R.string.add_favorite, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
							.putString(getString(R.string.pref_key_last_selected_location_type),
									LocationType.SelectedAddress.name()).commit();

					dialogInterface.dismiss();
					binding.headerLayout.callOnClickEditText();
				}
			}).setNeutralButton(R.string.close_app, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					getActivity().finish();
				}
			}).create().show();
		} else if (haveFavorites) {
			getParentFragmentManager().popBackStack();
			needsOnResultCallback = false;
			onResultFavoriteListener.onResult(result);
		} else {
			if (checkPermissionAndGpsOn()) {
				getParentFragmentManager().popBackStack();
				needsOnResultCallback = false;
				onResultFavoriteListener.onResult(result);
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

	private void checkHaveLocations() {
		weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						processIfNoLocations(result);
					}
				});
			}

			@Override
			public void onResultNoData() {

			}
		});

	}


	private static class MarkerHolder {
		int position;
		MarkerType markerType;

		public MarkerHolder(int position, MarkerType markerType) {
			this.position = position;
			this.markerType = markerType;
		}
	}

	public interface OnResultFavoriteListener {
		void onAddedNewAddress(FavoriteAddressDto newFavoriteAddressDto, List<FavoriteAddressDto> favoriteAddressDtoList, boolean removed);

		void onResult(List<FavoriteAddressDto> favoriteAddressDtoList);

		void onClickedAddress(@Nullable FavoriteAddressDto favoriteAddressDto);
	}

}