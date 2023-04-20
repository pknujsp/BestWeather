package com.lifedawn.bestweather.ui.findaddress

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.location.FusedLocation
import com.lifedawn.bestweather.commons.views.ProgressDialog
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.databinding.FragmentFindAddressBinding
import com.lifedawn.bestweather.ui.findaddress.map.MapFragment

class FindAddressFragment : Fragment() {
    private var binding: FragmentFindAddressBinding? = null
    private val addressesAdapter = FoundAddressesAdapter()
    private var fusedLocation: FusedLocation? = null
    private var networkStatus: NetworkStatus? = null
    private var locationLifeCycleObserver: LocationLifeCycleObserver? = null
    private var bundle: Bundle? = null
    private var onAddressListListener: OnAddressListListener? = null
    private var onListListener: OnListListener? = null
    private var onClickedAddressListener: FoundAddressesAdapter.OnClickedAddressListener? = null
    private var iBottomSheetState: IBottomSheetState? = null
    private var getWeatherViewModel: GetWeatherViewModel? = null
    val onEditTextQueryListener: OnEditTextQueryListener = object : OnEditTextQueryListener {
        override fun onTextChange(newText: String?) {
            if (newText!!.isEmpty()) return
            MainThreadWorker.runOnUiThread(Runnable { binding!!.progressResultView.onStarted() })
            Geocoding.nominatimGeocoding(requireContext().applicationContext, newText) { addressList ->
                if (activity != null) {
                    MainThreadWorker.runOnUiThread(Runnable {
                        addressesAdapter.setItemList(addressList)
                        addressesAdapter.filter.filter(newText)
                    })
                }
            }
        }

        override fun onTextSubmit(text: String?) {
            if (text!!.isEmpty()) Toast.makeText(context, R.string.empty_search_query, Toast.LENGTH_SHORT).show()
        }
    }

    fun setiBottomSheetState(iBottomSheetState: IBottomSheetState?): FindAddressFragment {
        this.iBottomSheetState = iBottomSheetState
        return this
    }

    fun setOnClickedAddressListener(onClickedAddressListener: FoundAddressesAdapter.OnClickedAddressListener?): FindAddressFragment {
        this.onClickedAddressListener = onClickedAddressListener
        return this
    }

    fun setOnListListener(onListListener: OnListListener?): FindAddressFragment {
        this.onListListener = onListListener
        return this
    }

