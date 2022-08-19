package com.lifedawn.bestweather.findaddress.map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentMapBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.findaddress.FoundAddressesAdapter;
import com.lifedawn.bestweather.findaddress.map.adapter.FavoriteLocationItemViewPagerAdapter;
import com.lifedawn.bestweather.findaddress.map.adapter.LocationItemViewPagerAdapter;
import com.lifedawn.bestweather.findaddress.map.interfaces.OnClickedLocationBtnListener;
import com.lifedawn.bestweather.findaddress.map.interfaces.OnClickedScrollBtnListener;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import java.util.ArrayList;
import java.util.Collections;
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
	private OnResultFragmentListener onResultFragmentListener;
	private Set<String> favoriteAddressSet = new HashSet<>();

	protected final Map<BottomSheetType, BottomSheetBehavior> bottomSheetBehaviorMap = new HashMap<>();
	protected final Map<BottomSheetType, LinearLayout> bottomSheetViewMap = new HashMap<>();
	protected final Map<MarkerType, RecyclerView.Adapter> adapterMap = new HashMap<>();


	private ViewPager2 locationItemBottomSheetViewPager;
	private boolean removedLocation = false;

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
			super.onFragmentAttached(fm, f, context);
		}

		@Override
		public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof FindAddressFragment) {
				removeMarkers(MarkerType.SEARCH);
				collapseAllExpandedBottomSheets();
			}
		}
	};

	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (!getChildFragmentManager().popBackStackImmediate()) {
				getParentFragmentManager().popBackStackImmediate();
			} else {
			}
		}
	};

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
								MainThreadWorker.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(getContext(), R.string.duplicate_address, Toast.LENGTH_SHORT).show();
									}
								});
							}
						} else {
							weatherViewModel.add(favoriteAddressDto, new DbQueryCallback<Long>() {
								@Override
								public void onResultSuccessful(Long id) {
									favoriteAddressDto.setId(id.intValue());

									if (getActivity() != null) {
										MainThreadWorker.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(getContext(), address.getAddressLine(0), Toast.LENGTH_SHORT).show();
												PreferenceManager.getDefaultSharedPreferences(getContext())
														.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id),
																favoriteAddressDto.getId()).commit();

												Bundle bundle = new Bundle();
												bundle.putBoolean(BundleKey.SelectedAddressDto.name(), true);
												bundle.putBoolean("removedLocation", removedLocation);
												bundle.putString(BundleKey.LastFragment.name(), MapFragment.class.getName());
												bundle.putInt(BundleKey.newFavoriteAddressDtoId.name(), favoriteAddressDto.getId());

												getParentFragmentManager().popBackStack();
												onResultFragmentListener.onResultFragment(bundle);
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

	public enum MarkerType {
		LONG_CLICK, SEARCH, FAVORITE
	}

	public MapFragment setOnResultFragmentListener(OnResultFragmentListener onResultFragmentListener) {
		this.onResultFragmentListener = onResultFragmentListener;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);

		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);

		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		getChildFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentMapBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		setSearchPlacesBottomSheet();
		setLocationItemsBottomSheet();

		binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);

				final int searchBottomSheetHeight =
						(int) (binding.getRoot().getHeight() - binding.header.getBottom() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36f, getResources().getDisplayMetrics()));
				LinearLayout locationSearchBottomSheet = bottomSheetViewMap.get(BottomSheetType.SEARCH_LOCATION);

				locationSearchBottomSheet.getLayoutParams().height = searchBottomSheetHeight;
				locationSearchBottomSheet.requestLayout();
			}
		});


		binding.header.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
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
				bundle.putString(BundleKey.RequestFragment.name(), FavoritesFragment.class.getName());
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

				childFragmentManager.beginTransaction().
						add(binding.bottomSheetSearchPlace.searchFragmentContainer.getId(), findAddressFragment,
								getString(R.string.tag_find_address_fragment)).
						addToBackStack(
								getString(R.string.tag_find_address_fragment)).
						commitAllowingStateLoss();

				setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_EXPANDED);
			}
		});

		binding.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressedCallback.handleOnBackPressed();
			}
		});
	}

	@Override
	public void onDestroy() {
		getLifecycle().removeObserver(locationLifeCycleObserver);
		getChildFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		onBackPressedCallback.remove();

		Bundle bundle = new Bundle();
		bundle.putBoolean(BundleKey.SelectedAddressDto.name(), false);
		bundle.putBoolean("removedLocation", removedLocation);
		bundle.putString(BundleKey.LastFragment.name(), MapFragment.class.getName());

		onResultFragmentListener.onResultFragment(bundle);

		super.onDestroy();
	}


	@SuppressLint("MissingPermission")
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		this.googleMap = googleMap;

		googleMap.setPadding(0, MyApplication.getStatusBarHeight() + binding.header.getBottom(), 0, 0);
		googleMap.getUiSettings().setZoomControlsEnabled(true);
		googleMap.getUiSettings().setRotateGesturesEnabled(false);
		googleMap.setMyLocationEnabled(true);
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

		initFavorites();
	}

	private void initFavorites() {
		weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						FavoriteLocationItemViewPagerAdapter adapter = new FavoriteLocationItemViewPagerAdapter();
						adapterMap.put(MarkerType.FAVORITE, adapter);
						adapter.setAddressList(result);
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
																	initFavorites();
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

			@Override
			public void onResultNoData() {

			}
		});
	}

	private void onLongClicked(LatLng latLng) {
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

						collapseAllExpandedBottomSheets();
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
				googleMap.setPadding(0, MyApplication.getStatusBarHeight() + binding.header.getBottom(), 0, translationValue);
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


	private static class MarkerHolder {
		int position;
		MarkerType markerType;

		public MarkerHolder(int position, MarkerType markerType) {
			this.position = position;
			this.markerType = markerType;
		}
	}
}