package me.wizos.loread.bean.gson.itemContents;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Summary {
	@SerializedName("direction")
	String direction;

	@SerializedName("content")
	String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
}
