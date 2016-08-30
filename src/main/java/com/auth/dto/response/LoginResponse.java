package com.auth.dto.response;

public class LoginResponse {

	private String providerToken;
	private String userName;
	private String userId;

	public String getProviderToken() {
		return providerToken;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserId() {
		return userId;
	}

	public void setProviderToken(String providerToken) {
		this.providerToken = providerToken;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
