package com.lifedawn.bestweather.ui.intro

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationResult
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver
import com.lifedawn.bestweather.commons.classes.MainThreadWorker.runOnUiThread
import com.lifedawn.bestweather.commons.classes.location.FusedLocation
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.constants.LocationType
import com.lifedawn.bestweather.commons.views.base.BaseFragment
import com.lifedawn.bestweather.commons.views.ProgressDialog.clearDialogs
import com.lifedawn.bestweather.commons.views.ProgressDialog.show
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto
import com.lifedawn.bestweather.databinding.FragmentIntroBinding
import com.lifedawn.bestweather.ui.findaddress.map.MapFragment
import com.lifedawn.bestweather.ui.findaddress.map.MapFragment.OnResultFavoriteListener
import com.lifedawn.bestweather.ui.main.viewmodel.InitViewModel
import com.lifedawn.bestweather.ui.main.view.MainFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IntroFragment @Inject constructor(
    private val fusedLocation : FusedLocation
) : BaseFragment<FragmentIntroBinding>(R.layout.fragment_intro) {
    private var initViewModel by activityViewModels<InitViewModel>()
    private var locationLifeCycleObserver: LocationLifeCycleObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationLifeCycleObserver = LocationLifeCycleObserver(requireActivity().activityResultRegistry, requireActivity())
        lifecycle.addObserver(locationLifeCycleObserver!!)
        parentFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeBtn.setOnClickListener({ v: View? -> requireActivity().finish() })
        binding.useCurrentLocation.setOnClickListener(View.OnClickListener {
            show(requireActivity(), getString(R.string.msg_finding_current_location), null)
            fusedLocation.findCurrentLocation(locationCallback, false)
        })
        binding!!.findAddress.setOnClickListener {
            it.findNavController().navigate(R.id.action_introFragment_to_mapFragment)
            val mapFragment = MapFragment()
            val bundle = Bundle()
            bundle.putString(BundleKey.RequestFragment.name, IntroFragment::class.java.name)
            mapFragment.arguments = bundle
            mapFragment.setOnResultFavoriteListener(object : OnResultFavoriteListener {
                override fun onAddedNewAddress(
                    newFavoriteAddressDto: FavoriteAddressDto,
                    favoriteAddressDtoList: List<FavoriteAddressDto>,
                    removed: Boolean
                ) {
                    parentFragmentManager.popBackStackImmediate()
                    val newFavoriteAddressDtoId = (newFavoriteAddressDto.id)!!
                    PreferenceManager.getDefaultSharedPreferences((context)!!).edit().putInt(
                        getString(R.string.pref_key_last_selected_favorite_address_id),
                        newFavoriteAddressDtoId
                    ).putString(
                        getString(R.string.pref_key_last_selected_location_type),
                        LocationType.SelectedAddress.name
                    ).putBoolean(getString(R.string.pref_key_show_intro), false).commit()
                    val mainFragment = MainFragment()
                    parentFragment!!.parentFragmentManager.beginTransaction().replace(
                        R.id.fragment_container, mainFragment,
                        mainFragment.tag
                    ).commitAllowingStateLoss()
                }

                override fun onResult(favoriteAddressDtoList: List<FavoriteAddressDto>) {}
                override fun onClickedAddress(favoriteAddressDto: FavoriteAddressDto?) {}
            })
            parentFragmentManager.beginTransaction().hide(this@IntroFragment).add(
                R.id.fragment_container, mapFragment,
                MapFragment::class.java.name
            ).addToBackStack(MapFragment::class.java.name).setPrimaryNavigationFragment(mapFragment).commitAllowingStateLoss()
        }
    }

    override fun onStart() {
        super.onStart()
        initViewModel!!.ready = true
    }

    override fun onDestroy() {
        lifecycle.removeObserver((locationLifeCycleObserver)!!)
        super.onDestroy()
    }

    private val locationCallback: FusedLocation.MyLocationCallback = object : MyLocationCallback() {
        fun onSuccessful(locationResult: LocationResult?) {
            //현재 위치 파악 성공
            PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                .edit().putBoolean(getString(R.string.pref_key_show_intro), false).commit()
            clearDialogs()
            val mainFragment = MainFragment()
            parentFragment!!.parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container, mainFragment,
                    mainFragment.tag
                ).commitAllowingStateLoss()
        }

        fun onFailed(fail: FusedLocation.Fail) {
            clearDialogs()
            if (fail === FusedLocation.Fail.DISABLED_GPS) {
                fusedLocation!!.onDisabledGps(
                    requireActivity(),
                    (locationLifeCycleObserver)!!,
                    object : ActivityResultCallback<ActivityResult?> {
                        override fun onActivityResult(result: ActivityResult?) {
                            if (fusedLocation!!.isOnGps) {
                                binding!!.useCurrentLocation.callOnClick()
                            }
                        }
                    })
            } else if (fail === FusedLocation.Fail.DENIED_LOCATION_PERMISSIONS) {
                fusedLocation!!.onRejectPermissions(
                    requireActivity(),
                    (locationLifeCycleObserver)!!,
                    object : ActivityResultCallback<ActivityResult?> {
                        override fun onActivityResult(result: ActivityResult?) {
                            if (fusedLocation!!.checkDefaultPermissions()) {
                                binding!!.useCurrentLocation.callOnClick()
                            }
                        }
                    },
                    object : ActivityResultCallback<Map<String?, Boolean?>> {
                        override fun onActivityResult(result: Map<String?, Boolean?>) {
                            if (!result.containsValue(false)) {
                                binding!!.useCurrentLocation.callOnClick()
                            } else {
                            }
                        }
                    })
            } else {
                //검색 실패
                runOnUiThread(object : Runnable {
                    override fun run() {
                        Toast.makeText(context, R.string.failedFindingLocation, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}