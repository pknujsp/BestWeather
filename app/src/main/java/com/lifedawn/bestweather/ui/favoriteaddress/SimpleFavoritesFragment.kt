package com.lifedawn.bestweather.ui.favoriteaddress

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.databinding.FragmentSimpleFavoritesBinding
import com.lifedawn.bestweather.ui.weathers.viewmodel.GetWeatherViewModel

class SimpleFavoritesFragment : Fragment() {
    private var binding: FragmentSimpleFavoritesBinding? = null
    private var adapter: FavoriteAddressesAdapter? = null
    private var getWeatherViewModel: GetWeatherViewModel? = null
    private var onClickedAddressListener: FavoriteAddressesAdapter.OnClickedAddressListener? = null
    private var bundle: Bundle? = null
    fun setOnClickedAddressListener(onClickedAddressListener: FavoriteAddressesAdapter.OnClickedAddressListener?): SimpleFavoritesFragment {
        this.onClickedAddressListener = onClickedAddressListener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWeatherViewModel = ViewModelProvider(requireActivity()).get<GetWeatherViewModel>(GetWeatherViewModel::class.java)
        bundle = if (arguments != null) arguments else savedInstanceState
        val showCheckBtn = bundle!!.getBoolean("showCheckBtn", false)
        adapter = FavoriteAddressesAdapter(showCheckBtn)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSimpleFavoritesBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.progressResultView.setContentView(binding!!.favoriteAddressList)
        binding!!.favoriteAddressList.addItemDecoration(
            DividerItemDecoration(
                requireContext().applicationContext,
                DividerItemDecoration.VERTICAL
            )
        )
        adapter!!.setOnClickedAddressListener(object : FavoriteAddressesAdapter.OnClickedAddressListener {
            override fun onClickedDelete(favoriteAddressDto: FavoriteAddressDto, position: Int) {
                MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.remove)
                    .setMessage(favoriteAddressDto.displayName)
                    .setPositiveButton(R.string.remove, DialogInterface.OnClickListener { dialog, which ->
                        getWeatherViewModel.delete(favoriteAddressDto, object : DbQueryCallback<Boolean?>() {
                            fun onResultSuccessful(result: Boolean?) {
                                MainThreadWorker.runOnUiThread(Runnable {
                                    onClickedAddressListener!!.onClickedDelete(favoriteAddressDto, position)
                                    dialog.dismiss()
                                })
                            }

                            fun onResultNoData() {}
                        })
                    }).setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() }).create()
                    .show()
            }

            override fun onClicked(favoriteAddressDto: FavoriteAddressDto?) {
                onClickedAddressListener!!.onClicked(favoriteAddressDto)
            }

            override fun onShowMarker(favoriteAddressDto: FavoriteAddressDto?, position: Int) {
                onClickedAddressListener!!.onShowMarker(favoriteAddressDto, position)
            }
        })
        adapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (adapter!!.itemCount == 0) {
                    binding!!.progressResultView.onFailed(getString(R.string.empty_favorite_addresses))
                } else {
                    binding!!.progressResultView.onSuccessful()
                }
            }
        })
        binding!!.favoriteAddressList.adapter = adapter
        getWeatherViewModel.favoriteAddressListLiveData.observe(viewLifecycleOwner, Observer<List<Any>?> { favoriteAddressDtoList ->
            adapter!!.favoriteAddressDtoList = favoriteAddressDtoList
            adapter!!.notifyDataSetChanged()
        })
    }
}