package com.auth.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.auth.model.User;

@Repository
public class UserDaoImpl extends AbstractDao<Long, User> implements UserDao {

	@Override
	public void saveUser(User user) {
		persist(user);
	}

	@Override
	public User getUser(String email) {
		Criteria criteria = createEntityCriteria();
		criteria.add(Restrictions.eq("email", email));
		return (User) criteria.uniqueResult();
	}

	@Override
	public void updateUser(User user) {
		update(user);

	}

}
