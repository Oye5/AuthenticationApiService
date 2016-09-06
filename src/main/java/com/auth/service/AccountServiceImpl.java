package com.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.dao.AccountDao;
import com.auth.model.Accounts;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

	@Autowired
	private AccountDao accountDao;

	@Override
	public int updateAccount(Accounts accounts) {
		return accountDao.updateAccount(accounts);

	}

	@Override
	public void createAccount(Accounts account) {
		accountDao.createAccount(account);

	}

}
