package com.auth.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "accounts")
public class Accounts implements Serializable {

	@Id
	@Column(name = "account_id")
	private int accountId;

	@Column(name = "client")
	@Size(max = 45)
	private String client;

	@Column(name = "account")
	@Size(max = 45)
	private String account;

	@Column(name = "verified")
	private int verified;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@Size(max = 45)
	private User user;

	@Column(name = "provider_name")
	@Size(max = 45)
	private String provider_name;

	@Column(name = "provider_token")
	@Size(max = 100)
	private String provider_token;

	public int getAccountId() {
		return accountId;
	}

	public String getClient() {
		return client;
	}

	public String getAccount() {
		return account;
	}

	public User getUser() {
		return user;
	}

	public String getProvider_name() {
		return provider_name;
	}

	public String getProvider_token() {
		return provider_token;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public int getVerified() {
		return verified;
	}

	public void setVerified(int verified) {
		this.verified = verified;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setProvider_name(String provider_name) {
		this.provider_name = provider_name;
	}

	public void setProvider_token(String provider_token) {
		this.provider_token = provider_token;
	}

}
