package me.wizos.loread.bean.gson;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

import me.wizos.loread.bean.gson.itemContents.Self;

@Parcel
public class StreamContents {
	@SerializedName("self")
	Self self;

	@SerializedName("description")
	String description;

	@SerializedName("direction")
	String direction;

	@SerializedName("continuation")
	String continuation;

	@SerializedName("id")
	String id;

	@SerializedName("title")
	String title;

	@SerializedName("updated")
	long updated;

	@SerializedName("items")
	ArrayList<Item> items;

	public String getContinuation() {
		return continuation;
	}

	public void setContinuation(String continuation) {
		this.continuation = continuation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<Item> getItems() {
		return items;
	}

	public void setItems(ArrayList<Item> items) {
		this.items = items;
	}

	public Self getSelf() {
		return self;
	}

	public void setSelf(Self self) {
		this.self = self;
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
