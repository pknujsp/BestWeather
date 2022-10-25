package com.lifedawn.bestweather.favorites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FavoriteAddressItemBinding;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAddressesAdapter extends RecyclerView.Adapter<FavoriteAddressesAdapter.ViewHolder> {
	private List<FavoriteAddressDto> favoriteAddressDtoList = new ArrayList<>();
	private OnClickedAddressListener onClickedAddressListener;

	public void setOnClickedAddressListener(OnClickedAddressListener onClickedAddressListener) {
		this.onClickedAddressListener = onClickedAddressListener;
	}

	public void setFavoriteAddressDtoList(List<FavoriteAddressDto> favoriteAddressDtoList) {
		this.favoriteAddressDtoList.clear();
		this.favoriteAddressDtoList.addAll(favoriteAddressDtoList);
	}

	@NonNull
	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		return new ViewHolder(FavoriteAddressItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull @NotNull FavoriteAddressesAdapter.ViewHolder holder, int position) {
		holder.onBind();
	}

	@Override
	public int getItemCount() {
		return favoriteAddressDtoList.size();
	}

	public List<FavoriteAddressDto> getFavoriteAddressDtoList() {
		return favoriteAddressDtoList;
	}

	protected final class ViewHolder extends RecyclerView.ViewHolder {
		private final FavoriteAddressItemBinding binding;
		private FavoriteAddressDto favoriteAddressDto;

		public ViewHolder(@NonNull @NotNull FavoriteAddressItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedAddressListener.onClicked(favoriteAddressDtoList.get(getBindingAdapterPosition()));
				}
			});

			binding.delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = getBindingAdapterPosition();
					onClickedAddressListener.onClickedDelete(favoriteAddressDtoList.get(position), position);
				}
			});

			binding.markerOnMap.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = getBindingAdapterPosition();
					onClickedAddressListener.onShowMarker(favoriteAddressDtoList.get(position), position);
				}
			});
		}

		public void onBind() {
			favoriteAddressDto = favoriteAddressDtoList.get(getBindingAdapterPosition());
			binding.addressName.setText(favoriteAddressDto.getDisplayName());
			binding.countryName.setText(favoriteAddressDto.getCountryName());
		}
	}

	public interface OnClickedAddressListener {
		void onClickedDelete(FavoriteAddressDto favoriteAddressDto, int position);

		void onClicked(FavoriteAddressDto favoriteAddressDto);

		void onShowMarker(FavoriteAddressDto favoriteAddressDto, int position);
	}
}
