package com.auth.dao;

import com.auth.model.Accounts;

public interface AccountDao {

	int updateAccount(Accounts account);

	void createAccount(Accounts account);

}
