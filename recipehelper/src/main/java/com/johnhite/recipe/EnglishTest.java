package com.johnhite.recipe;

import org.atteo.evo.inflector.English;
import org.jboss.dna.Inflector;

public class EnglishTest {

	public static void main(String...strings) {
		Inflector inflector = Inflector.getInstance();
		System.out.println(inflector.singularize("cats"));
		System.out.println(inflector.singularize("cat"));
		System.out.println(inflector.singularize("radishes"));
		System.out.println(inflector.singularize("radish"));
		System.out.println(inflector.singularize("tomatoes"));
		System.out.println(inflector.singularize("tomato"));
		System.out.println(inflector.singularize("raspberries"));
		System.out.println(inflector.singularize("raspberry"));
		
	}
}
