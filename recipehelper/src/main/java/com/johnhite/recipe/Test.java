package com.johnhite.recipe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.Maps;
import com.johnhite.recipe.core.IngredientAmount;
import com.johnhite.recipe.core.Recipe;

import jersey.repackaged.com.google.common.collect.Lists;

public class Test {

	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	
	public static void main(String... args) throws Exception {
		 // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1VqCYQgZCriuCh_3MQUYI1vRMmw7CLHuAB7Nf21jWIBI";
        final String range = "Sheet1!A2:A20";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential2(HTTP_TRANSPORT))
                .setApplicationName("Recipe Client")
                .build();
       /*ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            System.out.println("Name, Major");
            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                System.out.printf("%s\n", row.get(0));
            }
        }*/
        Map<Integer, List<IngredientAmount>> ingredientMap = getIngredientAmounts(service, spreadsheetId);
       /* final Set<String> allIngredients =  ingredientMap.values().stream()
        	.reduce(new ArrayList<IngredientAmount>(), (a, b) -> {a.addAll(b); return a;})
        	.stream().map(ia -> ia.getIngredient())
        	.collect(Collectors.toSet());
        
        System.out.println("==== All Ingredients ====");
        for (String i : allIngredients) {
        	System.out.println(i);
        }*/
        final Map<String, List<Integer>> ingredientLookup = sortRecipiesByIngredient(ingredientMap);
        final Map<Integer, Recipe> recipes = getRecipes(service, spreadsheetId, ingredientMap);
        final List<PantryItem> pantry = Lists.newArrayList(
        		new PantryItem("beefsteak tomato","beefsteak tomatos", "tomato", "tomatoes"),
        		new PantryItem("multicolored cherry tomatoes","orange cherry tomatoes", "cherry tomatoes"),
        		new PantryItem("yellow squash","summer squash", "yellow summer squash")
        );
        final List<Recipe> menu = createMenu(pantry, recipes, ingredientLookup);
        final Map<String, IngredientAmount> shoppingList = Maps.newHashMap();
        System.out.println("==== Menu ====");
        for (Recipe r: menu) {
        	System.out.println(r);
        	for (IngredientAmount a : r.getIngredients()) {
        		if (!shoppingList.containsKey(a.getIngredient())) {
        			shoppingList.put(a.getIngredient(), a);
        		} 
        		else {
        			IngredientAmount o = shoppingList.get(a.getIngredient());
        			o = o.add(a);
        			shoppingList.put(a.getIngredient(), o);
        		}
        	}
        }
        System.out.println("==== Shopping List ====");
        for (IngredientAmount ia : shoppingList.values()) {
        	System.out.println(ia);
        }
        
	}
	public static class PantryItem {
		public final String[] names;
		public PantryItem(String... names) {
			super();
			this.names = names;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(names);
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
			PantryItem other = (PantryItem) obj;
			if (!Arrays.equals(names, other.names))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return names[0];
		}
	}
	private static List<Recipe> createMenu(List<PantryItem> pantry, Map<Integer, Recipe> recipes, Map<String, List<Integer>> ingredientLookup) {
		final List<Recipe> menu = Lists.newArrayList();
		Map<PantryItem, Recipe> containsPantry = Maps.newHashMap();
		for (PantryItem pi : pantry) {
			for (String ing : pi.names) {
				List<Integer> pr = ingredientLookup.get(ing);
				if (pr != null) {
					for (int i: pr) {
						containsPantry.put(pi, recipes.get(i));
					}
				}
			}
			if (containsPantry.get(pi) == null) {
				System.out.println("Could not use pantry item " + pi);
			}
		}
		menu.addAll(containsPantry.values());
		int needed = 7 - menu.size();
		for (int i=0; i < needed; i++) {
        	int index = (int)Math.floor(Math.random() * (recipes.size() -1));
        	Recipe r = recipes.get(index);
        	menu.add(r);
        }
		return menu;
	}
	
	private static Map<String, List<Integer>> sortRecipiesByIngredient(Map<Integer, List<IngredientAmount>> ingredients) {
		final Map<String, List<Integer>> result = Maps.newHashMap();
		ingredients.forEach((recipe, ing) -> {
			ing.forEach(ia -> {
				List<Integer> rl = result.get(ia.getIngredient());
				if (rl == null) {
					rl = Lists.newArrayList();
					result.put(ia.getIngredient(), rl);
				}
				rl.add(recipe);
			});
		});
		return result;
	}
	
	private static Map<Integer, Recipe> getRecipes(Sheets service, String spreadsheetId, Map<Integer, List<IngredientAmount>> ingredientMap) throws IOException {
		Map<Integer, Recipe> recipies = Maps.newHashMap();
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
				recipies.put(rr.getId(),  rr);
			}
			
		}
		return recipies;
	}
	
	private static Map<Integer, List<IngredientAmount>> getIngredientAmounts(Sheets service, String spreadsheetId) throws IOException {
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

	private static Credential getCredentials(final NetHttpTransport transport) throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream("client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        		transport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(".")))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }
	
	private static Credential getCredential2(final NetHttpTransport transport) throws FileNotFoundException, IOException {
		GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("service_credential.json"))
			    .createScoped(SCOPES);
		return credential;
	}
}
