package com.johnhite.recipe.db;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;

import com.johnhite.recipe.db.entity.Ingredient;

public class IngredientDAO extends BaseDAO<Ingredient> {

	public IngredientDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	public Optional<Ingredient> getByNameExact(String name) {
		List<Ingredient> ingredients = findByPropertyExact("name", name);
		if (ingredients.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(ingredients.get(0));
	}
	
	public List<Ingredient> getByNameLike(String name) {
		return findByPropertyLike("name", name);
	}
	
	public List<Ingredient> getByType(String type) {
		return findByPropertyLike("type", type);
	}
	
	public List<Ingredient> getByCategory(String category) {
		return findByPropertyLike("category", category);
	}
	
	public List<Ingredient> findProduceLike(String name) {
		CriteriaBuilder builder = currentSession().getCriteriaBuilder();
		CriteriaQuery<Ingredient> query = criteriaQuery();
		Root<Ingredient> root = query.from(getEntityClass());
		query
			.select(root)
			.where(
					builder.and(
							builder.like(root.get("name"), currentSession().getCriteriaBuilder().literal(name)),
							builder.equal(root.get("type"), "produce")
					)
			);
		return list(query);
	}
}
