package com.johnhite.recipe.db.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "recipe_index")
@Entity
public class RecipeEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "source")
	private String source;
	
	@Column(name="title")
	private String title;
	
	@Column(name = "prep_time")
	private int prepTime;
	
	@Column(name = "servings")
	private int servings;
	@Column(name="calories")
	private int calories;
	@Column(name="description")
	private String description;
	@Column(name="link")
	private String link;
	@Column(name="ethnicity")
	private String ethnicity;
	
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
	public int getCalories() {
		return calories;
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
	public void setId(int id) {
		this.id = id;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setPrepTime(int prepTime) {
		this.prepTime = prepTime;
	}
	public void setServings(int servings) {
		this.servings = servings;
	}
	public void setCalories(int calories) {
		this.calories = calories;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public void setEthnicity(String ethnicity) {
		this.ethnicity = ethnicity;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + calories;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((ethnicity == null) ? 0 : ethnicity.hashCode());
		result = prime * result + id;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + prepTime;
		result = prime * result + servings;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		RecipeEntity other = (RecipeEntity) obj;
		if (calories != other.calories)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (ethnicity == null) {
			if (other.ethnicity != null)
				return false;
		} else if (!ethnicity.equals(other.ethnicity))
			return false;
		if (id != other.id)
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (prepTime != other.prepTime)
			return false;
		if (servings != other.servings)
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "RecipeEntity [id=" + id + ", source=" + source + ", title=" + title + ", prepTime=" + prepTime
				+ ", servings=" + servings + ", calories=" + calories + ", description=" + description + ", link="
				+ link + ", ethnicity=" + ethnicity + "]";
	}
	
	
}
