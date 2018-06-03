package com.johnhite.recipe.core;

import java.util.List;

import com.google.api.client.util.Lists;

public class Recipe {

	private final int id;
	private final String source;
	private final String title;
	private final int prepTime;
	private final int servings;
	private final int nutrition;
	private final String description;
	private final String link;
	private final String ethnicity;
	private final List<IngredientAmount> ingredients;
	
	public Recipe(int id, String source, String title, int prepTime, int servings, int nutrition, String description,
			String link, String ethnicity, List<IngredientAmount> ingredients) {
		super();
		this.id = id;
		this.source = source;
		this.title = title;
		this.prepTime = prepTime;
		this.servings = servings;
		this.nutrition = nutrition;
		this.description = description;
		this.link = link;
		this.ethnicity = ethnicity;
		this.ingredients = ingredients;
	}
	public int getId() {
		return id;
	}
	public String getSource() {
		return source;
	}
	public String getTitle() {
		return title;
	}
	public int getPrepTime() {
		return prepTime;
	}
	public int getServings() {
		return servings;
	}
	public int getNutrition() {
		return nutrition;
	}
	public String getDescription() {
		return description;
	}
	public String getLink() {
		return link;
	}
	public String getEthnicity() {
		return ethnicity;
	}
	public List<IngredientAmount> getIngredients() {
		if (ingredients == null) {
			return Lists.newArrayList();
		}
		return ingredients;
	}
	
	@Override
	public String toString() {
		return "Recipe [id=" + id + ", source=" + source + ", title=" + title + ", prepTime=" + prepTime + ", servings="
				+ servings + ", nutrition=" + nutrition + ", description=" + description + ", link=" + link
				+ ", ethnicity=" + ethnicity + ", ingredients=" + ingredients + "]";
	}


	public static class Builder {
		private int id;
		private String source;
		private String title;
		private int prepTime;
		private int servings;
		private int nutrition;
		private String description;
		private String link;
		private String ethnicity;
		private List<IngredientAmount> ingredients;
		
		public Recipe build() {
			return new Recipe(id, source, title, prepTime, servings, nutrition, description, link, ethnicity, ingredients);
		}
		public Builder setId(int id) {
			this.id = id;
			return this;
		}
		public Builder setSource(String source) {
			this.source = source;
			return this;
		}
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}
		public Builder setPrepTime(int prepTime) {
			this.prepTime = prepTime;
			return this;
		}
		public Builder setServings(int servings) {
			this.servings = servings;
			return this;
		}
		public Builder setNutrition(int nutrition) {
			this.nutrition = nutrition;
			return this;
		}
		public Builder setDescription(String description) {
			this.description = description;
			return this;
		}
		public Builder setLink(String link) {
			this.link = link;
			return this;
		}
		public Builder setEthnicity(String ethnicity) {
			this.ethnicity = ethnicity;
			return this;
		}
		public Builder setIngredients(List<IngredientAmount> ingredients) {
			this.ingredients = ingredients;
			return this;
		}
		
	}
}
