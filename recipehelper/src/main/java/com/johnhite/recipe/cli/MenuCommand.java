package com.johnhite.recipe.cli;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.johnhite.recipe.RecipeHelperConfiguration;
import com.johnhite.recipe.db.IngredientDAO;
import com.johnhite.recipe.db.RecipeDAO;
import com.johnhite.recipe.db.RecipeIngredientDAO;
import com.johnhite.recipe.db.entity.Ingredient;
import com.johnhite.recipe.db.entity.RecipeEntity;
import com.johnhite.recipe.db.entity.RecipeIngredient;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import jersey.repackaged.com.google.common.collect.Lists;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class MenuCommand extends ConfiguredCommand<RecipeHelperConfiguration> {
	private HibernateBundle<RecipeHelperConfiguration> hibernate;
	
	public MenuCommand(HibernateBundle<RecipeHelperConfiguration> hibernate) {
		super("menu", "generates a menu");
		this.hibernate = hibernate;
	}

	@Override
	public void configure(Subparser subparser) {
		super.configure(subparser);
	}

	@Override
	public void run(Bootstrap<RecipeHelperConfiguration> bootstrap, Namespace namespace, RecipeHelperConfiguration config) throws Exception {
		final Environment environment = new Environment(bootstrap.getApplication().getName(),
                bootstrap.getObjectMapper(),
                bootstrap.getValidatorFactory().getValidator(),
                bootstrap.getMetricRegistry(),
                bootstrap.getClassLoader(),
                bootstrap.getHealthCheckRegistry());
		bootstrap.run(config, environment);
		
		int numberRecipes = 4;
		final Map<Integer, RecipeEntity> recipes = getRecipes();
		final Map<Integer, List<RecipeIngredient>> recipeIngredients = getRecipeIngredients();
		final Map<Integer, Set<Integer>> recipesByIngredient = mapRecipesByIngredient(recipeIngredients);
		final Map<String, Ingredient> ingredientMap = getIngredientMap();
		final Set<Integer> recipeBlacklist = Sets.newHashSet(15, 24, 67, 43, 38);
		final Set<Integer> ingredientBlacklist = Sets.newHashSet();
		final List<Item> requestedItems = Lists.newArrayList(
				new Item(2, "rainbow radish", findIngredientsLike("rainbow%radish")),
				new Item(1, "strawberry", findIngredientsLike("strawberry")),
				new Item(1, "broccoli rabe", findIngredientsLike("broc%rab%")),
				new Item(1, "swiss chard", findIngredientsLike("%chard%")),
				new Item(2, "kale", findIngredientsLike("%kale%")),
				new Item(1, "escarole", findIngredientsLike("escarole"))
				//new AtLeastItem(1, "scallion", findIngredientsLike("scallion"))
				//new AtLeastItem(3, "onion", findIngredientsLike("%onion"))
		);
		
		List<RecipeEntity> menu = Lists.newArrayList();
		
		//use selected ingredients
		final Map<Item, Set<Integer>> requestedItemRecipes = Maps.newHashMap();
		final Set<Integer> requestedRecipeSet = Sets.newHashSet();
		for(Item i : requestedItems) {
			Set<Integer> itemRecipes = Sets.newHashSet();
			for (int id : i.ingredients) {
				itemRecipes.addAll(recipesByIngredient.get(id));
			}
			itemRecipes.removeAll(recipeBlacklist); //remove recipes we don't want to use
			requestedRecipeSet.addAll(itemRecipes);
			requestedItemRecipes.put(i, itemRecipes);
		}
		
		//Dynamic Programming subset sum problem
		//setup: get all recipes and their weights
		final Map<Integer, Integer> requestedRecipeIndex = Maps.newHashMap();
		final Map<Integer, Integer> requestedRecipeReverseIndex = Maps.newHashMap();
		int rind = 0;
		for (Integer recipeInd: requestedRecipeSet) {
			requestedRecipeIndex.put(rind, recipeInd);
			requestedRecipeReverseIndex.put(recipeInd, rind);
			rind++;
		}
		
		final Map<Integer, Weights> recipeWeights = Maps.newHashMap();
		final List<Item> foundItems = requestedItemRecipes.keySet().stream()
				.filter(item -> !requestedItemRecipes.get(item).isEmpty())
				.sorted()
				.collect(Collectors.toList());
		final Weights targetWeights = new Weights(new int[foundItems.size()]);
		
		//init recipe weights
		for (int i= 0; i < foundItems.size(); i++) {
			targetWeights.weights[i] = foundItems.get(i).count;
			 Set<Integer> rs = requestedItemRecipes.get(foundItems.get(i));
			 for (Integer r: rs ) {
				 Weights w = recipeWeights.get(r);
				 if (w == null) {
					 w = new Weights(new int[foundItems.size()]);
					 recipeWeights.put(r, w);
				 }
				 w.weights[i] += 1; //should add actual count of items in recipe, but I'll do that later
			 }
		}
		
		final List<Integer> sortedRequestedRecipes = requestedRecipeSet.stream().sorted((a, b) -> recipeWeights.get(a).compareTo(recipeWeights.get(b))).collect(Collectors.toList());
		final List<Weights> capacities = generateCapacities(targetWeights);
		final Map<Weights, Integer> capacityIndices = Maps.newHashMap();
		for (int i=0; i < capacities.size(); i++) {
			capacityIndices.put(capacities.get(i), i);
		}
		
		Weights[][] weightTable = new Weights[capacities.size()][sortedRequestedRecipes.size()];
		ks2(weightTable, capacities, capacityIndices, sortedRequestedRecipes, recipeWeights, targetWeights.weights.length);
		List<List<Integer>> solutions = ksTrace(weightTable, capacities, capacityIndices, sortedRequestedRecipes, recipeWeights, targetWeights.weights.length);
		//ks(sortedRequestedRecipes.size(), targetWeights, weightTable, capacityIndices, sortedRequestedRecipes, recipeWeights, targetWeights.weights.length);
		
		System.out.println(Arrays.toString(foundItems.toArray()));
		System.out.println("target: " + targetWeights);
		//Column Headers
		System.out.print("        \t");
		for (int i = 0; i < sortedRequestedRecipes.size(); i++) {
			System.out.print(recipeWeights.get(sortedRequestedRecipes.get(i)));
			System.out.print("\t");
		}
		System.out.println();
		//Table with row headers
		for (int i =0; i < capacities.size(); i++) {
			System.out.print(capacities.get(i));
			System.out.print("\t");
			for (int j = 0; j < sortedRequestedRecipes.size(); j++) {
				System.out.print(weightTable[i][j]);	
				System.out.print("\t");
			}
			System.out.println();
		}
		
		requestedItemRecipes.forEach( (i, itemRecipes) -> {
			List<Integer> rlist = Lists.newArrayList(itemRecipes);
			if (rlist.size() < i.count) {
				System.out.println("could not use " + (i.count - rlist.size()) + " " + i.name);
			}
			/*for (int j = 0; j < i.count; j++) {
				if (rlist.isEmpty()) {
					break;
				}
				int index = (int)(Math.random() * rlist.size());
				menu.add(recipes.get(rlist.get(index)));
				rlist.remove(index);
			}*/
		});
		
		List<Integer> seed = solutions.stream().sorted( (a,b) -> a.size() < b.size() ? 1 : -1).findFirst().get();
		for (Integer recipeId : seed) {
			menu.add(recipes.get(recipeId));
		}
		
		final List<RecipeIngredient> psuedoRecipe = Lists.newArrayList();
		menu.stream().forEach(r -> {
			psuedoRecipe.addAll(recipeIngredients.get(r.getId()));
		});
		
		List<Integer> sorted = sort(psuedoRecipe, recipeIngredients);
		sorted.removeAll(recipeBlacklist); //more blacklisting
		for (RecipeEntity e : menu) {
			sorted.removeAll(Lists.newArrayList(e.getId()));
		}
		
		int needed = numberRecipes - menu.size();
		for (int i=0; i < needed; i++) {
			menu.add(recipes.get(sorted.get(i)));
		}

		Map<Integer, RecipeIngredient> shoppingList = Maps.newHashMap();
		
		for (RecipeEntity e : menu) {
			System.out.println("" + e.getId() + " " + e.getTitle() + ", " + e.getPrepTime() +" minutes, "+ e.getLink());
			for (RecipeIngredient i : recipeIngredients.get(e.getId())) {
				RecipeIngredient listItem = shoppingList.get(i.getIngredientId());
				if (listItem == null) {
					listItem = i;
					shoppingList.put(i.getIngredientId(), i);
				} else {
					shoppingList.put(listItem.getIngredientId(), listItem.add(i));
				}
			}
		}
		
		//todo subtract out ingredients we have
		/*
		for (Item item : requestedItems) {
			Fraction amt = new Fraction(-item.count, 1);
			for (int id : item.ingredients) {
				RecipeIngredient ing = shoppingList.get(id);
				if (ing != null) {
					
				}
			}
		}
		*/
		System.out.println("Shopping list");
		shoppingList.values().stream().forEach( e -> System.out.println(e));
	}
	
	private void ks2(Weights[][] weightTable, List<Weights> capacities, Map<Weights, Integer> capacityIndices, List<Integer> sortedRequestedRecipes, Map<Integer, Weights> recipeWeights, int weightLength) {
		Weights zero = new Weights(new int[weightLength]);
		for (int i = 0; i < capacities.size(); i++) {
			Weights c = capacities.get(i);
			for (int j = 0; j < sortedRequestedRecipes.size(); j++) {
				Weights r = recipeWeights.get(sortedRequestedRecipes.get(j));
				Weights diff = c.subtract(r);
				if (diff.hasNegativeComponent()) {
					//can't take Recipe
					weightTable[i][j] = (j < 1) ? zero : weightTable[i][j-1];
				} 
				else{
					Weights a = (j < 1) ? zero : weightTable[i][j-1];
					Weights b = (j < 1) ? r : r.add(weightTable[capacityIndices.get(diff)][j-1]);
					Weights max = Weights.max(a, b);
					weightTable[i][j] = max;
				}
			}
		}
	}
	
	private List<List<Integer>> ksTrace(Weights[][] weightTable, List<Weights> capacities, Map<Weights, Integer> capacityIndices, List<Integer> sortedRequestedRecipes, Map<Integer, Weights> recipeWeights, int weightLength) {
		Set<Integer> solutionJ = Sets.newHashSet();
		Weights target = capacities.get(capacities.size()-1);
		for (int j =0; j < weightTable[capacities.size()-1].length; j++) {
			if (target.subtract(weightTable[capacities.size()-1][j]).isZero()) {
				solutionJ.add(j);
			}
		}
		
		List<List<Integer>> solutions = Lists.newArrayList();
		
		for (Integer maxJ : solutionJ) {
			int currenti = capacities.size()-1;
			Weights currentTarget = target;
			List<Integer> solution = Lists.newArrayList();
			for (int j = maxJ; j >= 0; j--) {
				if ( j > 0 && weightTable[currenti][j].equals(weightTable[currenti][j-1]) && recipeWeights.get(sortedRequestedRecipes.get(j)).compareTo(weightTable[currenti][j]) > 0 ) {
					//if equal, current item did not contribute
				}
				else {
					//current item did contribute. add it to the solution
					solution.add(sortedRequestedRecipes.get(j));
					//then find remaining capacity and move i and j
					Weights r = recipeWeights.get(sortedRequestedRecipes.get(j));
					currentTarget = currentTarget.subtract(r);
					currenti = capacityIndices.get(currentTarget);
				}
			}
			solutions.add(solution);
		}
		
		return solutions;
	}
	
	private Weights ks(int n, Weights c, Weights[][] weightTable, Map<Weights, Integer> capacityIndices, List<Integer> sortedRequestedRecipes, Map<Integer, Weights> recipeWeights, int weightLength) {
			
		if (weightTable[capacityIndices.get(c)][n] != null) {
			return weightTable[capacityIndices.get(c)][n];
		}
		
		if (n == 0 || c.isZero() /*need objective function method */) {
			Weights zero = new Weights(new int[weightLength]);
			weightTable[capacityIndices.get(c)][n] = zero;
			return zero;
		}
		
		Weights weightN = recipeWeights.get(sortedRequestedRecipes.get(n-1));
		if (c.subtract(weightN).hasNegativeComponent()) {
			Weights next = ks(n-1, c, weightTable, capacityIndices, sortedRequestedRecipes, recipeWeights, weightLength);
			weightTable[capacityIndices.get(c)][n] = next;
			return next;
		}
		
		Weights a = ks(n-1, c, weightTable, capacityIndices, sortedRequestedRecipes, recipeWeights, weightLength);
		Weights b = weightN.add( ks(n-1, c.subtract(weightN), weightTable, capacityIndices, sortedRequestedRecipes, recipeWeights, weightLength) );
		Weights max = Weights.max(a, b);
		weightTable[capacityIndices.get(c)][n] = max;
		return max;
	}
	
	
	private List<Weights> generateCapacities(final Weights target) {
		List<Weights> weights = Lists.newArrayList();
		Weights start = new Weights(new int[target.weights.length]);
		//weights.add(start);
		iterate(start, target, 0, weights);
		return weights;
	}
	
	private void iterate(Weights start, Weights target, int place, List<Weights> result) {
		if (place == target.weights.length) {
			return;
		}

		for (int i=0; i < target.weights[place]+1; i++) {
			Weights next = new Weights(start);
			next.weights[place] = i;
			iterate(next, target, place+1, result);
			if (place == target.weights.length-1) {
				result.add(next);
			}
		}
	}
	
	private List<Integer> sort(final List<RecipeIngredient> base, final Map<Integer, List<RecipeIngredient>> recipeIngredients) {
		List<Map.Entry<Integer, List<RecipeIngredient>>> table = Lists.newArrayList(recipeIngredients.entrySet());
		table.sort((a, b) -> {
			int dista = recipeDistance(base, a.getValue());
			int distb = recipeDistance(base, b.getValue());
			if (dista > distb) {
				return 1;
			}
			if (dista < distb) {
				return -1;
			}
			return 0;
		});
		
		return table.stream().map(e -> e.getKey()).collect(Collectors.toList());
	}
	
	private Set<Integer> findIngredientsLike(String likeExp) {
		SessionFactory sessionFactory = hibernate.getSessionFactory();
		Session session = sessionFactory.openSession();
		ManagedSessionContext.bind(session);
		IngredientDAO idao = new IngredientDAO(sessionFactory);
		List<Ingredient> match = idao.findProduceLike(likeExp);
		sessionFactory.getCurrentSession().close();
		return match.stream().map(Ingredient::getId).collect(Collectors.toSet());
	}
	
	private Set<Integer> findIngredientsLike(String likeExp, String notLikeExp) {
		final Set<Integer> like = findIngredientsLike(likeExp);
		final Set<Integer> notLike = findIngredientsLike(notLikeExp);
		return like.stream().filter(i -> notLike.contains(i)).collect(Collectors.toSet());
	}
	
	private Map<Integer, Set<Integer>> mapRecipesByIngredient(Map<Integer, List<RecipeIngredient>> recipeIngredients) {
		Map<Integer, Set<Integer>> result = Maps.newHashMap();
		recipeIngredients.forEach((recipeId, ingredientList) -> {
			ingredientList.forEach(ingredient -> {
				if (ingredient.getIngredientId() > 0) {
					Set<Integer> recipes = result.get(ingredient.getIngredientId());
					if (recipes == null) {
						recipes = Sets.newHashSet();
						result.put(ingredient.getIngredientId(), recipes);
					}
					recipes.add(recipeId);
				}
			});
		});
		return result;
	}
	
	private Map<Integer, RecipeEntity> getRecipes() {
		SessionFactory sessionFactory = hibernate.getSessionFactory();
		Session session = sessionFactory.openSession();
		ManagedSessionContext.bind(session);
		RecipeDAO idao = new RecipeDAO(sessionFactory);
		Map<Integer, RecipeEntity> result = idao.list().stream().collect(Collectors.toMap(RecipeEntity::getId, Function.identity()));
		sessionFactory.getCurrentSession().close();
		return result;
	}
	
	private Map<Integer, List<RecipeIngredient>> getRecipeIngredients() {
		SessionFactory sessionFactory = hibernate.getSessionFactory();
		Session session = sessionFactory.openSession();
		ManagedSessionContext.bind(session);
		RecipeIngredientDAO idao = new RecipeIngredientDAO(sessionFactory);
		List<RecipeIngredient> all = idao.list();
		Map<Integer, List<RecipeIngredient>> result = all.stream().collect(Collectors.groupingBy(r -> r.getRecipeId()));
		sessionFactory.getCurrentSession().close();
		return result;
	}
	
	private Map<String, Ingredient> getIngredientMap() {
		SessionFactory sessionFactory = hibernate.getSessionFactory();
		Session session = sessionFactory.openSession();
		ManagedSessionContext.bind(session);
		IngredientDAO idao = new IngredientDAO(sessionFactory);
		List<Ingredient> ing = idao.list();
		Map<String, Ingredient> result = Maps.newHashMap();
		for (Ingredient i : ing) {
			result.put(i.getName(), i);
		}
		sessionFactory.getCurrentSession().close();
		return result;
	}
	
	private static int recipeDistance(Collection<RecipeIngredient> a, Collection<RecipeIngredient> b) {
		Set<Integer> ia = a.stream().map(i -> {
			int id = i.getIngredientId();
			if (id == 0) {
				id = (int)(Math.random() * 10000) + 10000;
			}
			return id;
		}).collect(Collectors.toSet());
		
		Set<Integer> ib = b.stream().map(i -> {
			int id = i.getIngredientId();
			if (id == 0) {
				id = (int)(Math.random() * 10000) + 10000;
			}
			return id;
		}).collect(Collectors.toSet());
		
		return Sets.difference(ia, ib).size();
	}
	
	public static class AtLeastItem extends Item {

		public AtLeastItem(int count, String name, Set<Integer> ingredients) {
			super(count, name, ingredients);
			this.atMost = false;
		}
		
	}
	public static class Item implements Comparable<Item>{
		public Set<Integer> ingredients;
		public String name;
		public int count = 1;
		public boolean atMost = true;
		public Item(int count, String name, Set<Integer> ingredients) {
			this.count =count;
			this.name = name;
			this.ingredients = ingredients;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			Item other = (Item) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return name;
		}
		
		@Override
		public int compareTo(Item o) {
			//atMost is larger
			if (this.atMost && !o.atMost) {
				return -1;
			}
			if (o.atMost && !this.atMost) {
				return 1;
			}
			if (this.count == o.count) {
				return 0;
			}
			if (this.count > o.count) {
				return -1;
			}
			return 1;
		}
	}
	
	private static class Weights implements Comparable<Weights> {
		public int[] weights;
		
		public Weights(int[] weights) {
			this.weights = weights;
		}
		
		public Weights(Weights other) {
			this.weights = new int[other.weights.length];
			System.arraycopy(other.weights, 0, weights, 0, weights.length);
		}
		
		public Weights add(Weights other) {
			Weights sum = new Weights(other);
			for (int i=0; i < weights.length; i++) {
				sum.weights[i] += weights[i];
			}
			return sum;
		}
		
		public Weights subtract(Weights other) {
			Weights diff = new Weights(this);
			for (int i=0; i < weights.length; i++) {
				diff.weights[i] -= other.weights[i];
			}
			return diff;
		}
		
		public static Weights max(Weights a, Weights b) {
			for (int i =0; i < a.weights.length; i++) {
				if (a.weights[i] != b.weights[i]) {
					if (a.weights[i] > b.weights [i]) {
						return a;
					}
					return b;
				}
			}
			return a;
		}
		
		public boolean isZero() {
			for (int i = 0; i < weights.length; i++) {
				if (weights[i] > 0) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			return "[" + Arrays.toString(weights) + "]";
		}

		@Override
		public int compareTo(Weights other) {
			//assume weights are in order of importance
			for (int i=0; i < weights.length; i++) {
				if (weights[i] == other.weights[i]) {
					continue;
				}
				if (weights[i] > other.weights[i]) {
					return 1;
				}
				return -1;
			}
			return 0;
		}
		
		public boolean hasNegativeComponent() {
			for (int i = 0; i < weights.length; i++) {
				if (weights[i] < 0) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(weights);
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
			Weights other = (Weights) obj;
			if (!Arrays.equals(weights, other.weights))
				return false;
			return true;
		}
		
	}
}
