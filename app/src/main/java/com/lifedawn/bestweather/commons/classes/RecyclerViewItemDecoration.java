package com.lifedawn.bestweather.commons.classes;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class RecyclerViewItemDecoration extends RecyclerView.ItemDecoration {
	private final int marginHorizontal;
	private final int marginVertical;

	public RecyclerViewItemDecoration(Context context) {
		marginHorizontal = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.getResources().getDisplayMetrics());
		marginVertical = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.getResources().getDisplayMetrics());
	}

	@Override
	public void getItemOffsets(@NonNull @NotNull Rect outRect, @NonNull @NotNull View view, @NonNull @NotNull RecyclerView parent, @NonNull @NotNull RecyclerView.State state) {
		int position = parent.getChildLayoutPosition(view);
		int itemCounts = parent.getAdapter().getItemCount();

		//set right margin to all
		outRect.right = marginHorizontal;
		outRect.left = marginHorizontal;
		//set bottom margin to all
		outRect.bottom = marginVertical;
		//we only add top margin to the first row
		if (position < itemCounts) {
			outRect.top = marginVertical;
		}
		/*
		//add left margin only to the first column
		if(position%itemCounts==0){
			outRect.left = margin;
		}

		 */
	}

}
