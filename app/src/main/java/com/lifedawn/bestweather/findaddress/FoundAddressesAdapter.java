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
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.found_address_item, null));
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
		private TextView countryTextView;
		private TextView addressTextView;

		public ViewHolder(@NonNull @NotNull View itemView) {
			super(itemView);
			countryTextView = (TextView) itemView.findViewById(R.id.country);
			addressTextView = (TextView) itemView.findViewById(R.id.address_name);

			itemView.getRootView().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedAddressListener.onClickedAddress(filteredAddressList.get(getAdapterPosition()));
				}
			});
		}

		public void onBind() {
			Address address = filteredAddressList.get(getAdapterPosition());

			countryTextView.setText(address.getCountryName());
			addressTextView.setText(address.getAddressLine(0));
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
