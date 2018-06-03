package com.johnhite.recipe.cli;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.jboss.dna.Inflector;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.johnhite.recipe.RecipeHelperConfiguration;
import com.johnhite.recipe.core.IngredientAmount;
import com.johnhite.recipe.core.Recipe;
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
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class ImportCommand extends ConfiguredCommand<RecipeHelperConfiguration> {
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private Sheets service;
	private String spreadsheetId = "1VqCYQgZCriuCh_3MQUYI1vRMmw7CLHuAB7Nf21jWIBI";
	private Inflector inflector = Inflector.getInstance();
	
	private HibernateBundle<RecipeHelperConfiguration> hibernate;
	
	public ImportCommand(HibernateBundle<RecipeHelperConfiguration> hibernate) {
		super("import", "Imports from google doc");
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
		
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential(HTTP_TRANSPORT))
                .setApplicationName("Recipe Client")
                .build();
        
        
        //importIngredients();
        importRecipes();
	}
	
	private void importIngredients() throws IOException{
		SessionFactory sessionFactory = hibernate.getSessionFactory();
		Session session = sessionFactory.openSession();
		ManagedSessionContext.bind(session);
		IngredientDAO idao = new IngredientDAO(sessionFactory);
		List<Ingredient> ing = loadIngredients();
		Transaction tx= session.beginTransaction();
		for (Ingredient i : ing) {
			Optional<Ingredient> existing = idao.getByNameExact(i.getName());
			if (existing.isPresent()) {
				continue;
			}
			idao.persist(i);
		}
		sessionFactory.getCurrentSession().flush();
		tx.commit();
		sessionFactory.getCurrentSession().close();
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
	
	private void importRecipes() throws IOException{
		final List<Recipe> recipes = loadRecipes();
		final Map<String, Ingredient> ingredientMap = getIngredientMap();
		
		SessionFactory sessionFactory = hibernate.getSessionFactory();
		Session session = sessionFactory.openSession();
		ManagedSessionContext.bind(session);
		IngredientDAO idao = new IngredientDAO(sessionFactory);
		RecipeDAO rdao = new RecipeDAO(sessionFactory);
		RecipeIngredientDAO ridao = new RecipeIngredientDAO(sessionFactory);
		
		for (Recipe r : recipes) {
			if (r.getIngredients().isEmpty()) {
				continue;
			}
			Transaction tx= session.beginTransaction();
			RecipeEntity e = new RecipeEntity();
			e.setSource(r.getSource().toUpperCase());
			e.setTitle(r.getTitle());
			e.setPrepTime(r.getPrepTime());
			e.setServings(r.getServings());
			e.setCalories(r.getNutrition());
			e.setDescription(r.getDescription());
			e.setLink(r.getLink());
			e.setEthnicity(r.getEthnicity());
			e = rdao.persist(e);
			for (IngredientAmount i : r.getIngredients()) {
				RecipeIngredient ri = new RecipeIngredient();
				ri.setRecipeId(e.getId());
				ri.setAmount(i.getAmount());
				ri.setMeasurement(i.getMeasure());
				ri.setName(i.getIngredient());
				
				final String canonicalName = canonicalize(i.getIngredient());
				Ingredient ing = ingredientMap.get(canonicalName);
				if (ing == null) {
					ing = new Ingredient();
					ing.setName(canonicalName);
					if (!ing.getName().contains("spice blend") && !ing.getName().contains("seasoning")) {
						ing = idao.persist(ing);
						ingredientMap.put(ing.getName(), ing);
					}
				}
				if (ing != null) {
					ri.setIngredientId(ing.getId());
				}
				ridao.persist(ri);
			}
			sessionFactory.getCurrentSession().flush();
			tx.commit();
		}
		sessionFactory.getCurrentSession().close();
	}
	
	private Credential getCredential(final NetHttpTransport transport) throws FileNotFoundException, IOException {
		GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("service_credential.json"))
			    .createScoped(SCOPES);
		return credential;
	}
	
	private List<Ingredient> loadIngredients() throws IOException {
		List<Ingredient> ingredients = Lists.newArrayList();
		for (int start =2;; start += 51)  {
			String range = "'Ingredient Categories'!A" + start + ":C" + (start + 50);
			//System.out.println("fetching range: " + range);
			ValueRange response = service.spreadsheets().values()
	                .get(spreadsheetId, range)
	                .execute();
			List<List<Object>> values = response.getValues();
			if (values == null || values.isEmpty()) {
				break;
			}
			for (List row : values) {
				Ingredient i = new Ingredient();
				i.setName( canonicalize((String)row.get(0)) );
				if (i.getName().contains("spice blend") || i.getName().contains("seasoning")) {
					continue;
				}
				if (row.size() > 1) {
					i.setType(canonicalize((String)row.get(1)));
				}
				if (row.size() > 2) {
					i.setCategory(canonicalize((String)row.get(2)));
				}
				ingredients.add(i);
			}
		}
		return ingredients;
	}
	
	private String canonicalize(String ingredient) {
		String[] parts = ingredient.toLowerCase().split(" ");
		parts[parts.length-1] = inflector.singularize(parts[parts.length-1]);
		return Joiner.on(" ").skipNulls().join(parts);
	}
	
	private List<Recipe> loadRecipes() throws IOException {
		Map<Integer, List<IngredientAmount>> ingredientMap = loadIngredientAmounts();
		List<Recipe> recipies = Lists.newArrayList();
		for (int start = 2;; start += 51) {
			String range = "'Recipe Index'!A" + start + ":I" + (start + 50);
			//System.out.println("fetching range: " + range);
			ValueRange response = service.spreadsheets().values()
	                .get(spreadsheetId, range)
	                .execute();
			List<List<Object>> values = response.getValues();
			if (values == null || values.isEmpty()) {
				break;
			}
			
			Recipe.Builder r = new Recipe.Builder();
			for (List row : values) {
				if (row.isEmpty() || row.size() < 9) {
					continue;
				}
				
				int id = Integer.parseInt((String)row.get(0));
				r.setId(id);
				r.setSource((String)row.get(1));
				r.setTitle((String)row.get(2));
				r.setPrepTime(Integer.parseInt((String)row.get(3)));
				r.setServings(Integer.parseInt((String)row.get(4)));
				r.setNutrition(Integer.parseInt((String)row.get(5)));
				r.setDescription((String)row.get(6));
				r.setLink((String)row.get(7));
				r.setEthnicity((String)row.get(8));
				r.setIngredients(ingredientMap.get(id));
				
				final Recipe rr = r.build();
				//System.out.println(rr.getTitle());
				recipies.add(rr);
			}
			
		}
		return recipies;
	}
	
	private Map<Integer, List<IngredientAmount>> loadIngredientAmounts() throws IOException {
		Map<Integer, List<IngredientAmount>> ingredients = Maps.newHashMap();
		
		List<IngredientAmount> i = Lists.newArrayList();
		String currentRow = "1";
		for (int start = 2;; start += 51) {
			String range = "'Recipe Ingredients'!A" + start + ":C" + (start + 50);
			System.out.println("fetching range: " + range);
			ValueRange response = service.spreadsheets().values()
	                .get(spreadsheetId, range)
	                .execute();
			List<List<Object>> values = response.getValues();
			if (values == null || values.isEmpty()) {
				break;
			}
			
			for (List row : values) {
				if (row.isEmpty() || row.size() < 3) {
					continue;
				}
				String id = (String)row.get(0);
				String amount = (String)row.get(1);
				String ingredient = ((String)row.get(2)).toLowerCase();
				IngredientAmount ia = new IngredientAmount(ingredient, amount);
				if (!currentRow.equals(id)) {
					ingredients.put(Integer.parseInt(currentRow), i);
					i = Lists.newArrayList();
					currentRow = id;
				}
				i.add(ia);
				//System.out.println(ia.getIngredient());
			}
			
		}
		
		return ingredients;
	}
	
}
	
