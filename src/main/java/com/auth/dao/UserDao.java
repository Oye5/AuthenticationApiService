package com.auth.dao;

import com.auth.model.User;

public interface UserDao {

	void saveUser(User user);

	User getUser(String email);

	void updateUser(User user);
}
