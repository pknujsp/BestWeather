package com.lifedawn.bestweather.flickr;

import android.graphics.drawable.Drawable;

import com.lifedawn.bestweather.retrofit.responses.flickr.PhotosFromGalleryResponse;

import java.io.Serializable;

public class FlickrImgObj implements Serializable {
	Drawable img;
	PhotosFromGalleryResponse.Photos.Photo photo;

	public Drawable getImg() {
		return img;
	}

	public FlickrImgObj setImg(Drawable img) {
		this.img = img;
		return this;
	}

	public PhotosFromGalleryResponse.Photos.Photo getPhoto() {
		return photo;
	}

	public FlickrImgObj setPhoto(PhotosFromGalleryResponse.Photos.Photo photo) {
		this.photo = photo;
		return this;
	}

	public String getRealFlickrUrl() {
		return "https://www.flickr.com/photos/" + photo.getOwner() + "/" + photo.getId();
	}
}