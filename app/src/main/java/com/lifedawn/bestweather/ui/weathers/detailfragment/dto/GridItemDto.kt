package com.lifedawn.bestweather.ui.weathers.detailfragment.dto

import android.graphics.drawable.Drawable

class GridItemDto {
    val label: String
    val value: String
    val img: Drawable?
    var imgRotate: Int? = null

    constructor(label: String, value: String, img: Drawable?) {
        this.label = label
        this.value = value
        this.img = img
    }

    constructor(label: String, value: String, img: Drawable?, imgRotate: Int?) {
        this.label = label
        this.value = value
        this.img = img
        this.imgRotate = imgRotate
    }

    fun setImgRotate(imgRotate: Int): GridItemDto {
        this.imgRotate = imgRotate
        return this
    }
}