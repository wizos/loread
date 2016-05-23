package me.wizos.loread.gson;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

import me.wizos.loread.gson.itemContents.Self;

@Parcel
public class GsStreamContents {


	@SerializedName("direction")
	String direction;

	@SerializedName("id")
	String id;

	@SerializedName("title")
	String title;

	@SerializedName("description")
	String description;

	@SerializedName("self")
	Self self;

	@SerializedName("updated")
	long updated;

	@SerializedName("updatedUsec")
	long updatedUsec;

	@SerializedName("items")
	ArrayList<Item> items;

	@SerializedName("continuation")
	String continuation;



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


	public long getUpdatedUsec() {
		return updatedUsec;
	}
	public void setUpdatedUsec(long updatedUsec) {
		this.updatedUsec = updatedUsec;
	}
}
