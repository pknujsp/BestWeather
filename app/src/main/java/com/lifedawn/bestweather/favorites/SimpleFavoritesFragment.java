package com.lifedawn.bestweather.favorites;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.databinding.FragmentSimpleFavoritesBinding;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import java.util.List;


public class SimpleFavoritesFragment extends Fragment {
	private FragmentSimpleFavoritesBinding binding;
	private FavoriteAddressesAdapter adapter;
	private WeatherViewModel weatherViewModel;
	private FavoriteAddressesAdapter.OnClickedAddressListener onClickedAddressListener;
	private Bundle bundle;

	public SimpleFavoritesFragment setOnClickedAddressListener(FavoriteAddressesAdapter.OnClickedAddressListener onClickedAddressListener) {
		this.onClickedAddressListener = onClickedAddressListener;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		weatherViewModel = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);

		bundle = getArguments() != null ? getArguments() : savedInstanceState;
		boolean showCheckBtn = bundle.getBoolean("showCheckBtn", false);
		adapter = new FavoriteAddressesAdapter(showCheckBtn);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentSimpleFavoritesBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.progressResultView.setContentView(binding.favoriteAddressList);

		binding.favoriteAddressList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		binding.favoriteAddressList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

		adapter.setOnClickedAddressListener(new FavoriteAddressesAdapter.OnClickedAddressListener() {
			@Override
			public void onClickedDelete(FavoriteAddressDto favoriteAddressDto, int position) {
				new MaterialAlertDialogBuilder(requireActivity()).
						setTitle(R.string.remove)
						.setMessage(favoriteAddressDto.getDisplayName()).
						setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								weatherViewModel.delete(favoriteAddressDto, new DbQueryCallback<Boolean>() {
									@Override
									public void onResultSuccessful(Boolean result) {
										MainThreadWorker.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												onClickedAddressListener.onClickedDelete(favoriteAddressDto, position);
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

			@Override
			public void onClicked(FavoriteAddressDto favoriteAddressDto) {
				onClickedAddressListener.onClicked(favoriteAddressDto);
			}

			@Override
			public void onShowMarker(FavoriteAddressDto favoriteAddressDto, int position) {
				onClickedAddressListener.onShowMarker(favoriteAddressDto, position);
			}
		});
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();

				if (adapter.getItemCount() == 0) {
					binding.progressResultView.onFailed(getString(R.string.empty_favorite_addresses));
				} else {
					binding.progressResultView.onSuccessful();
				}
			}

		});

		binding.favoriteAddressList.setAdapter(adapter);

		weatherViewModel.favoriteAddressListLiveData.observe(getViewLifecycleOwner(), new Observer<List<FavoriteAddressDto>>() {
			@Override
			public void onChanged(List<FavoriteAddressDto> favoriteAddressDtoList) {
				adapter.setFavoriteAddressDtoList(favoriteAddressDtoList);
				adapter.notifyDataSetChanged();
			}
		});
	}

}