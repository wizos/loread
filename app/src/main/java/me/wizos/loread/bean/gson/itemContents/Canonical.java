package me.wizos.loread.bean.gson.itemContents;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Canonical {
	@SerializedName("href")
	String href;

	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}

	public String toString(){
//		System.out.println("【Canonical.toString()】" + "[\"href\": \""+ href + "\"]");
		return "[\"href\": \""+ href + "\"]";
	}
}
