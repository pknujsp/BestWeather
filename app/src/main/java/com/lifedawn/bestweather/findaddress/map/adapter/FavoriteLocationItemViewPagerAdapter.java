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
import com.lifedawn.bestweather.databinding.FoundAddressItemViewpagerBinding;
import com.lifedawn.bestweather.findaddress.map.interfaces.OnClickedLocationBtnListener;
import com.lifedawn.bestweather.findaddress.map.interfaces.OnClickedScrollBtnListener;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import java.util.List;

public class FavoriteLocationItemViewPagerAdapter extends RecyclerView.Adapter<FavoriteLocationItemViewPagerAdapter.FavoriteItemViewHolder> {
	private List<FavoriteAddressDto> addressList;
	private OnClickedLocationBtnListener<FavoriteAddressDto> onClickedLocationBtnListener;
	private OnClickedScrollBtnListener onClickedScrollBtnListener;

	public FavoriteLocationItemViewPagerAdapter setAddressList(List<FavoriteAddressDto> addressList) {
		this.addressList = addressList;
		return this;
	}

	public FavoriteLocationItemViewPagerAdapter setOnClickedLocationBtnListener(OnClickedLocationBtnListener<FavoriteAddressDto> onClickedLocationBtnListener) {
		this.onClickedLocationBtnListener = onClickedLocationBtnListener;
		return this;
	}

	public FavoriteLocationItemViewPagerAdapter setOnClickedScrollBtnListener(OnClickedScrollBtnListener onClickedScrollBtnListener) {
		this.onClickedScrollBtnListener = onClickedScrollBtnListener;
		return this;
	}

	public List<FavoriteAddressDto> getAddressList() {
		return addressList;
	}

	@NonNull
	@Override
	public FavoriteItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new FavoriteItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.found_address_item_viewpager, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull FavoriteItemViewHolder holder, int position) {
		holder.setDataView();
	}

	@Override
	public int getItemCount() {
		return addressList.size();
	}


	class FavoriteItemViewHolder extends RecyclerView.ViewHolder {
		protected FoundAddressItemViewpagerBinding binding;

		public FavoriteItemViewHolder(@NonNull View view) {
			super(view);
			binding = FoundAddressItemViewpagerBinding.bind(view);

			binding.addBtn.setText(R.string.remove);
			binding.addBtn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

			binding.addBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					onClickedLocationBtnListener.onSelected(addressList.get(getBindingAdapterPosition()), true);
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

			FavoriteAddressDto address = addressList.get(position);

			binding.addressName.setText(address.getAddress());
			binding.country.setText(address.getCountryName());

		}
	}
}

