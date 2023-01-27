package com.lifedawn.bestweather.ui.weathers

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.ArrayMap
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource
import com.lifedawn.bestweather.commons.views.BaseFragment
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding
import com.lifedawn.bestweather.databinding.LoadingViewAsyncBinding
import com.lifedawn.bestweather.ui.findaddress.map.MapFragment
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ExecutorService

class WeatherFragment(private val iWeatherFragment: IWeatherFragment) : BaseFragment<FragmentWeatherBinding>(
    R.layout.fragment_weather
), IGps, ILoadWeatherData {
    private val executorService: ExecutorService = MyApplication.getExecutorService()
    private var binding: FragmentWeatherBinding? = null
    private var asyncBinding: LoadingViewAsyncBinding? = null
    private var getWeatherViewModel: GetWeatherViewModel? = null
    private var menuOnClickListener: View.OnClickListener? = null
    private var fusedLocation: FusedLocation? = null
    private var networkStatus: NetworkStatus? = null
    private var locationCallbackInMainFragment: FusedLocation.MyLocationCallback? = null
    private var weatherViewController: WeatherViewController? = null
    private var flickrViewModel: FlickrViewModel? = null
    private var iRefreshFavoriteLocationListOnSideNav: IRefreshFavoriteLocationListOnSideNav? = null
    private var locationLifeCycleObserver: LocationLifeCycleObserver? = null
    private var weatherFragmentViewModel: WeatherFragmentViewModel? = null
    fun setMenuOnClickListener(menuOnClickListener: View.OnClickListener?): WeatherFragment {
        this.menuOnClickListener = menuOnClickListener
        return this
    }

    fun setiRefreshFavoriteLocationListOnSideNav(iRefreshFavoriteLocationListOnSideNav: IRefreshFavoriteLocationListOnSideNav?): WeatherFragment {
        this.iRefreshFavoriteLocationListOnSideNav = iRefreshFavoriteLocationListOnSideNav
        return this
    }

    private var onAsyncLoadCallback: OnAsyncLoadCallback? = null
    fun setOnAsyncLoadCallback(onAsyncLoadCallback: OnAsyncLoadCallback?): WeatherFragment {
        this.onAsyncLoadCallback = onAsyncLoadCallback
        return this
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationLifeCycleObserver = LocationLifeCycleObserver(requireActivity().activityResultRegistry, requireActivity())
        lifecycle.addObserver(locationLifeCycleObserver)
        networkStatus = NetworkStatus.getInstance(requireContext().applicationContext)
        fusedLocation = FusedLocation(requireContext().applicationContext)
        weatherFragmentViewModel = ViewModelProvider(this).get<WeatherFragmentViewModel>(WeatherFragmentViewModel::class.java)
        getWeatherViewModel = ViewModelProvider(requireActivity()).get<GetWeatherViewModel>(GetWeatherViewModel::class.java)
        locationCallbackInMainFragment = getWeatherViewModel.getLocationCallback()
        flickrViewModel = ViewModelProvider(this).get(FlickrViewModel::class.java)
        weatherFragmentViewModel.arguments = if (arguments == null) savedInstanceState else arguments
        weatherFragmentViewModel.locationType = weatherFragmentViewModel.arguments.getSerializable("LocationType") as LocationType
        weatherFragmentViewModel.favoriteAddressDto =
            if (weatherFragmentViewModel.arguments.containsKey("FavoriteAddressDto")) weatherFragmentViewModel.arguments.getSerializable("FavoriteAddressDto") as FavoriteAddressDto else null
    }

    /**
     * hidden is true이면 black, else white
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, activity)
        } else {
            HeaderbarStyle.setStyle(HeaderbarStyle.Style.White, activity)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        asyncBinding = LoadingViewAsyncBinding.inflate(inflater, container, false)
        val asyncLayoutInflater = AsyncLayoutInflater(requireContext())
        asyncLayoutInflater.inflate(R.layout.fragment_weather, container, object : OnInflateFinishedListener {
            @SuppressLint("MissingPermission")
            override fun onInflateFinished(view: View, resid: Int, parent: ViewGroup?) {
                binding = FragmentWeatherBinding.bind(view)
                asyncBinding!!.root.addView(binding!!.root)
                asyncBinding!!.progressCircular.pauseAnimation()
                asyncBinding!!.progressCircular.visibility = View.GONE
                shimmer(true, false)
                val statusBarHeight: Int = MyApplication.getStatusBarHeight()
                val layoutParams = binding!!.mainToolbar.root.layoutParams as FrameLayout.LayoutParams
                layoutParams.topMargin = statusBarHeight
                binding!!.mainToolbar.root.layoutParams = layoutParams
                val topMargin = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics)
                        + resources.getDimension(R.dimen.toolbarHeight) + statusBarHeight).toInt()
                val headerLayoutParams: ConstraintLayout.LayoutParams =
                    binding!!.weatherDataSourceLayout.layoutParams as ConstraintLayout.LayoutParams
                headerLayoutParams.topMargin = topMargin
                binding!!.weatherDataSourceLayout.layoutParams = headerLayoutParams
                onChangedStateBackgroundImg(false)
                binding!!.loadingAnimation.visibility = View.VISIBLE
                binding!!.flickrImageUrl.visibility = View.GONE
                binding!!.currentConditionsImg.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.black_alpha_30),
                    PorterDuff.Mode.DARKEN
                )
                weatherViewController = WeatherViewController(binding!!.rootLayout)
                lifecycle.addObserver(weatherViewController)
                weatherViewController.setWeatherView(PrecipType.CLEAR, null)
                binding!!.mainToolbar.openNavigationDrawer.setOnClickListener(menuOnClickListener)
                binding!!.mainToolbar.gps.setOnClickListener { v: View? ->
                    if (networkStatus.networkAvailable()) {
                        shimmer(true, false)
                        fusedLocation.findCurrentLocation(MY_LOCATION_CALLBACK, false)
                    } else {
                        Toast.makeText(requireContext().applicationContext, R.string.disconnected_network, Toast.LENGTH_SHORT).show()
                    }
                }
                binding!!.mainToolbar.find.setOnClickListener { v: View? ->
                    if (networkStatus.networkAvailable()) {
                        val mapFragment = MapFragment()
                        val arguments = Bundle()
                        arguments.putString(BundleKey.RequestFragment.name, WeatherFragment::class.java.name)
                        mapFragment.arguments = arguments
                        mapFragment.setOnResultFavoriteListener(object : OnResultFavoriteListener {
                            override fun onAddedNewAddress(
                                newFavoriteAddressDto: FavoriteAddressDto,
                                favoriteAddressDtoList: List<FavoriteAddressDto>,
                                removed: Boolean
                            ) {
                                parentFragmentManager.popBackStack()
                                iRefreshFavoriteLocationListOnSideNav.onResultMapFragment(newFavoriteAddressDto)
                            }

                            override fun onResult(favoriteAddressDtoList: List<FavoriteAddressDto>) {
                                iRefreshFavoriteLocationListOnSideNav.onResultMapFragment(null)
                            }

                            override fun onClickedAddress(favoriteAddressDto: FavoriteAddressDto?) {}
                        })
                        parentFragmentManager.beginTransaction().hide(this@WeatherFragment).add(
                            R.id.fragment_container,
                            mapFragment, MapFragment::class.java.name
                        ).addToBackStack(
                            MapFragment::class.java.name
                        ).setPrimaryNavigationFragment(mapFragment).commitAllowingStateLoss()
                    }
                }
                binding!!.mainToolbar.refresh.setOnClickListener { v: View? ->
                    if (networkStatus.networkAvailable()) {
                        requestNewData()
                    } else {
                        Toast.makeText(requireContext().applicationContext, R.string.disconnected_network, Toast.LENGTH_SHORT).show()
                    }
                }
                binding!!.flickrImageUrl.setOnClickListener { v: View? ->
                    if (binding!!.flickrImageUrl.tag != null) {
                        val url = binding!!.flickrImageUrl.tag as String
                        if (url == "failed") {
                            loadImgOfCurrentConditions(flickrViewModel.getLastParameter())
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(url)
                            startActivity(intent)
                        }
                    }
                }
                val adRequest = AdRequest.Builder().build()
                val adLoader = AdLoader.Builder(requireContext().applicationContext, getString(R.string.NATIVE_ADVANCE_unitId))
                    .forNativeAd { nativeAd: NativeAd? ->
                        val styles: NativeTemplateStyle = NativeTemplateStyle.Builder().withMainBackgroundColor(
                            ColorDrawable(Color.WHITE)
                        ).build()
                        val template: TemplateView = binding!!.adViewBottom
                        template.setStyles(styles)
                        template.setNativeAd(nativeAd)
                    }.withNativeAdOptions(NativeAdOptions.Builder().setRequestCustomMuteThisAd(true).build())
                    .build()
                adLoader.loadAd(adRequest)
                binding!!.adViewBelowAirQuality.loadAd(adRequest)
                binding!!.adViewBelowAirQuality.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        super.onAdClosed()
                        binding!!.adViewBelowAirQuality.loadAd(AdRequest.Builder().build())
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                    }
                }
                binding!!.adViewBelowDetailCurrentConditions.loadAd(adRequest)
                binding!!.adViewBelowDetailCurrentConditions.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        super.onAdClosed()
                        binding!!.adViewBelowDetailCurrentConditions.loadAd(AdRequest.Builder().build())
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                    }
                }
                weatherFragmentViewModel.resumedFragmentObserver.observe(viewLifecycleOwner, Observer { aBoolean: Boolean? ->
                    shimmer(false, true)
                    loadImgOfCurrentConditions(flickrViewModel.getLastParameter())
                })
                flickrViewModel.img.observe(viewLifecycleOwner) { flickrImgResponse ->
                    onChangedStateBackgroundImg(flickrImgResponse.successful)
                    if (flickrImgResponse.successful) {
                        Glide.with(requireContext()).load(flickrImgResponse.flickrImgData.getImg()).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .transition(
                                DrawableTransitionOptions.withCrossFade(300)
                            ).into(binding!!.currentConditionsImg)
                        val text: String =
                            flickrImgResponse.flickrImgData.getPhoto().getOwner() + "-" + flickrImgResponse.flickrImgData.getPhoto()
                                .getTitle()
                        binding!!.flickrImageUrl.setText(
                            TextUtil.getUnderLineColorText(
                                text, text,
                                ContextCompat.getColor(requireContext(), R.color.white)
                            )
                        )
                        binding!!.flickrImageUrl.tag = flickrImgResponse.flickrImgData.getRealFlickrUrl()
                        setBackgroundWeatherView(flickrImgResponse.flickrImgData.getWeather(), flickrImgResponse.flickrImgData.getVolume())
                    } else {
                        Glide.with(requireContext()).clear(binding!!.currentConditionsImg)
                        val text = getString(R.string.failed_load_img)
                        binding!!.flickrImageUrl.setText(
                            TextUtil.getUnderLineColorText(
                                text, text,
                                ContextCompat.getColor(requireContext(), R.color.black)
                            )
                        )
                        binding!!.flickrImageUrl.tag = "failed"
                        if (flickrImgResponse.flickrImgData != null) {
                            setBackgroundWeatherView(
                                flickrImgResponse.flickrImgData.getWeather(),
                                flickrImgResponse.flickrImgData.getVolume()
                            )
                        }
                    }
                    binding!!.loadingAnimation.visibility = View.GONE
                    binding!!.flickrImageUrl.visibility = View.VISIBLE
                }
                weatherFragmentViewModel.weatherDataResponse.observe(
                    viewLifecycleOwner,
                    Observer { responseResultObj: ResponseResultObj? -> responseResultObj?.let { processOnResult(it) } })
                shimmer(false, false)
                if (onAsyncLoadCallback != null) onAsyncLoadCallback!!.onFinished(this@WeatherFragment)
                onAsyncLoadCallback = null
            }
        })
        return asyncBinding!!.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(weatherFragmentViewModel.arguments)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun load() {
        shimmer(true, false)
        binding!!.mainToolbar.gps.visibility =
            if (weatherFragmentViewModel.locationType === LocationType.CurrentLocation) View.VISIBLE else View.GONE
        binding!!.mainToolbar.find.visibility =
            if (weatherFragmentViewModel.locationType === LocationType.CurrentLocation) View.GONE else View.VISIBLE
        executorService.submit {
            val bundle = Bundle()
            bundle.putSerializable(BundleKey.SelectedAddressDto.name, weatherFragmentViewModel.favoriteAddressDto)
            bundle.putSerializable(BundleKey.IGps.name, this as IGps)
            bundle.putString(BundleKey.LocationType.name, weatherFragmentViewModel.locationType.name)
            bundle.putString(BundleKey.RequestFragment.name, WeatherFragment::class.java.name)
            weatherFragmentViewModel.locationType = LocationType.valueOf(bundle.getString(BundleKey.LocationType.name))
            val clickGps: Boolean = weatherFragmentViewModel.arguments.getBoolean("clickGps", false)
            if (weatherFragmentViewModel.locationType === LocationType.CurrentLocation) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id), -1).putString(
                    getString(R.string.pref_key_last_selected_location_type), weatherFragmentViewModel.locationType.name
                ).commit()
                val locationResult: LocationResult = fusedLocation.lastCurrentLocation
                weatherFragmentViewModel.latitude = locationResult.getLocations().get(0).getLatitude()
                weatherFragmentViewModel.longitude = locationResult.getLocations().get(0).getLongitude()
                if (weatherFragmentViewModel.latitude == 0.0 && weatherFragmentViewModel.longitude == 0.0) {
                    //최근에 현재위치로 잡힌 위치가 없으므로 현재 위치 요청
                    requestCurrentLocation()
                } else if (clickGps) {
                    MainThreadWorker.runOnUiThread(Runnable { binding!!.mainToolbar.gps.callOnClick() })
                } else {
                    weatherFragmentViewModel.zoneId = ZoneId.of(
                        PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                            .getString("zoneId", "")
                    )
                    //위/경도에 해당하는 지역명을 불러오고, 날씨 데이터 다운로드
                    //이미 존재하는 날씨 데이터면 다운로드X
                    var refresh: Boolean =
                        !weatherFragmentViewModel.containWeatherData(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude)
                    if (weatherFragmentViewModel.isOldDownloadedData(
                            weatherFragmentViewModel.latitude,
                            weatherFragmentViewModel.longitude
                        )
                    ) {
                        refresh = true
                        weatherFragmentViewModel.removeOldDownloadedData(
                            weatherFragmentViewModel.latitude,
                            weatherFragmentViewModel.longitude
                        )
                    }
                    val finalRefresh = refresh
                    MainThreadWorker.runOnUiThread(Runnable {
                        requestAddressOfLocation(
                            weatherFragmentViewModel.latitude,
                            weatherFragmentViewModel.longitude,
                            finalRefresh
                        )
                    })
                }
            } else {
                weatherFragmentViewModel.selectedFavoriteAddressDto = bundle.getSerializable(
                    BundleKey.SelectedAddressDto.name
                ) as FavoriteAddressDto?
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                sharedPreferences.edit().putInt(
                    getString(R.string.pref_key_last_selected_favorite_address_id),
                    weatherFragmentViewModel.selectedFavoriteAddressDto.id
                ).putString(
                    getString(R.string.pref_key_last_selected_location_type),
                    weatherFragmentViewModel.locationType.name
                ).commit()
                weatherFragmentViewModel.mainWeatherProviderType =
                    weatherFragmentViewModel.getMainWeatherSourceType(weatherFragmentViewModel.selectedFavoriteAddressDto.countryCode)
                weatherFragmentViewModel.countryCode = weatherFragmentViewModel.selectedFavoriteAddressDto.countryCode
                weatherFragmentViewModel.addressName = weatherFragmentViewModel.selectedFavoriteAddressDto.displayName
                weatherFragmentViewModel.latitude = weatherFragmentViewModel.selectedFavoriteAddressDto.latitude.toDouble()
                weatherFragmentViewModel.longitude = weatherFragmentViewModel.selectedFavoriteAddressDto.longitude.toDouble()
                weatherFragmentViewModel.zoneId = ZoneId.of(weatherFragmentViewModel.selectedFavoriteAddressDto.zoneId)
                if (weatherFragmentViewModel.containWeatherData(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude)) {
                    if (weatherFragmentViewModel.isOldDownloadedData(
                            weatherFragmentViewModel.latitude,
                            weatherFragmentViewModel.longitude
                        )
                    ) {
                        weatherFragmentViewModel.removeOldDownloadedData(
                            weatherFragmentViewModel.latitude,
                            weatherFragmentViewModel.longitude
                        )
                        requestNewData()
                    } else {
                        //기존 데이터 표시
                        try {
                            val responseResultObj: WeatherResponseObj =
                                WeatherFragmentViewModel.FINAL_RESPONSE_MAP.get(weatherFragmentViewModel.latitude.toString() + weatherFragmentViewModel.longitude.toString())
                            if (responseResultObj != null) {
                                weatherFragmentViewModel.mainWeatherProviderType = responseResultObj.requestMainWeatherProviderType
                                MainThreadWorker.runOnUiThread(Runnable { reDraw() })
                            }
                        } catch (e: Exception) {
                            requestNewData()
                        }
                    }
                } else {
                    MainThreadWorker.runOnUiThread(Runnable { requestNewData() })
                }
                MainThreadWorker.runOnUiThread(Runnable {
                    binding!!.addressName.setText(weatherFragmentViewModel.addressName)
                    binding!!.countryName.setText(weatherFragmentViewModel.selectedFavoriteAddressDto.countryName)
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (weatherFragmentViewModel.multipleWeatherRestApiCallback != null) weatherFragmentViewModel.multipleWeatherRestApiCallback.cancel()
        if (weatherViewController != null) lifecycle.removeObserver(weatherViewController)
        if (locationLifeCycleObserver != null) lifecycle.removeObserver(locationLifeCycleObserver)
        if (binding!!.adViewBelowDetailCurrentConditions.parent != null) (binding!!.adViewBelowDetailCurrentConditions.parent as ViewGroup).removeView(
            binding!!.adViewBelowDetailCurrentConditions
        )
        binding!!.adViewBelowDetailCurrentConditions.destroy()
        if (binding!!.adViewBelowAirQuality.parent != null) (binding!!.adViewBelowAirQuality.parent as ViewGroup).removeView(
            binding!!.adViewBelowAirQuality
        )
        binding!!.adViewBelowAirQuality.destroy()
        if (binding!!.adViewBottom.parent != null) (binding!!.adViewBottom.parent as ViewGroup).removeView(binding!!.adViewBottom)
        binding!!.adViewBottom.destroyNativeAd()
        binding = null
        asyncBinding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setBackgroundWeatherView(weather: String, volume: String) {
        if (!PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                .getBoolean(getString(R.string.pref_key_show_background_animation), false)
        ) {
            weatherViewController.setWeatherView(PrecipType.CLEAR, null)
        } else {
            if (weather == Flickr.Weather.rain.text) {
                weatherViewController.setWeatherView(PrecipType.RAIN, volume)
            } else if (weather == Flickr.Weather.snow.text) {
                weatherViewController.setWeatherView(PrecipType.SNOW, volume)
            } else {
                weatherViewController.setWeatherView(PrecipType.CLEAR, volume)
            }
        }
    }

    private fun loadImgOfCurrentConditions(flickrRequestParameter: FlickrRequestParameter) {
        HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, requireActivity())
        binding!!.loadingAnimation.visibility = View.VISIBLE
        binding!!.flickrImageUrl.visibility = View.GONE
        flickrViewModel.loadImg(flickrRequestParameter)
    }

    private val MY_LOCATION_CALLBACK: FusedLocation.MyLocationCallback = object : MyLocationCallback() {
        fun onSuccessful(locationResult: LocationResult?) {
            //현재 위치 파악 성공
            //현재 위/경도 좌표를 최근 현재위치의 위/경도로 등록
            //날씨 데이터 요청
            val location: Location = getBestLocation(locationResult)
            weatherFragmentViewModel.zoneId = ZoneId.of(
                PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                    .getString("zoneId", "")
            )
            onChangedCurrentLocation(location)
            locationCallbackInMainFragment.onSuccessful(locationResult)
        }

        fun onFailed(fail: Fail) {
            shimmer(false, false)
            locationCallbackInMainFragment.onFailed(fail)
            if (fail === Fail.DISABLED_GPS) {
                fusedLocation.onDisabledGps(
                    requireActivity(),
                    locationLifeCycleObserver,
                    ActivityResultCallback { result: ActivityResult? ->
                        if (fusedLocation.isOnGps) {
                            binding!!.mainToolbar.gps.callOnClick()
                        }
                    })
            } else if (fail === Fail.DENIED_LOCATION_PERMISSIONS) {
                fusedLocation.onRejectPermissions(
                    requireActivity(),
                    locationLifeCycleObserver,
                    ActivityResultCallback { result: ActivityResult? ->
                        if (fusedLocation.checkDefaultPermissions()) {
                            binding!!.mainToolbar.gps.callOnClick()
                        }
                    },
                    ActivityResultCallback { result: Map<String?, Boolean?> ->
                        //gps사용 권한
                        //허가남 : 현재 위치 다시 파악
                        //거부됨 : 작업 취소
                        //계속 거부 체크됨 : 작업 취소
                        if (!result.containsValue(false)) binding!!.mainToolbar.gps.callOnClick()
                    })
            } else if (fail === Fail.FAILED_FIND_LOCATION) {
                val btnObjList: MutableList<BtnObj> = ArrayList<BtnObj>()
                btnObjList.add(BtnObj(View.OnClickListener { v: View? ->
                    val argument = Bundle()
                    argument.putBoolean("clickGps", true)
                    iWeatherFragment.addWeatherFragment(
                        weatherFragmentViewModel.locationType,
                        weatherFragmentViewModel.selectedFavoriteAddressDto,
                        argument
                    )
                }, getString(R.string.again)))
                setFailFragment(btnObjList)
            }
        }
    }

    override fun requestCurrentLocation() {
        binding!!.mainToolbar.gps.callOnClick()
    }

    private fun requestAddressOfLocation(latitude: Double, longitude: Double, refresh: Boolean) {
        Geocoding.nominatimReverseGeocoding(requireContext().applicationContext, latitude, longitude, error.NonExistentClass { address ->
            if (activity != null) {
                weatherFragmentViewModel.addressName = address.displayName
                weatherFragmentViewModel.mainWeatherProviderType = weatherFragmentViewModel.getMainWeatherSourceType(address.countryCode)
                weatherFragmentViewModel.countryCode = address.countryCode
                val addressStr = getString(R.string.current_location) + " : " + weatherFragmentViewModel.addressName
                val editor = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext).edit()
                TimeZoneUtils.INSTANCE.getTimeZone(latitude, longitude) { zoneId ->
                    editor.putString("zoneId", zoneId.getId()).apply()
                    onResultCurrentLocation(addressStr, address, refresh)
                }
            }
        })
    }

    private fun onResultCurrentLocation(addressStr: String, addressDto: Geocoding.AddressDto, refresh: Boolean) {
        requireActivity().runOnUiThread {
            binding!!.addressName.text = addressStr
            binding!!.countryName.setText(addressDto.country)
            getWeatherViewModel.setCurrentLocationAddressName(addressDto.displayName)
            if (refresh) {
                requestNewData()
            } else {
                //이미 데이터가 있으면 다시 그림
                val key: String = weatherFragmentViewModel.latitude.toString() + weatherFragmentViewModel.longitude.toString()
                try {
                    weatherFragmentViewModel.mainWeatherProviderType =
                        WeatherFragmentViewModel.FINAL_RESPONSE_MAP.get(key).requestMainWeatherProviderType
                    reDraw()
                } catch (e: Exception) {
                    requestNewData()
                }
            }
        }
    }

    fun reDraw() {
        //날씨 프래그먼트 다시 그림
        val key: String = weatherFragmentViewModel.latitude.toString() + weatherFragmentViewModel.longitude.toString()
        if (WeatherFragmentViewModel.FINAL_RESPONSE_MAP.containsKey(key)) {
            shimmer(true, false)
            executorService.submit {
                val weatherProviderTypeSet: MutableSet<WeatherProviderType> = HashSet<WeatherProviderType>()
                weatherProviderTypeSet.add(weatherFragmentViewModel.mainWeatherProviderType)
                weatherProviderTypeSet.add(WeatherProviderType.AQICN)
                try {
                    val responseObj: WeatherResponseObj = WeatherFragmentViewModel.FINAL_RESPONSE_MAP.get(key)
                    if (responseObj != null) {
                        setWeatherFragments(
                            weatherProviderTypeSet, responseObj.multipleWeatherRestApiCallback,
                            weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude
                        )
                    } else {
                        requestNewData()
                    }
                } catch (e: Exception) {
                    requestNewData()
                }
            }
        }
    }

    fun onChangedCurrentLocation(currentLocation: Location) {
        weatherFragmentViewModel.latitude = currentLocation.latitude
        weatherFragmentViewModel.longitude = currentLocation.longitude
        WeatherFragmentViewModel.FINAL_RESPONSE_MAP.remove(weatherFragmentViewModel.latitude.toString() + weatherFragmentViewModel.longitude.toString())
        requestAddressOfLocation(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude, true)
    }

    fun requestNewData() {
        shimmer(true, false)
        weatherFragmentViewModel.requestNewData()
    }

    private fun requestNewDataWithAnotherWeatherSource(newWeatherProviderType: WeatherProviderType) {
        shimmer(true, false)
        weatherFragmentViewModel.requestNewDataWithAnotherWeatherSource(newWeatherProviderType)
    }

    private fun processOnResult(responseResultObj: ResponseResultObj) {
        val entrySet: Set<Map.Entry<WeatherProviderType, ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult>>> =
            responseResultObj.multipleWeatherRestApiCallback.responseMap.entries
        //메인 날씨 제공사의 데이터가 정상이면 메인 날씨 제공사의 프래그먼트들을 설정하고 값을 표시한다.
        //메인 날씨 제공사의 응답이 불량이면 재 시도, 취소 중 택1 다이얼로그 표시
        for ((weatherProviderType, value) in entrySet) {
            if (weatherProviderType === WeatherProviderType.AQICN) {
                continue
            }
            for (responseResult in value.values) {
                if (!responseResult.isSuccessful()) {
                    if (weatherFragmentViewModel.containWeatherData(
                            weatherFragmentViewModel.latitude,
                            weatherFragmentViewModel.longitude
                        )
                    ) {
                        MainThreadWorker.runOnUiThread(Runnable {
                            shimmer(false, false)
                            Toast.makeText(requireContext().applicationContext, R.string.update_failed, Toast.LENGTH_SHORT).show()
                        })
                    } else {
                        //다시시도, 취소 중 택1
                        val btnObjList: MutableList<BtnObj> = ArrayList<BtnObj>()
                        val otherTypes: Set<WeatherProviderType> = weatherFragmentViewModel.getOtherWeatherSourceTypes(
                            weatherProviderType,
                            weatherFragmentViewModel.mainWeatherProviderType
                        )
                        val failedDialogItems = arrayOfNulls<String>(otherTypes.size)
                        val weatherProviderTypeArr: Array<WeatherProviderType?> = arrayOfNulls<WeatherProviderType>(otherTypes.size)
                        var arrIndex = 0
                        if (otherTypes.contains(WeatherProviderType.KMA_WEB)) {
                            weatherProviderTypeArr[arrIndex] = WeatherProviderType.KMA_WEB
                            failedDialogItems[arrIndex++] = getString(R.string.kma) + ", " + getString(
                                R.string.rerequest_another_weather_datasource
                            )
                        }
                        if (otherTypes.contains(WeatherProviderType.ACCU_WEATHER)) {
                            weatherProviderTypeArr[arrIndex] = WeatherProviderType.ACCU_WEATHER
                            failedDialogItems[arrIndex++] = getString(R.string.accu_weather) + ", " + getString(
                                R.string.rerequest_another_weather_datasource
                            )
                        }
                        if (otherTypes.contains(WeatherProviderType.OWM_ONECALL)) {
                            weatherProviderTypeArr[arrIndex] = WeatherProviderType.OWM_ONECALL
                            failedDialogItems[arrIndex++] = getString(R.string.owm) + ", " + getString(
                                R.string.rerequest_another_weather_datasource
                            )
                        }
                        if (otherTypes.contains(WeatherProviderType.MET_NORWAY)) {
                            weatherProviderTypeArr[arrIndex] = WeatherProviderType.MET_NORWAY
                            failedDialogItems[arrIndex] = getString(R.string.met) + ", " + getString(
                                R.string.rerequest_another_weather_datasource
                            )
                        }
                        btnObjList.add(
                            BtnObj(
                                View.OnClickListener { v: View? ->
                                    iWeatherFragment.addWeatherFragment(
                                        weatherFragmentViewModel.locationType,
                                        weatherFragmentViewModel.selectedFavoriteAddressDto,
                                        null
                                    )
                                }, getString(
                                    R.string.again
                                )
                            )
                        )
                        var index = 0
                        for (anotherProvider in weatherProviderTypeArr) {
                            btnObjList.add(BtnObj(View.OnClickListener { v: View? ->
                                val argument = Bundle()
                                argument.putSerializable("anotherProvider", anotherProvider)
                                iWeatherFragment.addWeatherFragment(
                                    weatherFragmentViewModel.locationType,
                                    weatherFragmentViewModel.selectedFavoriteAddressDto,
                                    argument
                                )
                            }, failedDialogItems[index]))
                            index++
                        }
                        MainThreadWorker.runOnUiThread(Runnable {
                            shimmer(false, false)
                            setFailFragment(btnObjList)
                        })
                    }
                    return
                }
            }
        }

        //응답 성공 하면
        val weatherResponseObj = WeatherResponseObj(
            responseResultObj.multipleWeatherRestApiCallback,
            responseResultObj.weatherProviderTypeSet, responseResultObj.mainWeatherProviderType
        )
        WeatherFragmentViewModel.FINAL_RESPONSE_MAP.put(
            weatherFragmentViewModel.latitude.toString() + weatherFragmentViewModel.longitude.toString(),
            weatherResponseObj
        )
        setWeatherFragments(
            responseResultObj.weatherProviderTypeSet,
            responseResultObj.multipleWeatherRestApiCallback,
            weatherFragmentViewModel.latitude,
            weatherFragmentViewModel.longitude
        )
    }

    private fun setFailFragment(btnObjList: List<BtnObj>) {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putInt(AlertFragment.Constant.DRAWABLE_ID.name, R.drawable.error)
        bundle.putString(AlertFragment.Constant.MESSAGE.name, getString(R.string.update_failed))
        val alertFragment = AlertFragment()
        alertFragment.setMenuOnClickListener(menuOnClickListener)
        alertFragment.setBtnObjList(btnObjList)
        alertFragment.setArguments(bundle)
        fragmentTransaction.replace(
            R.id.fragment_container, alertFragment,
            AlertFragment::class.java.getName()
        ).setPrimaryNavigationFragment(alertFragment).commitNow()
    }

    private fun setWeatherFragments(
        weatherProviderTypeSet: Set<WeatherProviderType>, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?,
        latitude: Double, longitude: Double
    ) {
        val weatherDataDTO: WeatherDataDto = weatherFragmentViewModel.createWeatherFragments(
            weatherProviderTypeSet,
            multipleWeatherRestApiCallback, latitude,
            longitude
        )
        val defaultBundle = Bundle()
        defaultBundle.putDouble(BundleKey.Latitude.name, weatherDataDTO.getLatitude())
        defaultBundle.putDouble(BundleKey.Longitude.name, weatherDataDTO.getLongitude())
        defaultBundle.putString(BundleKey.AddressName.name, weatherDataDTO.getAddressName())
        defaultBundle.putString(BundleKey.CountryCode.name, weatherDataDTO.getCountryCode())
        defaultBundle.putSerializable(BundleKey.WeatherProvider.name, weatherDataDTO.getMainWeatherProviderType())
        defaultBundle.putSerializable(BundleKey.TimeZone.name, weatherDataDTO.getZoneId())

        // simple current conditions ------------------------------------------------------------------------------------------------------
        val simpleCurrentConditionsBundle = Bundle()
        simpleCurrentConditionsBundle.putAll(defaultBundle)
        val simpleCurrentConditionsFragment = SimpleCurrentConditionsFragment()
        simpleCurrentConditionsFragment.setCurrentConditionsDto(weatherDataDTO.getCurrentConditionsDto())
            .setAirQualityDto(weatherDataDTO.getAirQualityDto())

        // hourly forecasts ----------------------------------------------------------------------------------------------------------------
        val hourlyForecastBundle = Bundle()
        hourlyForecastBundle.putAll(defaultBundle)
        val simpleHourlyForecastFragment = SimpleHourlyForecastFragment()
        simpleHourlyForecastFragment.setHourlyForecastDtoList(weatherDataDTO.getHourlyForecastList())

        // daily forecasts ----------------------------------------------------------------------------------------------------------------
        val dailyForecastBundle = Bundle()
        dailyForecastBundle.putAll(defaultBundle)
        val simpleDailyForecastFragment = SimpleDailyForecastFragment()
        simpleDailyForecastFragment.setDailyForecastDtoList(weatherDataDTO.getDailyForecastList())

        // detail current conditions ----------------------------------------------
        val detailCurrentConditionsBundle = Bundle()
        detailCurrentConditionsBundle.putAll(defaultBundle)
        detailCurrentConditionsBundle.putSerializable(WeatherDataType.currentConditions.name, weatherDataDTO.getCurrentConditionsDto())
        val detailCurrentConditionsFragment = DetailCurrentConditionsFragment()
        detailCurrentConditionsFragment.setCurrentConditionsDto(weatherDataDTO.getCurrentConditionsDto())

        // air quality  ----------------------------------------------
        val airQualityBundle = Bundle()
        airQualityBundle.putAll(defaultBundle)
        val simpleAirQualityFragment = SimpleAirQualityFragment()
        simpleAirQualityFragment.setAirQualityDto(weatherDataDTO.getAirQualityDto())
            .setAqiCnGeolocalizedFeedResponse(weatherDataDTO.getAirQualityResponse())
        val rainViewerFragment = SimpleRainViewerFragment()
        rainViewerFragment.setArguments(defaultBundle)
        assert(rainViewerFragment.getArguments() != null)
        rainViewerFragment.getArguments().putBoolean("simpleMode", true)
        simpleAirQualityFragment.setArguments(airQualityBundle)
        val sunSetRiseFragment = SunsetriseFragment()
        sunSetRiseFragment.setArguments(defaultBundle)
        simpleHourlyForecastFragment.setArguments(hourlyForecastBundle)
        simpleDailyForecastFragment.setArguments(dailyForecastBundle)
        simpleCurrentConditionsFragment.setArguments(simpleCurrentConditionsBundle)
        detailCurrentConditionsFragment.setArguments(detailCurrentConditionsBundle)
        val flickrRequestParameter = FlickrRequestParameter(
            weatherDataDTO.getMainWeatherProviderType(), weatherDataDTO.getCurrentConditionsWeatherVal(), latitude, longitude,
            weatherDataDTO.getZoneId(), weatherDataDTO.getPrecipitationVolume(),
            ZonedDateTime.parse(multipleWeatherRestApiCallback.getRequestDateTime().toString())
        )
        flickrViewModel.setLastParameter(flickrRequestParameter)
        weatherFragmentViewModel.iTextColor = simpleCurrentConditionsFragment
        try {
            requireActivity().runOnUiThread {
                changeWeatherDataSourcePicker(weatherFragmentViewModel.countryCode)
                binding!!.updatedDatetime.setText(
                    multipleWeatherRestApiCallback.getRequestDateTime().format(weatherFragmentViewModel.dateTimeFormatter)
                )
                val fragmentManager = childFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction
                    .replace(
                        binding!!.simpleCurrentConditions.id,
                        simpleCurrentConditionsFragment, getString(R.string.tag_simple_current_conditions_fragment)
                    )
                    .replace(
                        binding!!.simpleHourlyForecast.id, simpleHourlyForecastFragment,
                        getString(R.string.tag_simple_hourly_forecast_fragment)
                    )
                    .replace(
                        binding!!.simpleDailyForecast.id,
                        simpleDailyForecastFragment,
                        getString(R.string.tag_simple_daily_forecast_fragment)
                    )
                    .replace(
                        binding!!.detailCurrentConditions.id, detailCurrentConditionsFragment,
                        getString(R.string.tag_detail_current_conditions_fragment)
                    )
                    .replace(binding!!.simpleAirQuality.id, simpleAirQualityFragment, getString(R.string.tag_simple_air_quality_fragment))
                    .replace(
                        binding!!.sunSetRise.id, sunSetRiseFragment,
                        getString(R.string.tag_sun_set_rise_fragment)
                    )
                    .replace(
                        binding!!.radar.id, rainViewerFragment,
                        SimpleRainViewerFragment::class.java.getName()
                    )
                    .commit()
            }
        } catch (e: Exception) {
        }
    }

    private fun changeWeatherDataSourcePicker(countryCode: String?) {
        val provide = getString(R.string.provide) + " : "
        when (weatherFragmentViewModel.mainWeatherProviderType) {
            WeatherProviderType.KMA_WEB, KMA_API -> {
                binding!!.weatherDataSourceName.text = String.format("%s%s", provide, getString(R.string.kma))
                binding!!.weatherDataSourceIcon.setImageResource(R.drawable.kmaicon)
            }
            ACCU_WEATHER -> {
                binding!!.weatherDataSourceName.text = String.format("%s%s", provide, getString(R.string.accu_weather))
                binding!!.weatherDataSourceIcon.setImageResource(R.drawable.accuicon)
            }
            WeatherProviderType.OWM_ONECALL, OWM_INDIVIDUAL -> {
                binding!!.weatherDataSourceName.text = String.format("%s%s", provide, getString(R.string.owm))
                binding!!.weatherDataSourceIcon.setImageResource(R.drawable.owmicon)
            }
            WeatherProviderType.MET_NORWAY -> {
                binding!!.weatherDataSourceName.text = String.format("%s%s", provide, getString(R.string.met))
                binding!!.weatherDataSourceIcon.setImageResource(R.drawable.metlogo)
            }
        }
        binding!!.weatherDataSourceLayout.setOnClickListener { view: View? ->
            val items = arrayOfNulls<CharSequence>(if (countryCode != null && countryCode == "KR") 3 else 2)
            var checkedItemIdx = 0
            if (countryCode != null && countryCode == "KR") {
                items[0] = getString(R.string.kma)
                items[1] = getString(R.string.owm)
                items[2] = getString(R.string.met)
                if (weatherFragmentViewModel.mainWeatherProviderType === WeatherProviderType.OWM_ONECALL) {
                    checkedItemIdx = 1
                } else if (weatherFragmentViewModel.mainWeatherProviderType === WeatherProviderType.MET_NORWAY) {
                    checkedItemIdx = 2
                }
            } else {
                items[0] = getString(R.string.owm)
                items[1] = getString(R.string.met)
                if (weatherFragmentViewModel.mainWeatherProviderType !== WeatherProviderType.OWM_ONECALL) {
                    checkedItemIdx = 1
                }
            }
            val finalCheckedItemIdx = checkedItemIdx
            MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.title_pick_weather_data_source).setSingleChoiceItems(items,
                finalCheckedItemIdx, DialogInterface.OnClickListener { dialogInterface: DialogInterface, index: Int ->
                    val newWeatherProviderType: WeatherProviderType
                    if (finalCheckedItemIdx != index) {
                        newWeatherProviderType = if (items[index] == getString(R.string.kma)) {
                            WeatherProviderType.KMA_WEB
                        } else if (items[index] == getString(R.string.met)) {
                            WeatherProviderType.MET_NORWAY
                        } else {
                            WeatherProviderType.OWM_ONECALL
                        }
                        requestNewDataWithAnotherWeatherSource(newWeatherProviderType)
                    }
                    dialogInterface.dismiss()
                }).create().show()
        }
    }

    private fun onChangedStateBackgroundImg(isShow: Boolean) {
        HeaderbarStyle.setStyle(if (isShow) HeaderbarStyle.Style.White else HeaderbarStyle.Style.Black, requireActivity())
        val color = if (isShow) Color.WHITE else Color.BLACK
        binding!!.weatherDataSourceName.setTextColor(color)
        binding!!.updatedDatetimeLabel.setTextColor(color)
        binding!!.updatedDatetime.setTextColor(color)
        binding!!.countryName.setTextColor(color)
        binding!!.addressName.setTextColor(color)
        TextViewCompat.setCompoundDrawableTintList(binding!!.weatherDataSourceName, ColorStateList.valueOf(color))
        try {
            weatherFragmentViewModel.iTextColor.changeColor(color)
        } catch (e: Exception) {
        }
    }

    private fun shimmer(showShimmer: Boolean, noChangeHeader: Boolean) {
        if (showShimmer) {
            if (!noChangeHeader) HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, requireActivity())
            binding!!.rootSubLayout.visibility = View.GONE
            binding!!.shimmer.startShimmer()
            binding!!.shimmer.visibility = View.VISIBLE
        } else {
            if (!noChangeHeader) HeaderbarStyle.setStyle(HeaderbarStyle.Style.White, requireActivity())
            binding!!.shimmer.stopShimmer()
            binding!!.shimmer.visibility = View.GONE
            binding!!.rootSubLayout.visibility = View.VISIBLE
        }
    }

    class ResponseResultObj(
        weatherProviderTypeSet: Set<WeatherProviderType>,
        requestWeatherSources: ArrayMap<WeatherProviderType, RequestWeatherSource>, mainWeatherProviderType: WeatherProviderType
    ) : Serializable {
        @JvmField var multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback? = null
        var weatherProviderTypeSet: Set<WeatherProviderType>
        var mainWeatherProviderType: WeatherProviderType
        var requestWeatherSources: ArrayMap<WeatherProviderType, RequestWeatherSource>

        init {
            this.weatherProviderTypeSet = weatherProviderTypeSet
            this.requestWeatherSources = requestWeatherSources
            this.mainWeatherProviderType = mainWeatherProviderType
        }
    }

    class WeatherResponseObj(
        multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?,
        requestWeatherProviderTypeSet: Set<WeatherProviderType>,
        requestMainWeatherProviderType: WeatherProviderType
    ) : Serializable {
        @JvmField var multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?
        @JvmField var requestWeatherProviderTypeSet: Set<WeatherProviderType>
        @JvmField var requestMainWeatherProviderType: WeatherProviderType
        @JvmField var dataDownloadedDateTime: LocalDateTime

        init {
            this.multipleWeatherRestApiCallback = multipleWeatherRestApiCallback
            this.requestWeatherProviderTypeSet = requestWeatherProviderTypeSet
            this.requestMainWeatherProviderType = requestMainWeatherProviderType
            dataDownloadedDateTime = LocalDateTime.now()
        }
    }

    interface IWeatherFragment {
        fun addWeatherFragment(locationType: LocationType?, favoriteAddressDto: FavoriteAddressDto?, arguments: Bundle?)
    }

    interface ITextColor {
        fun changeColor(color: Int)
    }

    interface OnAsyncLoadCallback {
        fun onFinished(fragment: Fragment?)
    }

    interface OnResumeFragment {
        fun onResumeWithAsync(fragment: Fragment?)
    }
}