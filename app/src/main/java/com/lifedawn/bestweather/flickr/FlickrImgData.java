package com.lifedawn.bestweather.flickr;

import android.graphics.Bitmap;

import com.lifedawn.bestweather.retrofit.responses.flickr.PhotosFromGalleryResponse;

import java.io.Serializable;

public class FlickrImgData implements Serializable {
	private Bitmap img;
	private String weather;
	private String time;
	private String volume;
	private PhotosFromGalleryResponse.Photos.Photo photo;

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getWeather() {
		return weather;
	}

	public void setWeather(String weather) {
		this.weather = weather;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Bitmap getImg() {
		return img;
	}

	public void setImg(Bitmap img) {
		this.img = img;
	}

	public PhotosFromGalleryResponse.Photos.Photo getPhoto() {
		return photo;
	}

	public FlickrImgData setPhoto(PhotosFromGalleryResponse.Photos.Photo photo) {
		this.photo = photo;
		return this;
	}

	public void clear() {
		img.recycle();
		img = null;

		weather = null;
		time = null;
		volume = null;
		photo = null;
	}

	public String getRealFlickrUrl() {
		return "https://www.flickr.com/photos/" + photo.getOwner() + "/" + photo.getId();
	}
}