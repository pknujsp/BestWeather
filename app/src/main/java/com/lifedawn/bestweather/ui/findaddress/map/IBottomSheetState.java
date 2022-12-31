package com.lifedawn.bestweather.ui.findaddress.map;

public interface IBottomSheetState {
	void setStateOfBottomSheet(BottomSheetType bottomSheetType, int state);

	int getStateOfBottomSheet(BottomSheetType bottomSheetType);

	void collapseAllExpandedBottomSheets();
}
