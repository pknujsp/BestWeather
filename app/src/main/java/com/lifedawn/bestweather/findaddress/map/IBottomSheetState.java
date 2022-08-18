package com.lifedawn.bestweather.findaddress.map;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.Set;

public interface IBottomSheetState {
	void setStateOfBottomSheet(BottomSheetType bottomSheetType, int state);

	int getStateOfBottomSheet(BottomSheetType bottomSheetType);

	void collapseAllExpandedBottomSheets();
}
