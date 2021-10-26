package com.lifedawn.bestweather.favorites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAddressesAdapter extends RecyclerView.Adapter<FavoriteAddressesAdapter.ViewHolder> {
	private List<FavoriteAddressDto> favoriteAddressDtoList = new ArrayList<>();
	private OnDeleteListener onDeleteListener;

	public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
		this.onDeleteListener = onDeleteListener;
	}

	public void setFavoriteAddressDtoList(List<FavoriteAddressDto> favoriteAddressDtoList) {
		this.favoriteAddressDtoList.clear();
		this.favoriteAddressDtoList.addAll(favoriteAddressDtoList);
	}

	@NonNull
	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_address_item, null));
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
	
	class ViewHolder extends RecyclerView.ViewHolder {
		private TextView addressTextView;
		private ImageButton deleteBtn;

		public ViewHolder(@NonNull @NotNull View itemView) {
			super(itemView);

			addressTextView = (TextView) itemView.findViewById(R.id.address_name);
			deleteBtn = (ImageButton) itemView.findViewById(R.id.delete);
			deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = getAdapterPosition();
					onDeleteListener.onClickedDelete(favoriteAddressDtoList.get(position), position);
					favoriteAddressDtoList.remove(position);
					notifyDataSetChanged();
				}
			});
		}

		public void onBind() {
			addressTextView.setText(favoriteAddressDtoList.get(getAdapterPosition()).getAddress());
		}
	}

	public interface OnDeleteListener {
		void onClickedDelete(FavoriteAddressDto favoriteAddressDto, int position);
	}
}
