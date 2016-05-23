package me.wizos.loread.gson.itemContents;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Alternate {
	@SerializedName("href")
	String href;

	@SerializedName("type")
	String type;

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String toString(){
//		System.out.println("【Canonical.toString()】" + "[\"href\": \""+ href + "\"]");
		return "[\"href\": \""+ href + "\",\"type\":\"" + type + "\"]";
	}
}
