package com.lifedawn.bestweather.flickr;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.lifedawn.bestweather.retrofit.responses.flickr.PhotosFromGalleryResponse;

import java.io.Serializable;

public class FlickrImgObj implements Serializable {
	Bitmap img;
	PhotosFromGalleryResponse.Photos.Photo photo;

	public Bitmap getImg() {
		return img;
	}

	public void setImg(Bitmap img) {
		this.img = img;
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