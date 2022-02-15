package com.lifedawn.bestweather.findaddress;

import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FoundAddressItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FoundAddressesAdapter extends RecyclerView.Adapter<FoundAddressesAdapter.ViewHolder> implements Filterable {
	private List<Address> addressList = new ArrayList<>();
	private List<Address> filteredAddressList = new ArrayList<>();
	private Filter filter;
	private OnClickedAddressListener onClickedAddressListener;

	public void setOnClickedAddressListener(OnClickedAddressListener onClickedAddressListener) {
		this.onClickedAddressListener = onClickedAddressListener;
	}

	public void setAddressList(List<Address> addressList) {
		this.addressList.clear();
		this.addressList.addAll(addressList);
	}

	@NonNull
	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		FoundAddressItemBinding binding = FoundAddressItemBinding.inflate(LayoutInflater.from(parent.getContext()));
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

			itemView.getRootView().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedAddressListener.onClickedAddress(filteredAddressList.get(getBindingAdapterPosition()));
				}
			});
		}

		public void onBind() {
			Address address = filteredAddressList.get(getBindingAdapterPosition());

			binding.country.setText(address.getCountryName());
			binding.addressName.setText(address.getAddressLine(0));
		}
	}

	interface OnClickedAddressListener {
		void onClickedAddress(Address address);
	}

	private class ListFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults filterResults = new FilterResults();

			if (constraint == null || constraint.length() == 0) {
				filterResults.count = addressList.size();
				filterResults.values = addressList;
			} else {
				List<Address> newAddressList = new ArrayList<>();

				for (Address address : addressList) {
					if (address.getAddressLine(0).contains(constraint.toString())) {
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
			filteredAddressList.addAll((Collection<? extends Address>) results.values);

			notifyDataSetChanged();

		}
	}
}
