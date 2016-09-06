package com.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.dao.UserDao;
import com.auth.model.User;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	@Override
	public void saveUser(User user) {
		// TODO Auto-generated method stub
		userDao.saveUser(user);
	}

	@Override
	public User getUser(String email) {
		// TODO Auto-generated method stub
		return userDao.getUser(email);
	}

	@Override
	public void updateUser(User user) {
		userDao.updateUser(user);

	}

}
