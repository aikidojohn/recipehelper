package com.johnhite.recipe.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ingredient_seasonality")
public class IngredientSeasonality {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	@Column(name = "ingredient_id")
	private int ingredientId;
	@Column(name = "month_bitmap")
	private int monthBitmap;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIngredientId() {
		return ingredientId;
	}
	public void setIngredientId(int ingredientId) {
		this.ingredientId = ingredientId;
	}
	public int getMonthBitmap() {
		return monthBitmap;
	}
	public void setMonthBitmap(int monthBitmap) {
		this.monthBitmap = monthBitmap;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ingredientId;
		result = prime * result + monthBitmap;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IngredientSeasonality other = (IngredientSeasonality) obj;
		if (id != other.id)
			return false;
		if (ingredientId != other.ingredientId)
			return false;
		if (monthBitmap != other.monthBitmap)
			return false;
		return true;
	}
	
	
}
