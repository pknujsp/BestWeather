package com.lifedawn.bestweather.weathers.simplefragment.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.TextUtil;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.flickr.FlickrImgObj;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.flickr.PhotosFromGalleryResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.TimeZone;

public class BaseSimpleCurrentConditionsFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutSimpleCurrentConditionsBinding binding;
	protected GeolocalizedFeedResponse airQualityResponse;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	protected Double latitude;
	protected Double longitude;
	protected String addressName;
	protected String countryCode;
	protected WeatherSourceType mainWeatherSourceType;
	protected ZoneId zoneId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name()));
		visibilityUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name()));
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock24.name()));

		Bundle bundle = getArguments();
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		addressName = bundle.getString(BundleKey.AddressName.name());
		countryCode = bundle.getString(BundleKey.CountryCode.name());
		mainWeatherSourceType = (WeatherSourceType) bundle.getSerializable(
				BundleKey.WeatherDataSource.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutSimpleCurrentConditionsBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		/*
		MobileAds.initialize(getContext());
		AdRequest adRequest = new AdRequest.Builder().build();
		binding.adView.loadAd(adRequest);

		 */
		binding.flickrLayout.setVisibility(View.GONE);
		binding.flickrImageUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (binding.flickrImageUrl.getTag() != null) {
					String url = (String) binding.flickrImageUrl.getTag();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					startActivity(intent);
				}

			}
		});
	}

	public BaseSimpleCurrentConditionsFragment setAirQualityResponse(GeolocalizedFeedResponse airQualityResponse) {
		this.airQualityResponse = airQualityResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		setAqiValuesToViews();
	}

	public void setAqiValuesToViews() {
		if (airQualityResponse != null) {
			if (airQualityResponse.getStatus().equals("ok")) {
				GeolocalizedFeedResponse.Data.IAqi iAqi = airQualityResponse.getData().getIaqi();
				int val = Integer.MIN_VALUE;

				if (iAqi.getO3() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getO3().getValue()));
				}
				if (iAqi.getPm25() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getPm25().getValue()));
				}
				if (iAqi.getPm10() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getPm10().getValue()));
				}
				if (iAqi.getNo2() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getNo2().getValue()));
				}
				if (iAqi.getSo2() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getSo2().getValue()));
				}
				if (iAqi.getCo() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getCo().getValue()));
				}
				if (iAqi.getDew() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getDew().getValue()));
				}

				if (val == Integer.MIN_VALUE) {
					binding.airQuality.setText(R.string.not_data);
				} else {
					binding.airQuality.setText(AqicnResponseProcessor.getGradeDescription(val));
				}
			} else {
				binding.airQuality.setText(R.string.not_data);
			}
		} else {
			binding.airQuality.setText(R.string.not_data);
		}
	}

	public void setFlickrImgInfo(FlickrImgObj flickrImgInfo) {
		final String text = flickrImgInfo.getPhoto().getOwner();
		binding.flickrImageUrl.setText(TextUtil.getUnderLineColorText(text, text,
				ContextCompat.getColor(getContext(), R.color.white)));
		binding.flickrImageUrl.setTag(flickrImgInfo.getRealFlickrUrl());

		binding.flickrLayout.setVisibility(View.VISIBLE);
	}
}