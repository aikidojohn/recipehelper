package com.johnhite.recipe.db;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;

public abstract class BaseDAO<T> extends AbstractDAO<T> {

	public BaseDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	public T persist(T entity) {
		return super.persist(entity);
	}
	
	public Optional<T> getById(Serializable id) {
		return Optional.ofNullable(super.get(id));
	}
	
	public List<T> list() {
		CriteriaQuery<T> query = super.criteriaQuery();
		Root<T> root = query.from(super.getEntityClass());
		query.select(root);
		return super.list(query);
	}

	public List<T> findByPropertyExact(String property, Object value) {
		CriteriaQuery<T> query = super.criteriaQuery();
		Root<T> root = query.from(super.getEntityClass());
		query
			.select(root)
			.where(currentSession().getCriteriaBuilder().equal(root.get(property), value));
		return super.list(query);
	}
	
	public List<T> findByPropertyLike(String property, Object value) {
		CriteriaQuery<T> query = super.criteriaQuery();
		Root<T> root = query.from(super.getEntityClass());
		query
			.select(root)
			.where(currentSession().getCriteriaBuilder().like(root.get(property), currentSession().getCriteriaBuilder().literal(value.toString())));
		return super.list(query);
	}
}
