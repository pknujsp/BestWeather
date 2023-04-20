package com.lifedawn.bestweather.commons.classes

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class RecyclerViewItemDecoration(context: Context, hasFab: Boolean, fabCenterHeight: Int) : ItemDecoration() {
    private val marginHorizontal: Int
    private val marginVertical: Int
    private val hasFab: Boolean
    private val bottomFreeSpace: Int

    init {
        marginHorizontal = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
        marginVertical = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics).toInt()
        this.hasFab = hasFab
        bottomFreeSpace = fabCenterHeight * 2
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildLayoutPosition(view)
        val itemCounts = parent.adapter!!.itemCount

        //set right margin to all
        outRect.right = marginHorizontal
        outRect.left = marginHorizontal
        //set bottom margin to all
        //we only add top margin to the first row
        if (position < itemCounts) {
            outRect.top = marginVertical
        }
        if (hasFab && position == itemCounts - 1) {
            outRect.bottom = marginVertical + bottomFreeSpace
        } else {
            outRect.bottom = marginVertical
        }
        /*
		//add left margin only to the first column
		if(position%itemCounts==0){
			outRect.left = margin;
		}

		 */
    }
}