package me.wizos.loread.bean.gson;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

import me.wizos.loread.bean.gson.itemContents.Alternate;
import me.wizos.loread.bean.gson.itemContents.Canonical;
import me.wizos.loread.bean.gson.itemContents.Origin;
import me.wizos.loread.bean.gson.itemContents.Summary;

@Parcel
public class Item {
	@SerializedName("canonical")
	ArrayList<Canonical> canonical;

	@SerializedName("origin")
	Origin origin;

	@SerializedName("author")
	String author;

	@SerializedName("categories")
	ArrayList<String> categories;

	@SerializedName("published")
	long published;

	@SerializedName("timestampUsec")
	long timestampUsec;

	@SerializedName("summary")
	Summary summary;

	@SerializedName("crawlTimeMsec")
	long crawlTimeMsec;

	@SerializedName("id")
	String id;

	@SerializedName("title")
	String title;

	@SerializedName("updated")
	long updated;

	@SerializedName("alternate")
	ArrayList<Alternate> alternate;

	public ArrayList<Alternate> getAlternate() {
		return alternate;
	}

	public void setAlternate(ArrayList<Alternate> alternate) {
		this.alternate = alternate;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public ArrayList<Canonical> getCanonical() {
		return canonical;
	}

	public void setCanonical(ArrayList<Canonical> canonical) {
		this.canonical = canonical;
	}

	public ArrayList<String> getCategories() {
		return categories;
	}

	public void setCategories(ArrayList<String> categories) {
		this.categories = categories;
	}

	public long getCrawlTimeMsec() {
		return crawlTimeMsec;
	}

	public void setCrawlTimeMsec(long crawlTimeMsec) {
		this.crawlTimeMsec = crawlTimeMsec;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Origin getOrigin() {
		return origin;
	}

	public void setOrigin(Origin origin) {
		this.origin = origin;
	}

	public long getPublished() {
		return published;
	}

	public void setPublished(long published) {
		this.published = published;
	}

	public Summary getSummary() {
		return summary;
	}

	public void setSummary(Summary summary) {
		this.summary = summary;
	}

	public long getTimestampUsec() {
		return timestampUsec;
	}

	public void setTimestampUsec(long timestampUsec) {
		this.timestampUsec = timestampUsec;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getUpdated() {
		return updated;
	}

	public void setUpdated(long updated) {
		this.updated = updated;
	}
}
