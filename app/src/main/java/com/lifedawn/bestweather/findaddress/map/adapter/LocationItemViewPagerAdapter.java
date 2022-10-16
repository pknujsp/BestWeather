package com.lifedawn.bestweather.findaddress.map.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.databinding.FoundAddressItemViewpagerBinding;
import com.lifedawn.bestweather.findaddress.map.interfaces.OnClickedLocationBtnListener;
import com.lifedawn.bestweather.findaddress.map.interfaces.OnClickedScrollBtnListener;

import java.util.List;
import java.util.Set;

public class LocationItemViewPagerAdapter extends RecyclerView.Adapter<LocationItemViewPagerAdapter.LocationItemViewHolder> {
	private Set<String> favoriteAddressSet;
	private List<Geocoding.AddressDto> addressList;
	private OnClickedLocationBtnListener<Geocoding.AddressDto> onClickedLocationBtnListener;
	private OnClickedScrollBtnListener onClickedScrollBtnListener;

	public LocationItemViewPagerAdapter setOnClickedLocationBtnListener(OnClickedLocationBtnListener<Geocoding.AddressDto> onClickedLocationBtnListener) {
		this.onClickedLocationBtnListener = onClickedLocationBtnListener;
		return this;
	}

	public LocationItemViewPagerAdapter setOnClickedScrollBtnListener(OnClickedScrollBtnListener onClickedScrollBtnListener) {
		this.onClickedScrollBtnListener = onClickedScrollBtnListener;
		return this;
	}

	public LocationItemViewPagerAdapter setAddressList(List<Geocoding.AddressDto> addressList) {
		this.addressList = addressList;
		return this;
	}

	public List<Geocoding.AddressDto> getAddressList() {
		return addressList;
	}

	public LocationItemViewPagerAdapter setFavoriteAddressSet(Set<String> favoriteAddressSet) {
		this.favoriteAddressSet = favoriteAddressSet;
		return this;
	}

	@NonNull
	@Override
	public LocationItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new LocationItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.found_address_item_viewpager, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull LocationItemViewHolder holder, int position) {
		holder.setDataView();
	}

	@Override
	public int getItemCount() {
		return addressList.size();
	}


	class LocationItemViewHolder extends RecyclerView.ViewHolder {
		protected FoundAddressItemViewpagerBinding binding;

		public LocationItemViewHolder(@NonNull View view) {
			super(view);
			binding = FoundAddressItemViewpagerBinding.bind(view);
			binding.removeBtn.setVisibility(View.GONE);

			binding.addBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					onClickedLocationBtnListener.onSelected(addressList.get(getBindingAdapterPosition()), false);
				}
			});

			binding.left.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedScrollBtnListener.toLeft();
				}
			});

			binding.right.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedScrollBtnListener.toRight();
				}
			});
		}

		void setDataView() {
			final int position = getBindingAdapterPosition();
			String itemPosition = (position + 1) + " / " + getItemCount();
			binding.itemPosition.setText(itemPosition);

			Geocoding.AddressDto address = addressList.get(position);

			binding.addressName.setText(address.displayName);
			binding.country.setText(address.country);

			if (favoriteAddressSet.contains(address.latitude + "" + address.longitude)) {
				binding.addBtn.setClickable(false);
				binding.addBtn.setText(R.string.duplicate);
				binding.addBtn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
			} else {
				binding.addBtn.setClickable(true);
				binding.addBtn.setText(R.string.add);
				binding.addBtn.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
			}
		}
	}
}