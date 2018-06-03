package com.johnhite.recipe.core;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.fraction.Fraction;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;



public class IngredientAmount {
	private static Pattern parser = Pattern.compile("^(?<amount>[0-9\\/\\s]*)(?<measure>[a-zA-Z-\\s\\d]*)");
	private static Set<Conversion> conversions = Sets.newHashSet(
			new Conversion("lb","oz", new Fraction(16)),
			new Conversion("tbsp","tsp", new Fraction(3)),
			new Conversion("cup", "oz", new Fraction(8)),
			new Conversion("inch", "one-inch piece", new Fraction(1)),
			new Conversion("large bunch", "bunch", new Fraction(2))
		);
	private final String ingredient;
	private final String rawAmount;
	private final Fraction amount;
	private final String measure;
	
	public IngredientAmount(String ingredient, Fraction amount, String measure) {
		this.amount = amount;
		this.ingredient = ingredient;
		this.measure = measure;
		this.rawAmount = amount.toString() + measure;
	}

	public IngredientAmount(String ingredient, String amount) {
		super();
		this.ingredient = ingredient;
		this.rawAmount = amount;
		Matcher matcher = parser.matcher(amount.trim());
		if (matcher.find()) {
			String a = matcher.group("amount");
			String m = matcher.group("measure");
			m = (null != m && m.trim().startsWith("-")) ? m.trim().substring(1) : m;
			this.measure = (null != m) ? m.trim().toLowerCase() : null;
			if (null != a) {
				a = a.trim().toLowerCase();
				String[] parts = a.split(" ");
				Fraction frac = new Fraction(0);
				for (String part : parts) {
					if (part.contains("/")) {
						String[] nd = part.split("/");
						frac = frac.add(new Fraction(Integer.parseInt(nd[0]), Integer.parseInt(nd[1])));
					}
					else {
						frac = frac.add(new Fraction(Integer.parseInt(part)));
					}
				}
				this.amount = frac;
			}
			else {
				this.amount = null;
			}
			
		} else {
			this.amount = null;
			this.measure = null;
		}
		
	}
	public String getIngredient() {
		return ingredient;
	}
	public String getRawAmount() {
		return rawAmount;
	}
	
	public Fraction getAmount() {
		return amount;
	}
	public String getMeasure() {
		return measure;
	}
	public IngredientAmount add(IngredientAmount other) {
		if (other.ingredient.equals(this.ingredient)) {
			if (!Strings.isNullOrEmpty(this.measure) && !Strings.isNullOrEmpty(other.measure) && !this.measure.equals(other.measure)) {
				Optional<Conversion> c = conversions.stream().filter(x -> x.canConvert(this.measure, other.measure)).findFirst();
				if (c.isPresent()) {
					String smaller = c.get().getSmaller(this.measure, other.measure);
					if (this.measure.equals(smaller)) {
						Fraction converted = c.get().convert(other.measure, other.amount);
						Fraction newAmount = this.amount.add(converted);
						return new IngredientAmount(this.ingredient, newAmount, this.measure);
					}
					else {
						Fraction converted = c.get().convert(this.measure, this.amount);
						Fraction newAmount = other.amount.add(converted);
						return new IngredientAmount(this.ingredient, newAmount, other.measure);
					}
				}
			}
			else {
				Fraction newAmount = this.amount.add(other.amount);
				return new IngredientAmount(this.ingredient, newAmount, this.measure);
			}
		}
		throw new IllegalArgumentException("Cannot add ingredients that are not the same kind or measure.");
	}
	
	@Override
	public String toString() {
		return rawAmount + " " + ingredient + "[amount: " + amount + ", measure: " + measure + "]";
	}
	
	public static void main(String...strings) {
		System.out.println(new IngredientAmount("Garlic Cloves", "3").add(new IngredientAmount("Garlic Cloves", "2")));
		Conversion lb = new Conversion("lb", "oz", new Fraction(16));
		Conversion oz = new Conversion("oz", "lb", new Fraction(1,16));
		
		System.out.println(lb.canConvert("lb", "oz"));
		System.out.println(lb.canConvert("oz", "lb"));
		System.out.println(oz.canConvert("lb", "oz"));
		System.out.println(oz.canConvert("oz", "lb"));
		System.out.println(lb.getSmaller("lb", "oz"));
		System.out.println(lb.getSmaller("oz", "lb"));
		System.out.println(oz.getSmaller("lb", "oz"));
		System.out.println(oz.getSmaller("oz", "lb"));
		System.out.println(lb.convert("oz", new Fraction(24)));
		System.out.println(oz.convert("oz", new Fraction(24)));
		System.out.println(lb.convert("lb", new Fraction(1,4)));
		System.out.println(oz.convert("lb", new Fraction(1,4)));
	}
}
