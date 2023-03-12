package com.lifedawn.bestweather.ui.main.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.CloseWindow
import com.lifedawn.bestweather.commons.constants.LocationType
import com.lifedawn.bestweather.commons.views.base.BaseFragment
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto
import com.lifedawn.bestweather.databinding.FragmentMainBinding
import com.lifedawn.bestweather.ui.findaddress.map.MapFragment
import com.lifedawn.bestweather.ui.main.IRefreshFavoriteLocationListOnSideNav
import com.lifedawn.bestweather.ui.main.viewmodel.InitViewModel
import com.lifedawn.bestweather.ui.weathers.view.WeatherFragment
import com.lifedawn.bestweather.ui.weathers.viewmodel.GetWeatherViewModel

class MainFragment : BaseFragment<FragmentMainBinding>(R.layout.fragment_main), IRefreshFavoriteLocationListOnSideNav,
    WeatherFragment.IWeatherFragment {
    private val favTypeTagInFavLocItemView = R.id.locationTypeTagInFavLocItemViewInSideNav
    private val favDtoTagInFavLocItemView = R.id.favoriteLocationDtoTagInFavLocItemViewInSideNav
    private var getWeatherViewModel: GetWeatherViewModel? = null
    private var sharedPreferences: SharedPreferences? = null
    private var favoriteAddressDtoList: List<FavoriteAddressDto> = ArrayList<FavoriteAddressDto>()
    private var currentAddressName: String? = null
    private var initViewModel: InitViewModel? = null
    private var init = true
    private val closeWindow: CloseWindow = CloseWindow(object : OnBackKeyDoubleClickedListener() {
        fun onDoubleClicked() {}
    })
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStackImmediate()
            } else {
                if (binding.drawerLayout.isDrawerOpen(binding.sideNavigation)) binding.drawerLayout.closeDrawer(binding.sideNavigation) else onBeforeCloseApp()
            }
        }
    }
    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            private var originalUsingCurrentLocation = false
            override fun onFragmentAttached(
                fm: FragmentManager, f: Fragment,
                context: Context
            ) {
                super.onFragmentAttached(fm, f, context)
                if (f is SettingsMainFragment) {
                    originalUsingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), false)
                }
            }

            override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                super.onFragmentStarted(fm, f)
                if (!initViewModel.ready) initViewModel.ready = true
            }

            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentDestroyed(fm, f)
                if (f is SettingsMainFragment) {
                    val newUsingCurrentLocation = sharedPreferences.getBoolean(
                        getString(R.string.pref_key_use_current_location),
                        false
                    )
                    val lastSelectedLocationType: LocationType = LocationType.valueOf(
                        sharedPreferences.getString(
                            getString(R.string.pref_key_last_selected_location_type),
                            LocationType.CurrentLocation.name
                        )
                    )
                    if (originalUsingCurrentLocation != newUsingCurrentLocation) {
                        setCurrentLocationState(newUsingCurrentLocation)
                        if (newUsingCurrentLocation) {
                            //날씨 프래그먼트 다시 그림
                            val weatherFragment: WeatherFragment? =
                                childFragmentManager.findFragmentByTag(WeatherFragment::class.java.getName()) as WeatherFragment?
                            if (weatherFragment != null) weatherFragment.reDraw()
                        } else {
                            //현재 위치 사용을 끈 경우
                            if (lastSelectedLocationType === LocationType.CurrentLocation) {
                                if (favoriteAddressDtoList.isEmpty()) {
                                    binding.sideNavMenu.favorites.callOnClick()
                                } else {
                                    binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick()
                                }
                            } else {
                                //날씨 프래그먼트 다시 그림
                                val weatherFragment: WeatherFragment? =
                                    childFragmentManager.findFragmentByTag(WeatherFragment::class.java.getName()) as WeatherFragment?
                                if (weatherFragment != null) weatherFragment.reDraw()
                            }
                        }
                    } else {
                        //날씨 프래그먼트 다시 그림
                        val weatherFragment: WeatherFragment? =
                            childFragmentManager.findFragmentByTag(WeatherFragment::class.java.getName()) as WeatherFragment?
                        if (weatherFragment != null) weatherFragment.reDraw()
                    }
                    originalUsingCurrentLocation = newUsingCurrentLocation
                }
            }
        }

    @SuppressLint("MissingPermission")
    protected fun onBeforeCloseApp() {
        val view = layoutInflater.inflate(R.layout.close_app_dialog, null)
        val dialog: AlertDialog = MaterialAlertDialogBuilder(requireActivity())
            .setView(view).create()
        dialog.show()
        val window = dialog.window
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        }
        val adLoader = AdLoader.Builder(requireContext().applicationContext, getString(R.string.NATIVE_ADVANCE_unitId))
            .forNativeAd { nativeAd ->
                val styles: NativeTemplateStyle = NativeTemplateStyle.Builder().withMainBackgroundColor(ColorDrawable(Color.WHITE)).build()
                val template: TemplateView = view.findViewById<View>(R.id.adView) as TemplateView
                template.setStyles(styles)
                template.setNativeAd(nativeAd)
            }.withNativeAdOptions(NativeAdOptions.Builder().setRequestCustomMuteThisAd(true).build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
        view.findViewById<View>(R.id.cancelBtn).setOnClickListener { dialog.dismiss() }
        view.findViewById<View>(R.id.closeBtn).setOnClickListener {
            dialog.dismiss()
            requireActivity().finish()
        }
    }

    private fun setCurrentLocationState(newState: Boolean) {
        binding.sideNavMenu.currentLocationLayout.isClickable = newState
        binding.sideNavMenu.addressName.setText(if (newState) R.string.enabled_use_current_location else R.string.disabled_use_current_location)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
        initViewModel = ViewModelProvider(requireActivity()).get(InitViewModel::class.java)
        getWeatherViewModel = ViewModelProvider(requireActivity()).get<GetWeatherViewModel>(GetWeatherViewModel::class.java)
        getWeatherViewModel.setLocationCallback(object : MyLocationCallback() {
            fun onSuccessful(locationResult: LocationResult?) {}
            fun onFailed(fail: Fail) {
                if (fail === Fail.FAILED_FIND_LOCATION) {
                    //기존의 현재 위치 값이 없으면 즐겨찾기로 이동
                    Toast.makeText(requireContext().applicationContext, R.string.failedFindingLocation, Toast.LENGTH_SHORT).show()
                    val locationResult: LocationResult = FusedLocation(requireContext().applicationContext).lastCurrentLocation
                    if (locationResult.getLocations().get(0).getLatitude() == 0.0 ||
                        locationResult.getLocations().get(0).getLongitude() == 0.0
                    ) {
                        binding.sideNavMenu.favorites.callOnClick()
                    }
                }
            }
        })
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentAddressName", currentAddressName)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (savedInstanceState != null) {
            currentAddressName = savedInstanceState.getString("currentAddressName")
        }

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.sideNavMenu.favorites.setOnClickListener(sideNavOnClickListener)
        binding.sideNavMenu.settings.setOnClickListener(sideNavOnClickListener)
        binding.sideNavMenu.notificationAlarmSettings.setOnClickListener(sideNavOnClickListener)
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt()
        binding.sideNavMenu.currentLocationLayout.setPadding(
            padding, MyApplication.getStatusBarHeight() + padding, padding,
            padding
        )
        
        binding.sideNavMenu.currentLocationLayout.setOnClickListener {
            addWeatherFragment(LocationType.CurrentLocation, null, null)
            binding.drawerLayout.closeDrawer(binding.sideNavigation)
        }
        getWeatherViewModel.getCurrentLocationLiveData().observe(viewLifecycleOwner) { addressName ->
            currentAddressName = addressName
            if (currentAddressName != null) {
                binding.sideNavMenu.addressName.text = currentAddressName
            }
        }
        getWeatherViewModel.favoriteAddressListLiveData.observe(requireActivity()) { result ->
            createLocationsList(result)
            if (init) {
                init = false
                val usingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), false)
                val lastSelectedLocationType: LocationType = LocationType.valueOf(
                    sharedPreferences.getString(
                        getString(R.string.pref_key_last_selected_location_type),
                        LocationType.CurrentLocation.name
                    )
                )
                setCurrentLocationState(usingCurrentLocation)
                if (currentAddressName != null) binding.sideNavMenu.addressName.text = currentAddressName
                if (lastSelectedLocationType === LocationType.CurrentLocation) {
                    if (usingCurrentLocation) {
                        addWeatherFragment(lastSelectedLocationType, null, null)
                    } else {
                        if (favoriteAddressDtoList.size > 0) {
                            binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick()
                        } else {
                            binding.sideNavMenu.favorites.callOnClick()
                        }
                    }
                } else {
                    val lastSelectedFavoriteId = sharedPreferences.getInt(
                        getString(R.string.pref_key_last_selected_favorite_address_id), -1
                    )
                    if (!clickLocationItemById(lastSelectedFavoriteId)) {
                        if (favoriteAddressDtoList.size > 0) {
                            binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick()
                        } else {
                            binding.sideNavMenu.favorites.callOnClick()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun createLocationsList(result: List<FavoriteAddressDto>) {
        favoriteAddressDtoList = result
        binding.sideNavMenu.favoriteAddressLayout.visibility = if (favoriteAddressDtoList.size > 0) View.VISIBLE else View.GONE
        binding.sideNavMenu.favoriteAddressLayout.removeAllViews()
        val layoutInflater = layoutInflater
        for (favoriteAddressDto in favoriteAddressDtoList) {
            addFavoriteLocationItemView(layoutInflater, LocationType.SelectedAddress, favoriteAddressDto)
        }
    }

    override fun onRefreshedFavoriteLocationsList(requestKey: String?, bundle: Bundle) {
        val isSelectedNewAddress = bundle.getBoolean("added")
        if (isSelectedNewAddress) {
            val lastSelectedFavoriteAddressId = sharedPreferences.getInt(
                getString(R.string.pref_key_last_selected_favorite_address_id), -1
            )
            clickLocationItemById(lastSelectedFavoriteAddressId)
        } else {
            processIfPreviousFragmentIsFavorite()
        }
    }

    private fun processIfPreviousFragmentIsFavorite() {
        val lastSelectedFavoriteAddressId = sharedPreferences.getInt(getString(R.string.pref_key_last_selected_favorite_address_id), -1)
        val lastSelectedLocationType: LocationType = LocationType.valueOf(
            sharedPreferences.getString(getString(R.string.pref_key_last_selected_location_type), LocationType.CurrentLocation.name)
        )
        if (lastSelectedLocationType === LocationType.SelectedAddress) {
            if (favoriteAddressDtoList.isEmpty()) {
                setCurrentLocationState(true)
                binding.sideNavMenu.currentLocationLayout.callOnClick()
            } else {
                var removed = true
                for (favoriteAddressDto in favoriteAddressDtoList) {
                    if (favoriteAddressDto.id == lastSelectedFavoriteAddressId) {
                        removed = false
                        break
                    }
                }
                if (removed) {
                    binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick()
                }
            }
        } else {
            //현재 위치
            binding.sideNavMenu.currentLocationLayout.callOnClick()
        }
    }

    private fun clickLocationItemById(id: Int): Boolean {
        var favoriteAddressDto: FavoriteAddressDto? = null
        for (childIdx in 0 until binding.sideNavMenu.favoriteAddressLayout.childCount) {
            favoriteAddressDto = binding.sideNavMenu.favoriteAddressLayout
                .getChildAt(childIdx).getTag(favDtoTagInFavLocItemView) as FavoriteAddressDto
            if (favoriteAddressDto.id == id) {
                binding.sideNavMenu.favoriteAddressLayout.getChildAt(childIdx).callOnClick()
                return true
            }
        }
        return false
    }

    private fun addFavoriteLocationItemView(
        layoutInflater: LayoutInflater, locationType: LocationType,
        favoriteAddressDto: FavoriteAddressDto?
    ) {
        val locationItemView = layoutInflater.inflate(R.layout.favorite_address_item_in_side_nav, null) as TextView
        locationItemView.setOnClickListener {
            binding.drawerLayout.closeDrawer(binding.sideNavigation)
            addWeatherFragment(locationType, favoriteAddressDto, null)
        }
        locationItemView.setText(favoriteAddressDto.displayName)
        locationItemView.setTag(favDtoTagInFavLocItemView, favoriteAddressDto)
        locationItemView.setTag(favTypeTagInFavLocItemView, locationType)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val dp8 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        val dp16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
        layoutParams.setMargins(dp16, dp8, dp16, dp8)
        binding.sideNavMenu.favoriteAddressLayout.addView(locationItemView, layoutParams)
    }

    override fun onDestroy() {
        onBackPressedCallback.remove()
        childFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        super.onDestroy()
    }

    private val sideNavOnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.favorites -> {
                val mapFragment = MapFragment()
                val bundle = Bundle()
                bundle.putString(BundleKey.RequestFragment.name, MainFragment::class.java.name)
                mapFragment.arguments = bundle
                mapFragment.setOnResultFavoriteListener(object : OnResultFavoriteListener {
                    override fun onAddedNewAddress(
                        newFavoriteAddressDto: FavoriteAddressDto,
                        favoriteAddressDtoList: List<FavoriteAddressDto>,
                        removed: Boolean
                    ) {
                        childFragmentManager.popBackStackImmediate()
                        onResultMapFragment(newFavoriteAddressDto)
                    }

                    override fun onResult(favoriteAddressDtoList: List<FavoriteAddressDto>) {
                        onResultMapFragment(null)
                    }

                    override fun onClickedAddress(favoriteAddressDto: FavoriteAddressDto?) {}
                })
                val tag = MapFragment::class.java.name
                val transaction = childFragmentManager.beginTransaction()
                transaction.hide(childFragmentManager.primaryNavigationFragment).add(
                    binding.fragmentContainer.id, mapFragment,
                    tag
                ).addToBackStack(tag).setPrimaryNavigationFragment(mapFragment).commitAllowingStateLoss()
            }
            R.id.settings -> {
                val settingsMainFragment = SettingsMainFragment()
                childFragmentManager.beginTransaction().hide(childFragmentManager.primaryNavigationFragment).add(
                    binding.fragmentContainer.id, settingsMainFragment,
                    getString(R.string.tag_settings_main_fragment)
                ).addToBackStack(
                    getString(R.string.tag_settings_main_fragment)
                ).setPrimaryNavigationFragment(settingsMainFragment).commitAllowingStateLoss()
            }
            R.id.notificationAlarmSettings -> {
                val notificationFragment = NotificationFragment()
                val notiTag: String = NotificationFragment::class.java.getName()
                childFragmentManager.beginTransaction().hide(childFragmentManager.primaryNavigationFragment).add(
                    binding.fragmentContainer.id, notificationFragment,
                    notiTag
                ).addToBackStack(notiTag).setPrimaryNavigationFragment(notificationFragment).commitAllowingStateLoss()
            }
        }
        binding.drawerLayout.closeDrawer(binding.sideNavigation, false)
    }

    override fun onResultMapFragment(newFavoriteAddressDto: FavoriteAddressDto?) {
        //변경된 위치가 있는지 확인
        setCurrentLocationState(sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), false))
        val added = newFavoriteAddressDto != null
        if (added) {
            //즐겨찾기 변동 발생
            val result = Bundle()
            result.putBoolean("added", true)
            onRefreshedFavoriteLocationsList(null, result)
        } else {
            //변동 없음
            processIfPreviousFragmentIsFavorite()
        }
    }

    override fun addWeatherFragment(locationType: LocationType, favoriteAddressDto: FavoriteAddressDto?, arguments: Bundle?) {
        val bundle = Bundle()
        if (arguments != null) bundle.putAll(arguments)
        bundle.putSerializable("LocationType", locationType)
        bundle.putSerializable("FavoriteAddressDto", favoriteAddressDto)
        val newWeatherFragment = WeatherFragment(this)
        newWeatherFragment.setOnAsyncLoadCallback(object : OnAsyncLoadCallback {
            override fun onFinished(fragment: Fragment) {
                if (fragment.isAdded) (fragment as ILoadWeatherData).load()
            }
        })
        newWeatherFragment.setArguments(bundle)
        newWeatherFragment.setMenuOnClickListener(View.OnClickListener { v: View? ->
            binding.drawerLayout.openDrawer(
                binding.sideNavigation
            )
        })
        newWeatherFragment.setiRefreshFavoriteLocationListOnSideNav(this as IRefreshFavoriteLocationListOnSideNav)
        val fragmentManager = childFragmentManager
        fragmentManager.beginTransaction().replace(
            binding.fragmentContainer.id, newWeatherFragment,
            WeatherFragment::class.java.getName()
        ).setPrimaryNavigationFragment(newWeatherFragment).commit()
    }

    override fun refreshFavorites(callback: DbQueryCallback<List<FavoriteAddressDto?>?>) {
        getWeatherViewModel.getAll(object : DbQueryCallback<List<FavoriteAddressDto?>?>() {
            fun onResultSuccessful(result: List<FavoriteAddressDto?>?) {
                MainThreadWorker.runOnUiThread(Runnable { callback.onResultSuccessful(result) })
            }

            fun onResultNoData() {}
        })
    }
}