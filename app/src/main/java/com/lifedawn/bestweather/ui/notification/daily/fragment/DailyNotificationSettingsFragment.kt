package com.lifedawn.bestweather.ui.notification.daily.fragment

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.databinding.FragmentDailyPushNotificationSettingsBinding
import com.lifedawn.bestweather.ui.findaddress.map.MapFragment
import com.lifedawn.bestweather.ui.notification.NotificationType
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DailyNotificationSettingsFragment : Fragment() {
    private val hoursFormatter = DateTimeFormatter.ofPattern("a h:mm")
    private var binding: FragmentDailyPushNotificationSettingsBinding? = null
    private var viewModel: DailyNotificationViewModel? = null
    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentDestroyed(fm, f)
                if (f is MapFragment) {
                    if (!f.isClickedItem && !viewModel.isSelectedFavoriteLocation()) {
                        binding!!.commons.currentLocationRadio.isChecked = true
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
        viewModel = ViewModelProvider(this).get<DailyNotificationViewModel>(DailyNotificationViewModel::class.java)
        viewModel.setMainWeatherProviderType(WeatherRequestUtil.getMainWeatherSourceType(context, null))
        viewModel.setBundle(savedInstanceState ?: arguments)
        viewModel.setNotificationSession(viewModel.getBundle().getBoolean(BundleKey.NewSession.name))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(viewModel.getBundle())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDailyPushNotificationSettingsBinding.inflate(inflater, container, false)
        val layoutParams: ConstraintLayout.LayoutParams = binding!!.toolbar.root.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topMargin = MyApplication.getStatusBarHeight()
        binding!!.toolbar.root.layoutParams = layoutParams
        binding!!.toolbar.fragmentTitle.setText(R.string.daily_notification)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.commons.autoRefreshIntervalSpinner.visibility = View.GONE
        binding!!.commons.autoRefreshIntervalLabel.visibility = View.GONE
        binding!!.commons.singleWeatherDataSourceLayout.visibility = View.VISIBLE
        binding!!.toolbar.backBtn.setOnClickListener { v: View? -> parentFragmentManager.popBackStackImmediate() }
        binding!!.hours.setOnClickListener { v: View? ->
            val time: String = viewModel.getEditingNotificationDto().alarmClock
            val localTime = LocalTime.parse(time)
            val builder: MaterialTimePicker.Builder = MaterialTimePicker.Builder()
            val timePicker: MaterialTimePicker = builder.setTitleText(R.string.clock)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(localTime.hour)
                .setMinute(localTime.minute)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()
            timePicker.addOnPositiveButtonClickListener(View.OnClickListener { v1: View? ->
                val newHour: Int = timePicker.getHour()
                val newMinute: Int = timePicker.getMinute()
                val newLocalTime = LocalTime.of(newHour, newMinute, 0)
                viewModel.getEditingNotificationDto().alarmClock = newLocalTime.toString()
                binding!!.hours.text = newLocalTime.format(hoursFormatter)
            })
            timePicker.addOnNegativeButtonClickListener(View.OnClickListener { v12: View? -> timePicker.dismiss() })
            timePicker.show(childFragmentManager, MaterialTimePicker::class.java.getName())
        }
        binding!!.save.setOnClickListener { v: View? ->
            if (viewModel.isNewNotificationSession()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationHelper = NotificationHelper(requireContext().applicationContext)
                    val notificationObj: NotificationObj = notificationHelper.getNotificationObj(NotificationType.Daily)
                    notificationHelper.createNotificationChannel(notificationObj)
                }
                viewModel.add(viewModel.getEditingNotificationDto(), object : DbQueryCallback<DailyPushNotificationDto?>() {
                    fun onResultSuccessful(result: DailyPushNotificationDto) {
                        MainThreadWorker.runOnUiThread(Runnable {
                            val text = LocalTime.parse(result.alarmClock)
                                .format(hoursFormatter) + ", " + getString(R.string.registeredDailyNotification)
                            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                            viewModel.getDailyNotificationHelper().enablePushNotification(result)
                            parentFragmentManager.popBackStack()
                        })
                    }

                    fun onResultNoData() {}
                })
            } else {
                viewModel.update(viewModel.getEditingNotificationDto(), object : DbQueryCallback<DailyPushNotificationDto?>() {
                    fun onResultSuccessful(result: DailyPushNotificationDto?) {
                        MainThreadWorker.runOnUiThread(Runnable {
                            viewModel.getDailyNotificationHelper().modifyPushNotification(result)
                            parentFragmentManager.popBackStack()
                        })
                    }

                    fun onResultNoData() {}
                })
            }
        }
        initLocation()
        initWeatherDataProvider()
        initNotificationTypeSpinner()
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.isNewNotificationSession()) {
            binding!!.commons.currentLocationRadio.isChecked = true
        } else {
            if (viewModel.getSavedNotificationDto().locationType === LocationType.SelectedAddress) {
                binding!!.commons.selectedLocationRadio.isChecked = true
                binding!!.commons.selectedAddressName.setText(viewModel.getSelectedFavoriteAddressDto().displayName)
            } else {
                binding!!.commons.currentLocationRadio.isChecked = true
            }
        }
        binding!!.notificationTypesSpinner.setSelection(viewModel.getEditingNotificationDto().notificationType.getIndex())
        val localTime = LocalTime.parse(viewModel.getEditingNotificationDto().alarmClock)
        binding!!.hours.text = localTime.format(hoursFormatter)
        val weatherProviderType: WeatherProviderType = viewModel.getEditingNotificationDto().weatherProviderType
        if (weatherProviderType != null) {
            if (weatherProviderType === WeatherProviderType.OWM_ONECALL) binding!!.commons.owmRadio.isChecked =
                true else binding!!.commons.metNorwayRadio.isChecked = true
        } else {
            binding!!.commons.weatherDataSourceRadioGroup.check(if (viewModel.getMainWeatherProviderType() === WeatherProviderType.OWM_ONECALL) binding!!.commons.owmRadio.id else binding!!.commons.metNorwayRadio.id)
        }
        binding!!.commons.kmaTopPrioritySwitch.isChecked = viewModel.getEditingNotificationDto().isTopPriorityKma
    }

    override fun onDestroy() {
        parentFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initNotificationTypeSpinner() {
        val spinnerAdapter: SpinnerAdapter =
            ArrayAdapter(context!!, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.DailyPushNotificationType))
        binding!!.notificationTypesSpinner.adapter = spinnerAdapter
        binding!!.notificationTypesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                onSelectedNotificationType(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun onSelectedNotificationType(position: Int) {
        val dailyPushNotificationType: DailyPushNotificationType = DailyPushNotificationType.Companion.valueOf(position)
        viewModel.getEditingNotificationDto().notificationType = dailyPushNotificationType
        val context = requireContext().applicationContext
        var viewCreator: AbstractDailyNotiViewCreator? = null
        when (dailyPushNotificationType) {
            DailyPushNotificationType.First -> {
                //시간별 예보
                binding!!.commons.singleWeatherDataSourceLayout.visibility = View.VISIBLE
                viewCreator = FirstDailyNotificationViewCreator(context)
                viewModel.getEditingNotificationDto().isShowAirQuality = false
                viewModel.getEditingNotificationDto().weatherProviderType =
                    if (binding!!.commons.metNorwayRadio.isChecked) WeatherProviderType.MET_NORWAY else WeatherProviderType.OWM_ONECALL
            }
            DailyPushNotificationType.Second -> {
                //현재날씨
                binding!!.commons.singleWeatherDataSourceLayout.visibility = View.VISIBLE
                viewCreator = SecondDailyNotificationViewCreator(context)
                viewModel.getEditingNotificationDto().isShowAirQuality = true
                viewModel.getEditingNotificationDto().weatherProviderType =
                    if (binding!!.commons.metNorwayRadio.isChecked) WeatherProviderType.MET_NORWAY else WeatherProviderType.OWM_ONECALL
            }
            DailyPushNotificationType.Third -> {
                //일별 예보
                binding!!.commons.singleWeatherDataSourceLayout.visibility = View.VISIBLE
                viewCreator = ThirdDailyNotificationViewCreator(context)
                viewModel.getEditingNotificationDto().isShowAirQuality = false
                viewModel.getEditingNotificationDto().weatherProviderType =
                    if (binding!!.commons.metNorwayRadio.isChecked) WeatherProviderType.MET_NORWAY else WeatherProviderType.OWM_ONECALL
            }
            DailyPushNotificationType.Fourth -> {
                //현재 대기질
                binding!!.commons.singleWeatherDataSourceLayout.visibility = View.GONE
                viewCreator = FourthDailyNotificationViewCreator(context)
                viewModel.getEditingNotificationDto().isShowAirQuality = true
                viewModel.getEditingNotificationDto().weatherProviderType = null
            }
            else -> {
                //대기질 예보
                binding!!.commons.singleWeatherDataSourceLayout.visibility = View.GONE
                viewCreator = FifthDailyNotificationViewCreator(context)
                viewModel.getEditingNotificationDto().isShowAirQuality = true
                viewModel.getEditingNotificationDto().weatherProviderType = null
            }
        }
        val remoteViews: RemoteViews = viewCreator.createRemoteViews(true)
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        remoteViews.setViewPadding(R.id.root_layout, padding, padding, padding, padding)
        val previewWidgetView = remoteViews.apply(context, binding!!.previewLayout)
        binding!!.previewLayout.removeAllViews()
        binding!!.previewLayout.addView(previewWidgetView)
    }

    private fun initWeatherDataProvider() {
        binding!!.commons.weatherDataSourceRadioGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            val checked: WeatherProviderType =
                if (checkedId == R.id.met_norway_radio) WeatherProviderType.MET_NORWAY else WeatherProviderType.OWM_ONECALL
            viewModel.getEditingNotificationDto().weatherProviderType = checked
        }
        binding!!.commons.kmaTopPrioritySwitch.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            viewModel.getEditingNotificationDto().isTopPriorityKma = isChecked
        }
    }

    private fun initLocation() {
        binding!!.commons.changeAddressBtn.visibility = View.GONE
        binding!!.commons.selectedAddressName.visibility = View.GONE
        binding!!.commons.locationRadioGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            if (checkedId == binding!!.commons.currentLocationRadio.id && binding!!.commons.currentLocationRadio.isChecked) {
                binding!!.commons.changeAddressBtn.visibility = View.GONE
                binding!!.commons.selectedAddressName.visibility = View.GONE
                viewModel.getEditingNotificationDto().locationType = LocationType.CurrentLocation
            } else if (checkedId == binding!!.commons.selectedLocationRadio.id && binding!!.commons.selectedLocationRadio.isChecked) {
                binding!!.commons.changeAddressBtn.visibility = View.VISIBLE
                binding!!.commons.selectedAddressName.visibility = View.VISIBLE
                if (viewModel.isSelectedFavoriteLocation()) {
                    viewModel.getEditingNotificationDto().addressName = viewModel.getSelectedFavoriteAddressDto().displayName
                    viewModel.getEditingNotificationDto().latitude = viewModel.getSelectedFavoriteAddressDto().latitude.toDouble()
                    viewModel.getEditingNotificationDto().longitude = viewModel.getSelectedFavoriteAddressDto().longitude.toDouble()
                    viewModel.getEditingNotificationDto().zoneId = viewModel.getSelectedFavoriteAddressDto().zoneId
                    viewModel.getEditingNotificationDto().countryCode = viewModel.getSelectedFavoriteAddressDto().countryCode
                    viewModel.getEditingNotificationDto().locationType = LocationType.SelectedAddress
                } else {
                    openFavoritesFragment()
                }
            }
        }
        binding!!.commons.changeAddressBtn.setOnClickListener { v: View? -> openFavoritesFragment() }
    }

    private fun openFavoritesFragment() {
        val mapFragment = MapFragment()
        val bundle = Bundle()
        bundle.putString(BundleKey.RequestFragment.name, DailyNotificationSettingsFragment::class.java.name)
        mapFragment.arguments = bundle
        mapFragment.setOnResultFavoriteListener(object : OnResultFavoriteListener {
            fun onAddedNewAddress(
                newFavoriteAddressDto: FavoriteAddressDto?,
                favoriteAddressDtoList: List<FavoriteAddressDto?>?,
                removed: Boolean
            ) {
                onClickedAddress(newFavoriteAddressDto)
            }

            fun onResult(favoriteAddressDtoList: List<FavoriteAddressDto?>?) {}
            fun onClickedAddress(favoriteAddressDto: FavoriteAddressDto?) {
                if (favoriteAddressDto == null) {
                    if (!viewModel.isSelectedFavoriteLocation()) {
                        Toast.makeText(context, R.string.not_selected_address, Toast.LENGTH_SHORT).show()
                        binding!!.commons.currentLocationRadio.isChecked = true
                    }
                } else {
                    viewModel.setSelectedFavoriteLocation(true)
                    viewModel.setSelectedFavoriteAddressDto(favoriteAddressDto)
                    binding!!.commons.selectedAddressName.setText(favoriteAddressDto.displayName)

                    //address,latitude,longitude,countryCode
                    viewModel.getEditingNotificationDto().addressName = favoriteAddressDto.displayName
                    viewModel.getEditingNotificationDto().latitude = favoriteAddressDto.latitude.toDouble()
                    viewModel.getEditingNotificationDto().longitude = favoriteAddressDto.longitude.toDouble()
                    viewModel.getEditingNotificationDto().zoneId = favoriteAddressDto.zoneId
                    viewModel.getEditingNotificationDto().countryCode = favoriteAddressDto.countryCode
                    viewModel.getEditingNotificationDto().locationType = LocationType.SelectedAddress
                    parentFragmentManager.popBackStack()
                }
            }
        })
        val tag = MapFragment::class.java.name
        parentFragmentManager.beginTransaction().hide(this@DailyNotificationSettingsFragment).add(
            R.id.fragment_container,
            mapFragment, tag
        ).addToBackStack(tag).commit()
    }
}