package com.johnhite.recipe.db.entity;

import com.google.common.collect.ImmutableList;

public class Entities {

	public static final ImmutableList<Class<?>> CLASSES = ImmutableList.<Class<?>>builder().add(
			CategoryAvailability.class,
			CategorySeasonality.class,
			Ingredient.class,
			IngredientAvailability.class,
			IngredientSeasonality.class,
			RecipeEntity.class,
			RecipeIngredient.class,
			RecipeStep.class
	).build();
}
