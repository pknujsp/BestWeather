package com.lifedawn.bestweather.findaddress;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.databinding.FoundAddressItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class FoundAddressesAdapter extends RecyclerView.Adapter<FoundAddressesAdapter.ViewHolder> implements Filterable {
	private List<Geocoding.AddressDto> itemList = new ArrayList<>();
	private List<Geocoding.AddressDto> filteredAddressList = new ArrayList<>();
	private Set<String> favoriteAddressSet;

	private Filter filter;
	private OnClickedAddressListener onClickedAddressListener;
	private FindAddressFragment.OnAddressListListener onAddressListListener;
	private FindAddressFragment.OnListListener onListListener;

	public FoundAddressesAdapter setFavoriteAddressSet(Set<String> favoriteAddressSet) {
		this.favoriteAddressSet = favoriteAddressSet;
		return this;
	}

	public FoundAddressesAdapter setOnListListener(FindAddressFragment.OnListListener onListListener) {
		this.onListListener = onListListener;
		return this;
	}

	public FoundAddressesAdapter setOnAddressListListener(FindAddressFragment.OnAddressListListener onAddressListListener) {
		this.onAddressListListener = onAddressListListener;
		return this;
	}

	public void setOnClickedAddressListener(OnClickedAddressListener onClickedAddressListener) {
		this.onClickedAddressListener = onClickedAddressListener;
	}

	public void setItemList(List<Geocoding.AddressDto> itemList) {
		this.itemList.clear();
		this.itemList.addAll(itemList);
	}

	@NonNull
	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		FoundAddressItemBinding binding = FoundAddressItemBinding.inflate(LayoutInflater.from(parent.getContext()), null, false);
		binding.getRoot().setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull @NotNull FoundAddressesAdapter.ViewHolder holder, int position) {
		holder.onBind();
	}

	@Override
	public int getItemCount() {
		return filteredAddressList.size();
	}

	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new ListFilter();
		}
		return filter;
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private FoundAddressItemBinding binding;

		public ViewHolder(@NonNull @NotNull FoundAddressItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onListListener.onPOIItemSelectedByList(getBindingAdapterPosition());
				}
			});

			binding.addBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedAddressListener.onClickedAddress(filteredAddressList.get(getBindingAdapterPosition()));
				}
			});

			binding.itemPosition.setVisibility(View.GONE);

		}

		public void onBind() {
			Geocoding.AddressDto address = filteredAddressList.get(getBindingAdapterPosition());

			binding.country.setText(address.country);
			binding.addressName.setText(address.toName());

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

	public interface OnClickedAddressListener {
		void onClickedAddress(Geocoding.AddressDto addressDto);
	}

	private class ListFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults filterResults = new FilterResults();

			if (constraint == null || constraint.length() == 0) {
				filterResults.count = itemList.size();
				filterResults.values = itemList;
			} else {
				List<Geocoding.AddressDto> newAddressList = new ArrayList<>();

				for (Geocoding.AddressDto address : itemList) {
					if (address.displayName.contains(constraint.toString())) {
						newAddressList.add(address);
					}
				}
				filterResults.values = newAddressList;
				filterResults.count = newAddressList.size();
			}
			return filterResults;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filteredAddressList.clear();
			filteredAddressList.addAll((Collection<? extends Geocoding.AddressDto>) results.values);

			onAddressListListener.onSearchedAddressList(filteredAddressList);
			notifyDataSetChanged();

		}
	}
}
