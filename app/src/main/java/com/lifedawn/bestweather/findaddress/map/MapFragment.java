package com.lifedawn.bestweather.findaddress.map;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alert.AlertFragment;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.views.CustomEditText;
import com.lifedawn.bestweather.databinding.FragmentMapBinding;
import com.lifedawn.bestweather.main.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
	private FragmentMapBinding binding;
	private GoogleMap googleMap;
	private Map<MarkerType, List<Marker>> markerMaps = new HashMap<>();
	private LocationLifeCycleObserver locationLifeCycleObserver;

	protected enum MarkerType {
		LONG_CLICK, SEARCH
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);
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

		binding.searchView.setOnEditTextQueryListener(new CustomEditText.OnEditTextQueryListener() {
			@Override
			public void onTextChange(String newText) {

			}

			@Override
			public void onTextSubmit(String text) {

			}
		});

		binding.searchView.setOnBackClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStack();
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getLifecycle().removeObserver(locationLifeCycleObserver);
	}


	@SuppressLint("MissingPermission")
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		this.googleMap = googleMap;

		googleMap.setPadding(0, MyApplication.getStatusBarHeight() + binding.searchView.getBottom(), 0, 0);
		googleMap.getUiSettings().setZoomControlsEnabled(true);
		googleMap.getUiSettings().setRotateGesturesEnabled(false);
		googleMap.setMyLocationEnabled(true);
		googleMap.setOnMarkerClickListener(this);

		googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(@NonNull LatLng latLng) {
				removeMarkers(MarkerType.LONG_CLICK);
				addMarker(MarkerType.LONG_CLICK, latLng, "");

				Geocoding.geocoding(getContext(), latLng.latitude, latLng.longitude, new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {

					}
				});
			}
		});


		googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
			@Override
			public boolean onMyLocationButtonClick() {
				return false;
			}
		});


		googleMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
			@Override
			public void onMyLocationClick(@NonNull Location location) {

			}
		});
	}

	private void addMarker(MarkerType markerType, LatLng latLng, String title) {
		Marker marker = googleMap.addMarker(new MarkerOptions()
				.position(latLng)
				.title(title));

		if (!markerMaps.containsKey(markerType)) {
			markerMaps.put(markerType, new ArrayList<>());
		}

		markerMaps.get(markerType).add(marker);
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

	@Override
	public boolean onMarkerClick(@NonNull Marker marker) {
		return false;
	}
}