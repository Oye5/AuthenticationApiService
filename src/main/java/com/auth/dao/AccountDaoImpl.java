package com.auth.dao;

import org.springframework.stereotype.Repository;

import com.auth.model.Accounts;

@Repository
public class AccountDaoImpl extends AbstractDao<Long, Accounts> implements AccountDao {

	@Override
	public int updateAccount(Accounts account) {
		return updateAccounts(account);

	}

}
