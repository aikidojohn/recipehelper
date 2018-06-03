package com.johnhite.recipe.db;

import org.hibernate.SessionFactory;

import com.johnhite.recipe.db.entity.RecipeEntity;

public class RecipeDAO extends BaseDAO<RecipeEntity> {

	public RecipeDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

}
