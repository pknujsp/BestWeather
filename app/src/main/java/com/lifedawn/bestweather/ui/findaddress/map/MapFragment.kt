package com.lifedawn.bestweather.ui.findaddress.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.alarm.AlarmSettingsFragment
import com.lifedawn.bestweather.commons.views.BaseFragment
import com.lifedawn.bestweather.commons.views.ProgressDialog
import com.lifedawn.bestweather.databinding.FragmentMapBinding
import com.lifedawn.bestweather.ui.favoriteaddress.FavoriteAddressesAdapter
import com.lifedawn.bestweather.ui.findaddress.FoundAddressesAdapter
import java.util.*

class MapFragment : BaseFragment<FragmentMapBinding>(R.layout.fragment_map), OnMapReadyCallback, OnMarkerClickListener, FoundAddressesAdapter
    .OnClickedAddressListener, OnListListener,
    IBottomSheetState {
    private var googleMap: GoogleMap? = null
    private val markerMaps: MutableMap<MarkerType?, MutableList<Marker>> = HashMap()
    private var locationLifeCycleObserver: LocationLifeCycleObserver? = null
    private var getWeatherViewModel: GetWeatherViewModel? = null
    private var onResultFavoriteListener: OnResultFavoriteListener? = null
    private val favoriteAddressSet: MutableSet<String> = HashSet()
    protected val bottomSheetBehaviorMap: MutableMap<BottomSheetType, BottomSheetBehavior<*>> =
        HashMap<BottomSheetType, BottomSheetBehavior<*>>()
    protected val bottomSheetViewMap: MutableMap<BottomSheetType, LinearLayout> = HashMap()
    protected val adapterMap: MutableMap<MarkerType, RecyclerView.Adapter<*>> = HashMap<MarkerType, RecyclerView.Adapter<*>>()
    private var fusedLocation: FusedLocation? = null
    private var locationItemBottomSheetViewPager: ViewPager2? = null
    private var removedLocation = false
    private var enableCurrentLocation = false
    private val addedNewLocation = false
    private val refresh = false
    val isClickedItem = false
    private var requestFragment: String? = null
    private var clickedHeader = false
    private var bundle: Bundle? = null
    private var showFavoriteAddBtn = false
    fun setOnResultFavoriteListener(onResultFavoriteListener: OnResultFavoriteListener?): MapFragment {
        this.onResultFavoriteListener = onResultFavoriteListener
        return this
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!childFragmentManager.popBackStackImmediate()) {
                if (requestFragment == MainFragment::class.java.getName() || requestFragment == WeatherFragment::class.java.getName()) {
                    checkHaveLocations()
                } else if (requestFragment == OngoingNotificationSettingsFragment::class.java.getName() || requestFragment == DailyNotificationSettingsFragment::class.java.getName() || requestFragment == ConfigureWidgetActivity::class.java.getName()) {
                    onResultFavoriteListener!!.onClickedAddress(null)
                    parentFragmentManager.popBackStack()
                } else {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }
    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentResumed(fm, f)
                if (f is FindAddressFragment) {
                    binding!!.headerLayout.requestFocusEditText()
                }
            }

            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentDestroyed(fm, f)
                if (f is FindAddressFragment) {
                    clickedHeader = false
                    if (activity!!.currentFocus != null) DeviceUtils.Companion.hideKeyboard(
                        requireContext().applicationContext, requireActivity().currentFocus!!
                            .windowToken
                    )
                    binding!!.headerLayout.clearFocusEditText()
                    removeMarkers(MarkerType.SEARCH)
                    collapseAllExpandedBottomSheets()
                } else if (f is SimpleFavoritesFragment) {
                    collapseAllExpandedBottomSheets()
                }
            }
        }

    override fun onClickedAddress(addressDto: Geocoding.AddressDto?) {
        ProgressDialog.show(requireActivity(), getString(R.string.adding_a_new_favorite_location), null)
        val latitude: Double = addressDto.latitude
        val longitude: Double = addressDto.longitude
        val favoriteAddressDto = FavoriteAddressDto()
        favoriteAddressDto.countryName = addressDto.country
        favoriteAddressDto.countryCode = if (addressDto.countryCode == null) "" else addressDto.countryCode
        favoriteAddressDto.displayName = addressDto.displayName
        favoriteAddressDto.latitude = latitude.toString()
        favoriteAddressDto.longitude = longitude.toString()
        TimeZoneUtils.INSTANCE.getTimeZone(latitude, longitude) { zoneId ->
            favoriteAddressDto.zoneId = zoneId.getId()
            add(favoriteAddressDto)
        }
    }

    private fun add(favoriteAddressDto: FavoriteAddressDto) {
        getWeatherViewModel.contains(favoriteAddressDto.latitude, favoriteAddressDto.longitude,
            object : DbQueryCallback<Boolean?>() {
                fun onResultSuccessful(contains: Boolean) {
                    if (contains) {
                        if (activity != null) {
                            MainThreadWorker.runOnUiThread(Runnable {
                                ProgressDialog.clearDialogs()
                                Toast.makeText(context, R.string.duplicate_address, Toast.LENGTH_SHORT).show()
                            })
                        }
                    } else {
                        MainThreadWorker.runOnUiThread(Runnable {
                            getWeatherViewModel.favoriteAddressListLiveData.removeObservers(
                                viewLifecycleOwner
                            )
                        })
                        getWeatherViewModel.add(favoriteAddressDto, object : DbQueryCallback<Long?>() {
                            fun onResultSuccessful(id: Long) {
                                if (activity != null) {
                                    favoriteAddressDto.id = id.toInt()
                                    PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                                        .edit().putInt(
                                            getString(R.string.pref_key_last_selected_favorite_address_id),
                                            favoriteAddressDto.id
                                        ).putString(
                                            getString(R.string.pref_key_last_selected_location_type),
                                            LocationType.SelectedAddress.name
                                        ).commit()
                                    try {
                                        Thread.sleep(900)
                                    } catch (e: InterruptedException) {
                                        e.printStackTrace()
                                    }
                                    MainThreadWorker.runOnUiThread(Runnable {
                                        Toast.makeText(context, favoriteAddressDto.displayName, Toast.LENGTH_SHORT).show()
                                        ProgressDialog.clearDialogs()
                                        onResultFavoriteListener!!.onAddedNewAddress(favoriteAddressDto, null, removedLocation)
                                    })
                                }
                            }

                            fun onResultNoData() {}
                        })
                    }
                }

                fun onResultNoData() {}
            })
    }

    enum class MarkerType {
        LONG_CLICK, SEARCH, FAVORITE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity!!.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationLifeCycleObserver = LocationLifeCycleObserver(requireActivity().activityResultRegistry, requireActivity())
        lifecycle.addObserver(locationLifeCycleObserver)
        getWeatherViewModel = ViewModelProvider(requireActivity()).get<GetWeatherViewModel>(GetWeatherViewModel::class.java)
        childFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)
        fusedLocation = FusedLocation(requireContext().applicationContext)
        bundle = if (arguments != null) arguments else savedInstanceState
        requestFragment = bundle!!.getString(BundleKey.RequestFragment.name)
        if (requestFragment == ConfigureWidgetActivity::class.java.getName() || requestFragment == OngoingNotificationSettingsFragment::class.java.getName() || requestFragment == AlarmSettingsFragment::class.java.getName() || requestFragment == DailyNotificationSettingsFragment::class.java.getName()) {
            showFavoriteAddBtn = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val supportMapFragment: SupportMapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction().add(binding!!.mapFragmentContainer.id, supportMapFragment).commitAllowingStateLoss()
        supportMapFragment.getMapAsync(this)
        setSearchPlacesBottomSheet()
        setLocationItemsBottomSheet()
        setFavoritesBottomSheet()
        binding!!.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding!!.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val searchBottomSheetHeight = (binding!!.root.height - binding!!.headerLayout.bottom - TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics
                )).toInt()
                val locationSearchBottomSheet = bottomSheetViewMap[BottomSheetType.SEARCH_LOCATION]
                val favoritesBottomSheet = bottomSheetViewMap[BottomSheetType.FAVORITES]
                locationSearchBottomSheet!!.layoutParams.height = searchBottomSheetHeight
                favoritesBottomSheet!!.layoutParams.height = searchBottomSheetHeight
                locationSearchBottomSheet.requestLayout()
                favoritesBottomSheet.requestLayout()
            }
        })
        binding!!.headerLayout.setEditTextOnFocusListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                if (clickedHeader) {
                    return@setEditTextOnFocusListener
                }
                clickedHeader = true
                val childFragmentManager = childFragmentManager
                if (childFragmentManager.findFragmentByTag(getString(R.string.tag_find_address_fragment)) != null) {
                    return@setEditTextOnFocusListener
                }
                val backStackCount = childFragmentManager.backStackEntryCount
                for (count in 0 until backStackCount) {
                    childFragmentManager.popBackStack()
                }
                val findAddressFragment = FindAddressFragment()
                val bundle = Bundle()
                bundle.putString(BundleKey.RequestFragment.name, MapFragment::class.java.name)
                findAddressFragment.setArguments(bundle)
                findAddressFragment.setOnAddressListListener(OnAddressListListener { addressList: List<Geocoding.AddressDto?> ->
                    if (bottomSheetBehaviorMap[BottomSheetType.LOCATION_ITEM].getState() != BottomSheetBehavior.STATE_COLLAPSED) setStateOfBottomSheet(
                        BottomSheetType.LOCATION_ITEM,
                        BottomSheetBehavior.STATE_COLLAPSED
                    )
                    removeMarkers(MarkerType.SEARCH)
                    val adapter = LocationItemViewPagerAdapter()
                    adapterMap[MarkerType.SEARCH] = adapter
                    adapter.setFavoriteAddressSet(favoriteAddressSet)
                    adapter.setAddressList(addressList)
                    adapter.setOnClickedLocationBtnListener(OnClickedLocationBtnListener<Geocoding.AddressDto> { e: Geocoding.AddressDto?, remove: Boolean ->
                        onClickedAddress(
                            e
                        )
                    })
                    adapter.setOnClickedScrollBtnListener(object : OnClickedScrollBtnListener {
                        override fun toLeft() {
                            if (binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem > 0) {
                                binding!!.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
                                    binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem - 1, true
                                )
                            }
                        }

                        override fun toRight() {
                            if (binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem < adapter.getItemCount() - 1) {
                                binding!!.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
                                    binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem + 1, true
                                )
                            }
                        }
                    })
                    var i = 0
                    for (address in addressList) {
                        addMarker(MarkerType.SEARCH, i++, address)
                    }
                    showMarkers(MarkerType.SEARCH)
                })
                findAddressFragment.setiBottomSheetState(this@MapFragment)
                findAddressFragment.setOnClickedAddressListener(this@MapFragment)
                findAddressFragment.setOnListListener(this@MapFragment)
                binding!!.headerLayout.setOnEditTextQueryListener(findAddressFragment.getOnEditTextQueryListener())
                childFragmentManager.beginTransaction().add(
                    binding!!.bottomSheetSearchPlace.searchFragmentContainer.id, findAddressFragment,
                    getString(R.string.tag_find_address_fragment)
                ).addToBackStack(
                    getString(R.string.tag_find_address_fragment)
                ).commitAllowingStateLoss()
                setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_EXPANDED)
            }
        }
        binding!!.favorite.setOnClickListener { view1: View? ->
            val childFragmentManager = childFragmentManager
            if (childFragmentManager.findFragmentByTag(SimpleFavoritesFragment::class.java.getName()) != null) {
                return@setOnClickListener
            }
            collapseAllExpandedBottomSheets()
            val backStackCount = childFragmentManager.backStackEntryCount
            for (count in 0 until backStackCount) {
                childFragmentManager.popBackStack()
            }
            val simpleFavoritesFragment = SimpleFavoritesFragment()
            val bundle = Bundle()
            bundle.putBoolean("showCheckBtn", showFavoriteAddBtn)
            simpleFavoritesFragment.setArguments(bundle)
            simpleFavoritesFragment.setOnClickedAddressListener(object : FavoriteAddressesAdapter.OnClickedAddressListener {
                override fun onClickedDelete(favoriteAddressDto: FavoriteAddressDto, position: Int) {
                    removedLocation = true
                }

                override fun onClicked(favoriteAddressDto: FavoriteAddressDto?) {
                    if (showFavoriteAddBtn) {
                        onResultFavoriteListener!!.onClickedAddress(favoriteAddressDto)
                    }
                }

                override fun onShowMarker(favoriteAddressDto: FavoriteAddressDto?, position: Int) {
                    setStateOfBottomSheet(BottomSheetType.FAVORITES, BottomSheetBehavior.STATE_COLLAPSED)
                    getChildFragmentManager().popBackStack()
                    val marker = markerMaps[MarkerType.FAVORITE]!![position]
                    onMarkerClick(marker)
                }
            })
            childFragmentManager.beginTransaction().add(
                binding!!.favoritesBottomSheet.fragmentContainer.id, simpleFavoritesFragment,
                SimpleFavoritesFragment::class.java.getName()
            ).addToBackStack(
                SimpleFavoritesFragment::class.java.getName()
            ).commitAllowingStateLoss()
            setStateOfBottomSheet(BottomSheetType.FAVORITES, BottomSheetBehavior.STATE_EXPANDED)
        }
        binding!!.headerLayout.setOnBackClickListener { v: View? -> onBackPressedCallback.handleOnBackPressed() }
        if (requestFragment == MainFragment::class.java.getName()) {
            binding!!.favorite.callOnClick()
        }
    }

    override fun onDestroy() {
        lifecycle.removeObserver(locationLifeCycleObserver)
        childFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        onBackPressedCallback.remove()
        super.onDestroy()
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.getUiSettings().setZoomControlsEnabled(false)
        googleMap.getUiSettings().setRotateGesturesEnabled(false)
        googleMap.getUiSettings().setMyLocationButtonEnabled(false)
        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnMapClickListener(OnMapClickListener { latLng: LatLng? ->
            setStateOfBottomSheet(
                BottomSheetType.LOCATION_ITEM,
                BottomSheetBehavior.STATE_COLLAPSED
            )
        })
        googleMap.setOnMapLongClickListener(OnMapLongClickListener { latLng: LatLng -> onLongClicked(latLng) })
        binding!!.mapButtons.zoomInBtn.setOnClickListener { view: View? -> googleMap.animateCamera(CameraUpdateFactory.zoomIn()) }
        binding!!.mapButtons.zoomOutBtn.setOnClickListener { view: View? -> googleMap.animateCamera(CameraUpdateFactory.zoomOut()) }
        binding!!.mapButtons.currentLocationBtn.setOnClickListener { v: View? ->
            fusedLocation.findCurrentLocation(object : MyLocationCallback() {
                fun onSuccessful(locationResult: LocationResult?) {
                    val result: Location = getBestLocation(locationResult)
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(result.latitude, result.longitude), 10f
                        )
                    )
                }

                fun onFailed(fail: Fail) {
                    if (fail === Fail.DISABLED_GPS) {
                        fusedLocation.onDisabledGps(
                            requireActivity(),
                            locationLifeCycleObserver,
                            ActivityResultCallback { result: ActivityResult? ->
                                if (fusedLocation.isOnGps) {
                                    binding!!.mapButtons.currentLocationBtn.callOnClick()
                                }
                            })
                    } else if (fail === Fail.DENIED_LOCATION_PERMISSIONS) {
                        fusedLocation.onRejectPermissions(
                            requireActivity(),
                            locationLifeCycleObserver,
                            ActivityResultCallback { result: ActivityResult? ->
                                if (fusedLocation.checkDefaultPermissions()) {
                                    binding!!.mapButtons.currentLocationBtn.callOnClick()
                                }
                            },
                            ActivityResultCallback<Map<String?, Boolean?>> { result -> //gps사용 권한
                                //허가남 : 현재 위치 다시 파악
                                //거부됨 : 작업 취소
                                //계속 거부 체크됨 : 작업 취소
                                if (!result.containsValue(false)) {
                                    binding!!.mapButtons.currentLocationBtn.callOnClick()
                                }
                            })
                    }
                }
            }, false)
        }
        val adapter = FavoriteLocationItemViewPagerAdapter()
        adapterMap[MarkerType.FAVORITE] = adapter
        adapter.setOnClickedLocationBtnListener(OnClickedLocationBtnListener<FavoriteAddressDto> { e: FavoriteAddressDto, remove: Boolean ->
            if (remove) {
                MaterialAlertDialogBuilder(activity).setTitle(R.string.remove)
                    .setMessage(e.displayName)
                    .setPositiveButton(R.string.remove, DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                        getWeatherViewModel.delete(e, object : DbQueryCallback<Boolean?>() {
                            fun onResultSuccessful(result: Boolean?) {
                                MainThreadWorker.runOnUiThread(Runnable {
                                    setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED)
                                    removedLocation = true
                                    dialog.dismiss()
                                })
                            }

                            fun onResultNoData() {}
                        })
                    }).setNegativeButton(
                        R.string.cancel,
                        DialogInterface.OnClickListener { dialog: DialogInterface, which: Int -> dialog.dismiss() }).create().show()
            } else {
                onResultFavoriteListener!!.onClickedAddress(e)
            }
        })
        adapter.setOnClickedScrollBtnListener(object : OnClickedScrollBtnListener {
            override fun toLeft() {
                if (binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem > 0) {
                    binding!!.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
                        binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem - 1, true
                    )
                }
            }

            override fun toRight() {
                if (binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem < adapter.getItemCount() - 1) {
                    binding!!.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
                        binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem + 1, true
                    )
                }
            }
        })
        getWeatherViewModel.favoriteAddressListLiveData.observe(viewLifecycleOwner) { result ->
            if (!addedNewLocation) {
                val adapter1: FavoriteLocationItemViewPagerAdapter = adapterMap[MarkerType.FAVORITE] as FavoriteLocationItemViewPagerAdapter
                adapter1.setAddressList(result)
                adapter1.setShowAddBtn(showFavoriteAddBtn)
                favoriteAddressSet.clear()
                for (favoriteAddressDto in result) {
                    favoriteAddressSet.add(favoriteAddressDto.latitude + favoriteAddressDto.longitude)
                }
                removeMarkers(MarkerType.FAVORITE)
                for (i in 0 until result.size()) {
                    addFavoriteMarker(i, result.get(i))
                }
            }
        }
    }

    private fun onLongClicked(latLng: LatLng) {
        collapseAllExpandedBottomSheets()
        Geocoding.nominatimReverseGeocoding(requireContext().applicationContext, latLng.latitude, latLng.longitude,
            error.NonExistentClass { addressDto ->
                MainThreadWorker.runOnUiThread(Runnable {
                    val addresses: MutableList<Geocoding.AddressDto> = ArrayList<Geocoding.AddressDto>()
                    addresses.add(addressDto)
                    val adapter = LocationItemViewPagerAdapter()
                    adapterMap[MarkerType.LONG_CLICK] = adapter
                    adapter.setAddressList(addresses)
                    adapter.setFavoriteAddressSet(favoriteAddressSet)
                    adapter.setOnClickedLocationBtnListener(OnClickedLocationBtnListener<Geocoding.AddressDto> { e: Geocoding.AddressDto?, remove: Boolean ->
                        onClickedAddress(
                            e
                        )
                    })
                    adapter.setOnClickedScrollBtnListener(object : OnClickedScrollBtnListener {
                        override fun toLeft() {
                            if (binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem > 0) {
                                binding!!.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
                                    binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem - 1, true
                                )
                            }
                        }

                        override fun toRight() {
                            if (binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem < adapter.getItemCount() - 1) {
                                binding!!.placeslistBottomSheet.placeItemsViewpager.setCurrentItem(
                                    binding!!.placeslistBottomSheet.placeItemsViewpager.currentItem + 1, true
                                )
                            }
                        }
                    })
                    removeMarkers(MarkerType.LONG_CLICK)
                    addMarker(MarkerType.LONG_CLICK, 0, addressDto)
                    showMarkers(MarkerType.LONG_CLICK)
                    locationItemBottomSheetViewPager.setTag(MarkerType.LONG_CLICK)
                    locationItemBottomSheetViewPager.setAdapter(adapterMap[MarkerType.LONG_CLICK])
                    locationItemBottomSheetViewPager.setCurrentItem(0, false)
                    setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_EXPANDED)
                })
            })
    }

    private fun addMarker(markerType: MarkerType, position: Int, address: Geocoding.AddressDto) {
        val marker: Marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(address.latitude, address.longitude))
                .title(address.displayName)
        )
        marker.tag = MarkerHolder(position, markerType)
        if (!markerMaps.containsKey(markerType)) {
            markerMaps[markerType] = ArrayList()
        }
        markerMaps[markerType]!!.add(marker)
    }

    private fun addFavoriteMarker(position: Int, favoriteAddressDto: FavoriteAddressDto) {
        val markerOptions: MarkerOptions = MarkerOptions()
            .anchor(0.5f, 0.5f)
            .position(LatLng(favoriteAddressDto.latitude.toDouble(), favoriteAddressDto.longitude.toDouble()))
            .title(favoriteAddressDto.displayName)
        val view = (context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.marker_view, null)
        val tv_marker = view.findViewById<TextView>(R.id.marker)
        tv_marker.setText(String((position + 1).toString() + ""))
        val marker: Marker = googleMap.addMarker(
            markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(
                    createDrawableFromView(
                        activity,
                        view
                    )
                )
            )
        )
        marker.tag = MarkerHolder(position, MarkerType.FAVORITE)
        if (!markerMaps.containsKey(MarkerType.FAVORITE)) {
            markerMaps[MarkerType.FAVORITE] = ArrayList()
        }
        markerMaps[MarkerType.FAVORITE]!!.add(marker)
    }

    private fun createDrawableFromView(context: Context?, view: View): Bitmap {
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun removeMarkers(markerType: MarkerType) {
        if (markerMaps.containsKey(markerType)) {
            val markers: List<Marker> = markerMaps[markerType]!!
            for (marker in markers) {
                marker.remove()
            }
            markerMaps.remove(markerType)
        }
    }

    private fun showMarkers(markerType: MarkerType) {
        if (markerMaps.containsKey(markerType)) {
            val builder: LatLngBounds.Builder = LatLngBounds.Builder()
            for (marker in markerMaps[markerType]!!) {
                builder.include(marker.position)
            }
            val bounds: LatLngBounds = builder.build()
            var cameraUpdate: CameraUpdate? = null
            cameraUpdate = if (markerMaps[markerType]!!.size == 1) {
                CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 14f)
            } else {
                CameraUpdateFactory.newLatLngBounds(
                    bounds, TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 36f,
                        resources.displayMetrics
                    ).toInt()
                )
            }
            googleMap.moveCamera(cameraUpdate)
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val markerHolder = marker.tag as MarkerHolder?
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerMaps[markerHolder!!.markerType]!![markerHolder.position].position))
        locationItemBottomSheetViewPager.setTag(markerHolder.markerType)
        binding!!.placeslistBottomSheet.placeItemsViewpager.adapter = adapterMap[markerHolder.markerType]
        locationItemBottomSheetViewPager.setCurrentItem(markerHolder.position, false)
        setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_EXPANDED)
        return true
    }

    protected fun setLocationItemsBottomSheet() {
        val locationItemBottomSheet = binding!!.placeslistBottomSheet.placesBottomsheet
        locationItemBottomSheetViewPager = binding!!.placeslistBottomSheet.placeItemsViewpager
        locationItemBottomSheetViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            var markerType: MarkerType? = null
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (getStateOfBottomSheet(BottomSheetType.LOCATION_ITEM) == BottomSheetBehavior.STATE_EXPANDED) {
                    markerType = locationItemBottomSheetViewPager.getTag()
                    onPOIItemSelectedByBottomSheet(position, markerType)
                }
            }
        })
        locationItemBottomSheetViewPager.setOffscreenPageLimit(2)
        val locationItemBottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<LinearLayout>(locationItemBottomSheet)
        locationItemBottomSheetBehavior.setDraggable(false)
        locationItemBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //expanded일때 offset == 1.0, collapsed일때 offset == 0.0
                //offset에 따라서 버튼들이 이동하고, 지도의 좌표가 변경되어야 한다.
                val translationValue = (bottomSheet.height * slideOffset).toInt()
                try {
                    Objects.requireNonNull(binding).mapLayout.setPadding(0, 0, 0, translationValue)
                } catch (e: Exception) {
                }
            }
        })
        bottomSheetViewMap[BottomSheetType.LOCATION_ITEM] = locationItemBottomSheet
        bottomSheetBehaviorMap[BottomSheetType.LOCATION_ITEM] = locationItemBottomSheetBehavior
    }

    private fun setSearchPlacesBottomSheet() {
        val locationSearchBottomSheet = binding!!.bottomSheetSearchPlace.searchPlaceBottomsheet
        val locationSearchBottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<LinearLayout>(locationSearchBottomSheet)
        locationSearchBottomSheetBehavior.setDraggable(false)
        locationSearchBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        locationSearchBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        bottomSheetViewMap[BottomSheetType.SEARCH_LOCATION] = locationSearchBottomSheet
        bottomSheetBehaviorMap[BottomSheetType.SEARCH_LOCATION] = locationSearchBottomSheetBehavior
    }

    private fun setFavoritesBottomSheet() {
        val favoritesBottomSheet = binding!!.favoritesBottomSheet.favoritesBottomsheet
        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<LinearLayout>(favoritesBottomSheet)
        bottomSheetBehavior.setDraggable(false)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        bottomSheetViewMap[BottomSheetType.FAVORITES] = favoritesBottomSheet
        bottomSheetBehaviorMap[BottomSheetType.FAVORITES] = bottomSheetBehavior
    }

    override fun setStateOfBottomSheet(bottomSheetType: BottomSheetType, state: Int) {
        bottomSheetBehaviorMap[bottomSheetType].setState(state)
    }

    override fun getStateOfBottomSheet(bottomSheetType: BottomSheetType): Int {
        return bottomSheetBehaviorMap[bottomSheetType].getState()
    }

    fun getBottomSheetBehaviorOfExpanded(currentBottomSheetBehavior: BottomSheetBehavior<*>?): List<BottomSheetBehavior<*>> {
        val keySet: Set<BottomSheetType> = bottomSheetBehaviorMap.keys
        val bottomSheetBehaviors: MutableList<BottomSheetBehavior<*>> = ArrayList<BottomSheetBehavior<*>>()
        for (bottomSheetType in keySet) {
            if (bottomSheetBehaviorMap[bottomSheetType].getState() == BottomSheetBehavior.STATE_EXPANDED) {
                if (currentBottomSheetBehavior != null) {
                    if (bottomSheetBehaviorMap[bottomSheetType] != currentBottomSheetBehavior) {
                        bottomSheetBehaviors.add(bottomSheetBehaviorMap[bottomSheetType])
                    }
                }
            }
        }
        return bottomSheetBehaviors
    }

    override fun collapseAllExpandedBottomSheets() {
        val keySet: Set<BottomSheetType> = bottomSheetBehaviorMap.keys
        for (bottomSheetType in keySet) {
            if (getStateOfBottomSheet(bottomSheetType) == BottomSheetBehavior.STATE_EXPANDED) {
                setStateOfBottomSheet(bottomSheetType, BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
    }

    override fun onPOIItemSelectedByList(position: Int) {
        setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_COLLAPSED)
        val fragmentManager = childFragmentManager
        fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(getString(R.string.tag_find_address_fragment))!!)
            .addToBackStack(null).commitAllowingStateLoss()

        //bottomsheet가 아닌 list에서 아이템을 선택한 경우 호출
        //adapter -> poiitem생성 -> select poiitem -> bottomsheet열고 정보 표시
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLng(
                markerMaps[MarkerType.SEARCH]!![position].position
            )
        )
        //선택된 마커의 아이템 리스트내 위치 파악 후 뷰 페이저 이동
        locationItemBottomSheetViewPager.setTag(MarkerType.SEARCH)
        locationItemBottomSheetViewPager.setAdapter(adapterMap[MarkerType.SEARCH])
        locationItemBottomSheetViewPager.setCurrentItem(position, false)
        setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun onPOIItemSelectedByBottomSheet(position: Int, markerType: MarkerType?) {
        //bottomsheet에서 스크롤 하는 경우 호출
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerMaps[markerType]!![position].position))
    }

    private fun processIfNoLocations(result: List<FavoriteAddressDto?>) {
        val haveFavorites = result.size > 0
        val useCurrentLocation = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext).getBoolean(
            getString(R.string.pref_key_use_current_location), false
        )
        if (!haveFavorites && !useCurrentLocation) {
            //즐겨찾기가 비었고, 현재 위치 사용이 꺼져있음
            //현재 위치 사용 여부 다이얼로그 표시
            //확인 : 현재 위치의 날씨 데이터로드, 취소 : 앱 종료
            MaterialAlertDialogBuilder(activity).setTitle(R.string.title_empty_locations).setMessage(
                R.string.msg_empty_locations
            ).setPositiveButton(
                R.string.use,
                DialogInterface.OnClickListener { dialogInterface, i ->
                    PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext).edit().putBoolean(
                        getString(R.string.pref_key_use_current_location), true
                    )
                        .putString(
                            getString(R.string.pref_key_last_selected_location_type),
                            LocationType.CurrentLocation.name
                        ).commit()
                    enableCurrentLocation = true
                    dialogInterface.dismiss()
                    if (checkPermissionAndGpsOn()) {
                        parentFragmentManager.popBackStack()
                        onResultFavoriteListener!!.onResult(result)
                    }
                }).setNegativeButton(R.string.add_favorite, DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
                binding!!.headerLayout.callOnClickEditText()
            }).setNeutralButton(R.string.close_app, DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
                activity!!.finish()
            }).create().show()
        } else if (haveFavorites) {
            parentFragmentManager.popBackStack()
            onResultFavoriteListener!!.onResult(result)
        } else {
            if (checkPermissionAndGpsOn()) {
                parentFragmentManager.popBackStack()
                onResultFavoriteListener!!.onResult(result)
            }
        }
    }

    private fun checkPermissionAndGpsOn(): Boolean {
        val locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isPermissionGranted =
            context!!.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return if (isPermissionGranted && isGpsEnabled) {
            true
        } else if (!isPermissionGranted) {
            Toast.makeText(
                context,
                R.string.message_needs_location_permission,
                Toast.LENGTH_SHORT
            ).show()

            // 다시 묻지 않음을 선택했는지 확인
            val neverAskAgain = PreferenceManager.getDefaultSharedPreferences(context!!).getBoolean(
                getString(R.string.pref_key_never_ask_again_permission_for_access_location), false
            )
            if (neverAskAgain) {
                startActivity(IntentUtil.getAppSettingsIntent(activity))
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            }
            false
        } else {
            Toast.makeText(context, R.string.request_to_make_gps_on, Toast.LENGTH_SHORT)
                .show()
            startActivity(IntentUtil.locationSettingsIntent)
            false
        }
    }

    private fun checkHaveLocations() {
        getWeatherViewModel.getAll(object : DbQueryCallback<List<FavoriteAddressDto?>?>() {
            fun onResultSuccessful(result: List<FavoriteAddressDto?>) {
                MainThreadWorker.runOnUiThread(Runnable { processIfNoLocations(result) })
            }

            fun onResultNoData() {}
        })
    }

    private class MarkerHolder(var position: Int, var markerType: MarkerType)
    interface OnResultFavoriteListener {
        fun onAddedNewAddress(
            newFavoriteAddressDto: FavoriteAddressDto?,
            favoriteAddressDtoList: List<FavoriteAddressDto?>?,
            removed: Boolean
        )

        fun onResult(favoriteAddressDtoList: List<FavoriteAddressDto?>?)
        fun onClickedAddress(favoriteAddressDto: FavoriteAddressDto?)
    }
}