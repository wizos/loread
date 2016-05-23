package me.wizos.loread.gson;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class UserInfo {
	@SerializedName("userId")
	long userId;

	@SerializedName("userName")
	String userName;

	@SerializedName("userProfileId")
	String userProfileId;

	@SerializedName("userEmail")
	String userEmail;

	@SerializedName("isBloggerUser")
	Boolean isBloggerUser;

	@SerializedName("signupTimeSec")
	long signupTimeSec;

	@SerializedName("isMultiLoginEnabled")
	Boolean isMultiLoginEnabled;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserProfileId() {
		return userProfileId;
	}

	public void setUserProfileId(String userProfileId) {
		this.userProfileId = userProfileId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public Boolean getIsBloggerUser() {
		return isBloggerUser;
	}

	public void setIsBloggerUser(Boolean isBloggerUser) {
		this.isBloggerUser = isBloggerUser;
	}

	public long getSignupTimeSec() {
		return signupTimeSec;
	}

	public void setSignupTimeSec(long signupTimeSec) {
		this.signupTimeSec = signupTimeSec;
	}

	public Boolean getIsMultiLoginEnabled() {
		return isMultiLoginEnabled;
	}

	public void setIsMultiLoginEnabled(Boolean isMultiLoginEnabled) {
		this.isMultiLoginEnabled = isMultiLoginEnabled;
	}
}
