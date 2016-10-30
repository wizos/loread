package me.wizos.loread.bean.gson;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Map;

@Parcel
public class StreamPrefs {
	@SerializedName("streamprefs")
	Map<String, ArrayList<StreamPref>> streamPrefs;

	public Map<String, ArrayList<StreamPref>> getStreamPrefsMaps() {
		return streamPrefs;
	}

	public void setStreamPrefs(Map<String, ArrayList<StreamPref>> streamPrefs) {
		this.streamPrefs = streamPrefs;
		System.out.println("【StreamPrefs类】"+ streamPrefs );
	}
}
