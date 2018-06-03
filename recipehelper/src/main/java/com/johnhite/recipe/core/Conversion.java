package com.johnhite.recipe.core;

import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

import com.google.common.collect.Sets;

public class Conversion {
	public static Set<Conversion> conversions = Sets.newHashSet(
			new Conversion("lb","oz", new Fraction(16)),
			new Conversion("tbsp","tsp", new Fraction(3)),
			new Conversion("cup", "oz", new Fraction(8)),
			new Conversion("cup", "tbsp", new Fraction(16)),
			new Conversion("inch", "one-inch piece", new Fraction(1)),
			new Conversion("large bunch", "bunch", new Fraction(2))
		);
	
	private Set<String> measures;
	private String from;
	private String to;
	private Fraction conversion;
	private Fraction reciprocal;
	private boolean fromSmaller;
	
	public Conversion(String from, String to, Fraction conv) {
		this.from = from;
		this.to = to;
		this.measures = Sets.newHashSet(from, to);
		this.conversion = conv;
		this.reciprocal = conv.reciprocal();
		this.fromSmaller = conversion.compareTo(reciprocal) <= 0;
	}
	
	public boolean canConvert(String from, String to) {
		return measures.contains(from) && measures.contains(to);
	}
	
	public String getSmaller(String measure1, String measure2) {
		if (measure1.equals(measure2)) {
			return measure1;
		}
		if (measure1.equals(from) && fromSmaller) {
			return measure1;
		}
		if (measure1.equals(to) && !fromSmaller) {
			return measure1;
		}
		return measure2;
	}
	
	public Fraction convert(String from, Fraction amount) {
		if (from.equals(this.from)) {
			return amount.multiply(conversion);
		}
		return amount.multiply(reciprocal);
	}
}