package com.auth.service;

import com.auth.model.User;

public interface UserService {

	void saveUser(User user);

	User getUser(String email);

	void updateUser(User user);

}
