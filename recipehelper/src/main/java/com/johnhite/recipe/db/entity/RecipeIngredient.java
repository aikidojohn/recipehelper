package com.johnhite.recipe.db.entity;

import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.math3.fraction.Fraction;

import com.google.common.base.Strings;
import com.johnhite.recipe.core.Conversion;

@Entity
@Table(name = "recipe_ingredient")
public class RecipeIngredient {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "recipe_id")
	private int recipeId;
	@Column(name = "amount_numerator")
	private int amountNumerator;
	@Column(name = "amount_denominator")
	private int amountDenominator;
	@Column(name = "measurement")
	private String measurement;
	@Column(name = "ingredient_name")
	private String name;
	@Column(name = "ingredient_id")
	private Integer ingredientId;
	
	@Transient
	private Fraction amount;
	
	public RecipeIngredient() {
		
	}
	
	public RecipeIngredient(RecipeIngredient other, Fraction amount) {
		this.recipeId = other.recipeId;
		this.measurement = other.measurement;
		this.name = other.name;
		this.ingredientId = other.ingredientId;
		setAmount(amount);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(int recipeId) {
		this.recipeId = recipeId;
	}

	public int getAmountNumerator() {
		return amountNumerator;
	}

	public void setAmountNumerator(int amountNumerator) {
		this.amountNumerator = amountNumerator;
	}

	public int getAmountDenominator() {
		return amountDenominator;
	}

	public void setAmountDenominator(int amountDenominator) {
		this.amountDenominator = amountDenominator;
	}

	public String getMeasurement() {
		return measurement;
	}

	public void setMeasurement(String measurement) {
		this.measurement = measurement;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(Integer ingredientId) {
		this.ingredientId = ingredientId;
	}

	public Fraction getAmount() {
		if (amount == null) {
			amount = new Fraction(amountNumerator, amountDenominator);
		}
		return amount;
	}

	public void setAmount(Fraction amount) {
		this.amount = amount;
		this.amountNumerator = amount.getNumerator();
		this.amountDenominator = amount.getDenominator();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + amountDenominator;
		result = prime * result + amountNumerator;
		result = prime * result + id;
		result = prime * result + ((ingredientId == null) ? 0 : ingredientId.hashCode());
		result = prime * result + ((measurement == null) ? 0 : measurement.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + recipeId;
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
		RecipeIngredient other = (RecipeIngredient) obj;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (amountDenominator != other.amountDenominator)
			return false;
		if (amountNumerator != other.amountNumerator)
			return false;
		if (id != other.id)
			return false;
		if (ingredientId == null) {
			if (other.ingredientId != null)
				return false;
		} else if (!ingredientId.equals(other.ingredientId))
			return false;
		if (measurement == null) {
			if (other.measurement != null)
				return false;
		} else if (!measurement.equals(other.measurement))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (recipeId != other.recipeId)
			return false;
		return true;
	}
	
	public RecipeIngredient add(RecipeIngredient other) {
		if (other.ingredientId.equals(this.ingredientId)) {
			if (!Strings.isNullOrEmpty(this.measurement) && !Strings.isNullOrEmpty(other.measurement) && !this.measurement.equals(other.measurement)) {
				Optional<Conversion> c = Conversion.conversions.stream().filter(x -> x.canConvert(this.measurement, other.measurement)).findFirst();
				if (c.isPresent()) {
					String smaller = c.get().getSmaller(this.measurement, other.measurement);
					if (this.measurement.equals(smaller)) {
						Fraction converted = c.get().convert(other.measurement, other.getAmount());
						Fraction newAmount = this.getAmount().add(converted);
						return new RecipeIngredient(this, newAmount);
					}
					else {
						Fraction converted = c.get().convert(this.measurement, this.getAmount());
						Fraction newAmount = other.getAmount().add(converted);
						return new RecipeIngredient(other, newAmount);
					}
				}
			}
			else {
				Fraction newAmount = this.getAmount().add(other.getAmount());
				return new RecipeIngredient(this, newAmount);
			}
		}
		throw new IllegalArgumentException("Cannot add ingredients that are not the same kind or measure. " + this.measurement + "<>" + other.measurement);
	}

	@Override
	public String toString() {
		return "" + getAmount().toString() + " " + measurement + " " + name;
	}
	
	
}
