package com.johnhite.recipe.db;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;

import com.johnhite.recipe.db.entity.RecipeIngredient;

public class RecipeIngredientDAO extends BaseDAO<RecipeIngredient> {

	public RecipeIngredientDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
}
