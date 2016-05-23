package me.wizos.loread.gson.itemContents;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Origin {
	@SerializedName("streamId")
	String streamId;

	@SerializedName("title")
	String title;

	@SerializedName("htmlUrl")
	String htmlUrl;


	public String getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String toString(){
//		System.out.println("【Origin.toString()】" + toString());
		return "{\"streamId\": \""+streamId + "\",\"title\": \"" + title + "\",\"htmlUrl\": \"" + htmlUrl +"\"}";
	}

}
