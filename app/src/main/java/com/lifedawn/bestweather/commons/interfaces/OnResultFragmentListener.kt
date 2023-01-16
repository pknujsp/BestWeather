package com.lifedawn.bestweather.commons.interfaces

import android.os.Bundle
import java.io.Serializable

interface OnResultFragmentListener : Serializable {
    fun onResultFragment(result: Bundle?)
}