    fun setOnAddressListListener(onAddressListListener: OnAddressListListener?): FindAddressFragment {
        this.onAddressListListener = onAddressListListener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = if (arguments != null) arguments else savedInstanceState
        fusedLocation = FusedLocation(requireContext().applicationContext)
        networkStatus = NetworkStatus.getInstance(context)
        locationLifeCycleObserver = LocationLifeCycleObserver(requireActivity().activityResultRegistry, requireActivity())
        lifecycle.addObserver(locationLifeCycleObserver)
        getWeatherViewModel = ViewModelProvider(requireActivity()).get<GetWeatherViewModel>(GetWeatherViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFindAddressBinding.inflate(inflater, container, false)
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
        binding!!.progressResultView.setContentView(binding!!.addressList)
        binding!!.progressResultView.onFailed(getString(R.string.title_empty_locations))
        binding!!.addressList.setHasFixedSize(true)
        binding!!.currentLocationBtn.setOnClickListener {
            if (networkStatus.networkAvailable()) {
                ProgressDialog.show(requireActivity(), getString(R.string.msg_finding_current_location)) {
                    fusedLocation.cancel(
                        myLocationCallback
                    )
                }
                fusedLocation.findCurrentLocation(myLocationCallback, false)
            } else {
                Toast.makeText(context, R.string.disconnected_network, Toast.LENGTH_SHORT).show()
            }
        }
        addressesAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                if (addressesAdapter.itemCount == 0) {
                    binding!!.progressResultView.onFailed(getString(R.string.title_empty_locations))
                } else {
                    binding!!.progressResultView.onSuccessful()
                }
            }
        })
        addressesAdapter.setOnClickedAddressListener { addressDto -> onClickedAddressListener!!.onClickedAddress(addressDto) }
        addressesAdapter.setOnAddressListListener(object : OnAddressListListener {
            override fun onSearchedAddressList(addressList: List<Geocoding.AddressDto?>?) {
                onAddressListListener!!.onSearchedAddressList(addressList)
            }
        })
        addressesAdapter.setOnListListener(object : OnListListener {
            override fun onPOIItemSelectedByList(position: Int) {
                onListListener!!.onPOIItemSelectedByList(position)
            }

            override fun onPOIItemSelectedByBottomSheet(position: Int, markerType: MapFragment.MarkerType?) {}
        })
        binding!!.addressList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding!!.addressList.adapter = addressesAdapter
        getWeatherViewModel.getAll(object : DbQueryCallback<List<FavoriteAddressDto?>?>() {
            fun onResultSuccessful(result: List<FavoriteAddressDto?>) {
                val favoriteAddressSet: MutableSet<String> = HashSet()
                for (favoriteAddressDto in result) {
                    favoriteAddressSet.add(favoriteAddressDto.latitude + favoriteAddressDto.longitude)
                }
                addressesAdapter.setFavoriteAddressSet(favoriteAddressSet)
            }

            fun onResultNoData() {}
        })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
        } else {
            iBottomSheetState.setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED)
            iBottomSheetState.setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    override fun onDestroy() {
        ProgressDialog.clearDialogs()
        lifecycle.removeObserver(locationLifeCycleObserver)
        super.onDestroy()
    }

    private val myLocationCallback: FusedLocation.MyLocationCallback = object : MyLocationCallback() {
        fun onSuccessful(locationResult: LocationResult?) {
            val location: Location = getBestLocation(locationResult)
            Geocoding.nominatimReverseGeocoding(requireContext().applicationContext, location.latitude, location.longitude,
                object : ReverseGeocodingCallback() {
                    fun onReverseGeocodingResult(addressDto: Geocoding.AddressDto?) {
                        MainThreadWorker.runOnUiThread(Runnable {
                            ProgressDialog.clearDialogs()
                            if (addressDto != null) {
                                onClickedAddressListener!!.onClickedAddress(addressDto)
                            } else {
                                Toast.makeText(context, R.string.failedFindingLocation, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                })
        }

        fun onFailed(fail: Fail) {
            ProgressDialog.clearDialogs()
            if (fail === Fail.DISABLED_GPS) {
                fusedLocation.onDisabledGps(requireActivity(), locationLifeCycleObserver, ActivityResultCallback<ActivityResult?> {
                    if (fusedLocation.isOnGps) {
                        binding!!.currentLocationBtn.callOnClick()
                    }
                })
            } else if (fail === Fail.DENIED_LOCATION_PERMISSIONS) {
                fusedLocation.onRejectPermissions(requireActivity(), locationLifeCycleObserver, ActivityResultCallback<ActivityResult?> {
                    if (fusedLocation.checkDefaultPermissions()) {
                        binding!!.currentLocationBtn.callOnClick()
                    }
                }, ActivityResultCallback<Map<String?, Boolean?>> { result ->
                    if (!result.containsValue(false)) {
                        binding!!.currentLocationBtn.callOnClick()
                    } else {
                    }
                })
            } else {
                //검색 실패
                Toast.makeText(context, R.string.failedFindingLocation, Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface OnAddressListListener {
        fun onSearchedAddressList(addressList: List<Geocoding.AddressDto?>?)
    }

    interface OnListListener {
        fun onPOIItemSelectedByList(position: Int)
        fun onPOIItemSelectedByBottomSheet(position: Int, markerType: MapFragment.MarkerType?)
    }
}