package com.lifedawn.bestweather.ui.notification.ongoing

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.databinding.FragmentBaseNotificationSettingsBinding
import com.lifedawn.bestweather.ui.findaddress.map.MapFragment

class OngoingNotificationSettingsFragment constructor() : Fragment(), NotificationUpdateCallback {
    private val dataTypeOfIcons: Array<DataTypeOfIcon> =
        arrayOf<DataTypeOfIcon>(WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE, WidgetNotiConstants.DataTypeOfIcon.WEATHER_ICON)
    private var ongoingNotificationViewModel: OngoingNotificationViewModel? = null
    private var binding: FragmentBaseNotificationSettingsBinding? = null
    private var newSelectedAddressDto: FavoriteAddressDto? = null
    private var originalSelectedFavoriteAddressDto: FavoriteAddressDto? = null
    private var ongoingNotificationDto: OngoingNotificationDto? = null
    private var intervalsLong: LongArray
    private var selectedFavoriteLocation: Boolean = false
    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            public override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentDestroyed(fm, f)
                if (f is MapFragment) {
                    if (!f.isClickedItem && !selectedFavoriteLocation) {
                        binding!!.commons.currentLocationRadio.setChecked(true)
                    }
                }
            }
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ongoingNotificationViewModel = ViewModelProvider(requireActivity()).get(
            OngoingNotificationViewModel::class.java
        )
        getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
        val intervalsStr: Array<String> = getResources().getStringArray(R.array.AutoRefreshIntervalsLong)
        intervalsLong = LongArray(intervalsStr.size)
        for (i in intervalsStr.indices) {
            intervalsLong.get(i) = intervalsStr.get(i).toLong()
        }
    }

    public override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBaseNotificationSettingsBinding.inflate(inflater, container, false)
        val layoutParams: RelativeLayout.LayoutParams = binding!!.toolbar.getRoot().getLayoutParams() as RelativeLayout.LayoutParams
        layoutParams.topMargin = MyApplication.getStatusBarHeight()
        binding!!.toolbar.getRoot().setLayoutParams(layoutParams)
        binding!!.toolbar.backBtn.setOnClickListener(View.OnClickListener({ v: View? -> getParentFragmentManager().popBackStackImmediate() }))
        binding!!.toolbar.fragmentTitle.setText(R.string.always_notification)
        if (PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext())
                .getBoolean(getString(R.string.pref_key_met), true)
        ) {
            binding!!.commons.metNorwayRadio.setChecked(true)
        } else {
            binding!!.commons.owmRadio.setChecked(true)
        }
        val spinnerAdapter: SpinnerAdapter = ArrayAdapter(
            requireContext().getApplicationContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(
                R.array.AutoRefreshIntervals
            )
        )
        binding!!.commons.autoRefreshIntervalSpinner.setAdapter(spinnerAdapter)
        binding!!.dataTypeOfIconSpinner.setAdapter(
            ArrayAdapter(
                requireContext().getApplicationContext(), android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.DataTypeOfIcons)
            )
        )
        if (PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext())
                .getBoolean(getString(R.string.pref_key_met), true)
        ) {
            binding!!.commons.metNorwayRadio.setChecked(true)
        } else {
            binding!!.commons.owmRadio.setChecked(true)
        }
        initLocation()
        initWeatherProvider()
        initAutoRefreshInterval()
        initDataTypeOfIconSpinner()
        val ongoingNotiViewCreator: OngoingNotiViewCreator = OngoingNotiViewCreator(requireContext().getApplicationContext(), null)
        val remoteViews: Array<RemoteViews?>? = ongoingNotiViewCreator.createRemoteViews(true)
        val padding: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics()).toInt()
        remoteViews!!.get(1)!!.setViewPadding(R.id.root_layout, padding, padding, padding, padding)
        binding!!.previewLayout.addView(remoteViews.get(1)!!.apply(requireContext().getApplicationContext(), binding!!.previewLayout))
        return binding!!.getRoot()
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.saveBtn.setOnClickListener(View.OnClickListener({ v: View? ->
            ongoingNotificationViewModel!!.save(ongoingNotificationDto, BackgroundWorkCallback({
                MainThreadWorker.runOnUiThread(
                    Runnable({

                        // 저장된 알림 데이터가 있으면 알림 표시
                        val context: Context = requireContext().getApplicationContext()
                        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                            val ongoingNotificationHelper: OngoingNotificationHelper = OngoingNotificationHelper(context)
                            val pendingIntent: PendingIntent? = ongoingNotificationHelper.createManualPendingIntent(
                                getString(R.string.com_lifedawn_bestweather_action_REFRESH), PendingIntent.FLAG_UPDATE_CURRENT or
                                        PendingIntent.FLAG_IMMUTABLE
                            )
                            try {
                                pendingIntent!!.send()
                                getParentFragmentManager().popBackStack()
                            } catch (e: PendingIntent.CanceledException) {
                                e.printStackTrace()
                            }
                        } else {
                            ongoingNotificationViewModel!!.remove()
                            Toast.makeText(getContext(), R.string.disabledNotification, Toast.LENGTH_SHORT).show()
                            startActivity(IntentUtil.getNotificationSettingsIntent(getActivity()))
                        }
                    })
                )
            }))
        }))
    }

    public override fun onStart() {
        super.onStart()
        ongoingNotificationViewModel!!.getOngoingNotificationDto(object : DbQueryCallback<OngoingNotificationDto?>() {
            fun onResultSuccessful(originalDto: OngoingNotificationDto) {
                MainThreadWorker.runOnUiThread(Runnable({
                    ongoingNotificationDto = originalDto
                    if (originalDto.getLocationType() === LocationType.SelectedAddress) {
                        originalSelectedFavoriteAddressDto = FavoriteAddressDto()
                        originalSelectedFavoriteAddressDto.displayName = ongoingNotificationDto.getDisplayName()
                        originalSelectedFavoriteAddressDto.countryCode = ongoingNotificationDto.getCountryCode()
                        originalSelectedFavoriteAddressDto.latitude = ongoingNotificationDto.getLatitude().toString()
                        originalSelectedFavoriteAddressDto.longitude = ongoingNotificationDto.getLongitude().toString()
                        originalSelectedFavoriteAddressDto.zoneId = ongoingNotificationDto.getZoneId()
                        selectedFavoriteLocation = true
                        binding!!.commons.selectedAddressName.setText(ongoingNotificationDto.getDisplayName())
                        binding!!.commons.selectedLocationRadio.setChecked(true)
                    } else {
                        binding!!.commons.currentLocationRadio.setChecked(true)
                    }
                    init()
                }))
            }

            fun onResultNoData() {
                MainThreadWorker.runOnUiThread(Runnable({
                    ongoingNotificationDto = createDefaultDto()
                    init()
                }))
            }
        })
    }

    private fun init() {
        val intervalsStr: Array<String> = getResources().getStringArray(R.array.AutoRefreshIntervalsLong)
        val autoRefreshInterval: Long = ongoingNotificationDto.getUpdateIntervalMillis()
        for (i in intervalsStr.indices) {
            if (intervalsStr.get(i).toLong() == autoRefreshInterval) {
                binding!!.commons.autoRefreshIntervalSpinner.setSelection(i)
                break
            }
        }
        binding!!.dataTypeOfIconSpinner.setSelection(
            if (ongoingNotificationDto.getDataTypeOfIcon() === WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE) 0 else 1,
            false
        )
        binding!!.commons.kmaTopPrioritySwitch.setChecked(ongoingNotificationDto.isTopPriorityKma())
    }

    public override fun onDestroy() {
        getParentFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        super.onDestroy()
    }

    public override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    protected fun initAutoRefreshInterval() {
        binding!!.commons.autoRefreshIntervalSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            public override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                ongoingNotificationDto.setUpdateIntervalMillis(intervalsLong.get(position))
            }

            public override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    protected fun initDataTypeOfIconSpinner() {
        binding!!.dataTypeOfIconSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            public override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                ongoingNotificationDto.setDataTypeOfIcon(dataTypeOfIcons.get(position))
            }

            public override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    protected fun initWeatherProvider() {
        binding!!.commons.weatherDataSourceRadioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener({ group: RadioGroup?, checkedId: Int ->
            val checkedWeatherProviderType: WeatherProviderType =
                if (checkedId == R.id.met_norway_radio) WeatherProviderType.MET_NORWAY else WeatherProviderType.OWM_ONECALL
            onCheckedWeatherProvider(checkedWeatherProviderType)
        }))
        binding!!.commons.kmaTopPrioritySwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener({ buttonView: CompoundButton?, isChecked: Boolean ->
            ongoingNotificationDto.setTopPriorityKma(
                isChecked
            )
        }))
    }

    protected fun initLocation() {
        binding!!.commons.changeAddressBtn.setVisibility(View.GONE)
        binding!!.commons.selectedAddressName.setVisibility(View.GONE)
        binding!!.commons.locationRadioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener({ group: RadioGroup?, checkedId: Int ->
            if (checkedId == binding!!.commons.currentLocationRadio.getId() && binding!!.commons.currentLocationRadio.isChecked()) {
                binding!!.commons.changeAddressBtn.setVisibility(View.GONE)
                binding!!.commons.selectedAddressName.setVisibility(View.GONE)
                ongoingNotificationDto.setLocationType(LocationType.CurrentLocation)
            } else if (checkedId == binding!!.commons.selectedLocationRadio.getId() && binding!!.commons.selectedLocationRadio.isChecked()) {
                binding!!.commons.changeAddressBtn.setVisibility(View.VISIBLE)
                binding!!.commons.selectedAddressName.setVisibility(View.VISIBLE)
                if (selectedFavoriteLocation) {
                    onSelectedFavoriteLocation(originalSelectedFavoriteAddressDto)
                } else {
                    openFavoritesFragment()
                }
            }
        }))
        binding!!.commons.changeAddressBtn.setOnClickListener(View.OnClickListener({ v: View? -> openFavoritesFragment() }))
    }

    protected fun openFavoritesFragment() {
        val mapFragment: MapFragment = MapFragment()
        val bundle: Bundle = Bundle()
        bundle.putString(BundleKey.RequestFragment.name, OngoingNotificationSettingsFragment::class.java.getName())
        mapFragment.setArguments(bundle)
        mapFragment.setOnResultFavoriteListener(object : OnResultFavoriteListener {
            fun onAddedNewAddress(
                newFavoriteAddressDto: FavoriteAddressDto?,
                favoriteAddressDtoList: List<FavoriteAddressDto?>?,
                removed: Boolean
            ) {
            }

            fun onResult(favoriteAddressDtoList: List<FavoriteAddressDto?>?) {}
            fun onClickedAddress(favoriteAddressDto: FavoriteAddressDto?) {
                if (favoriteAddressDto == null) {
                    if (!selectedFavoriteLocation) {
                        Toast.makeText(getContext(), R.string.not_selected_address, Toast.LENGTH_SHORT).show()
                        binding!!.commons.currentLocationRadio.setChecked(true)
                    }
                } else {
                    newSelectedAddressDto = favoriteAddressDto
                    binding!!.commons.selectedAddressName.setText(newSelectedAddressDto.displayName)
                    onSelectedFavoriteLocation(newSelectedAddressDto)
                    getParentFragmentManager().popBackStack()
                }
            }
        })
        val tag: String = MapFragment::class.java.getName()
        getParentFragmentManager().beginTransaction().hide(this@OngoingNotificationSettingsFragment).add(
            R.id.fragment_container,
            mapFragment, tag
        ).addToBackStack(tag).commit()
    }

    fun onSelectedFavoriteLocation(favoriteAddressDto: FavoriteAddressDto?) {
        selectedFavoriteLocation = true
        originalSelectedFavoriteAddressDto = favoriteAddressDto
        ongoingNotificationDto.setDisplayName(favoriteAddressDto.displayName)
        ongoingNotificationDto.setCountryCode(favoriteAddressDto.countryCode)
        ongoingNotificationDto.setZoneId(favoriteAddressDto.zoneId)
        ongoingNotificationDto.setLatitude(favoriteAddressDto.latitude.toDouble())
        ongoingNotificationDto.setLongitude(favoriteAddressDto.longitude.toDouble())
        ongoingNotificationDto.setLocationType(LocationType.SelectedAddress)
    }

    public override fun updateNotification(remoteViews: RemoteViews?) {}
    fun onCheckedWeatherProvider(weatherProviderType: WeatherProviderType) {
        if (ongoingNotificationDto.getWeatherSourceType() !== weatherProviderType) {
            ongoingNotificationDto.setWeatherSourceType(weatherProviderType)
        }
    }

    private fun createDefaultDto(): OngoingNotificationDto {
        val defaultDto: OngoingNotificationDto = OngoingNotiDtoOngoing()
        defaultDto.setLocationType(LocationType.CurrentLocation)
        defaultDto.setOn(true)
        defaultDto.setWeatherSourceType(
            if (PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext()).getBoolean(
                    getString(
                        R.string.pref_key_met
                    ), true
                )
            ) WeatherProviderType.MET_NORWAY else WeatherProviderType.OWM_ONECALL
        )
        defaultDto.setTopPriorityKma(false)
        defaultDto.setUpdateIntervalMillis(0)
        defaultDto.setDataTypeOfIcon(WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE)
        return defaultDto
    }
}