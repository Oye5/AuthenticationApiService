package com.auth.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "accounts")
public class Accounts implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "account_id")
	private String accountId;

	@Column(name = "client")
	@Size(max = 45)
	private String client;

	@Column(name = "account")
	@Size(max = 45)
	private String account;

	@Column(name = "verified")
	private int verified;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User userId;

	@Column(name = "provider_name")
	@Size(max = 45)
	private String provider_name;

	@Column(name = "provider_token")
	@Size(max = 100)
	private String provider_token;

	public String getAccountId() {
		return accountId;
	}

	public String getClient() {
		return client;
	}

	public String getAccount() {
		return account;
	}

	public int getVerified() {
		return verified;
	}

	public User getUserId() {
		return userId;
	}

	public String getProvider_name() {
		return provider_name;
	}

	public String getProvider_token() {
		return provider_token;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public void setVerified(int verified) {
		this.verified = verified;
	}

	public void setUserId(User userId) {
		this.userId = userId;
	}

	public void setProvider_name(String provider_name) {
		this.provider_name = provider_name;
	}

	public void setProvider_token(String provider_token) {
		this.provider_token = provider_token;
	}

}
