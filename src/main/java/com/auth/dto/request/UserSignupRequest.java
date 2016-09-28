package com.auth.dto.request;

public class UserSignupRequest {

	private String id;
	private String appType;

	private String email;

	private String username;

	private String password;

	public String getId() {
		return id;
	}

	public String getAppType() {
		return appType;
	}

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
