package com.lifedawn.bestweather.retrofit.responses.flickr;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhotosFromGalleryResponse {

	@Expose
	@SerializedName("photos")
	private Photos photos;

	public Photos getPhotos() {
		return photos;
	}

	public void setPhotos(Photos photos) {
		this.photos = photos;
	}

	public static class Photos {
		@Expose
		@SerializedName("page")
		private String page;

		@Expose
		@SerializedName("pages")
		private String pages;

		@Expose
		@SerializedName("perpage")
		private String perPage;

		@Expose
		@SerializedName("total")
		private String total;

		@Expose
		@SerializedName("photo")
		private List<Photo> photo;

		public String getPage() {
			return page;
		}

		public void setPage(String page) {
			this.page = page;
		}

		public String getPages() {
			return pages;
		}

		public void setPages(String pages) {
			this.pages = pages;
		}

		public String getPerPage() {
			return perPage;
		}

		public void setPerPage(String perPage) {
			this.perPage = perPage;
		}

		public String getTotal() {
			return total;
		}

		public void setTotal(String total) {
			this.total = total;
		}

		public List<Photo> getPhoto() {
			return photo;
		}

		public void setPhoto(List<Photo> photo) {
			this.photo = photo;
		}

		public static class Photo {
			@Expose
			@SerializedName("id")
			private String id;

			@Expose
			@SerializedName("owner")
			private String owner;

			@Expose
			@SerializedName("secret")
			private String secret;

			@Expose
			@SerializedName("server")
			private String server;

			@Expose
			@SerializedName("farm")
			private String farm;

			@Expose
			@SerializedName("title")
			private String title;

			@Expose
			@SerializedName("ispublic")
			private String isPublic;

			@Expose
			@SerializedName("isfriend")
			private String isFriend;

			@Expose
			@SerializedName("isfamily")
			private String isFamily;

			@Expose
			@SerializedName("is_primary")
			private String isPrimary;

			@Expose
			@SerializedName("has_comment")
			private String hasComment;

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public String getOwner() {
				return owner;
			}

			public void setOwner(String owner) {
				this.owner = owner;
			}

			public String getSecret() {
				return secret;
			}

			public void setSecret(String secret) {
				this.secret = secret;
			}

			public String getServer() {
				return server;
			}

			public void setServer(String server) {
				this.server = server;
			}

			public String getFarm() {
				return farm;
			}

			public void setFarm(String farm) {
				this.farm = farm;
			}

			public String getTitle() {
				return title;
			}

			public void setTitle(String title) {
				this.title = title;
			}

			public String getIsPublic() {
				return isPublic;
			}

			public void setIsPublic(String isPublic) {
				this.isPublic = isPublic;
			}

			public String getIsFriend() {
				return isFriend;
			}

			public void setIsFriend(String isFriend) {
				this.isFriend = isFriend;
			}

			public String getIsFamily() {
				return isFamily;
			}

			public void setIsFamily(String isFamily) {
				this.isFamily = isFamily;
			}

			public String getIsPrimary() {
				return isPrimary;
			}

			public void setIsPrimary(String isPrimary) {
				this.isPrimary = isPrimary;
			}

			public String getHasComment() {
				return hasComment;
			}

			public void setHasComment(String hasComment) {
				this.hasComment = hasComment;
			}
		}
	}
}
