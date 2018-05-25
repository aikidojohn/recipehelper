package com.johnhite.recipe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.johnhite.recipe.core.Recipe;

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
        
        getRecipes(service, spreadsheetId);
	}
	
	private static Map<Integer, Recipe> getRecipes(Sheets service, String spreadsheetId) throws IOException {
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
					break;
				}
				
				r.setId(Integer.parseInt((String)row.get(0)));
				r.setSource((String)row.get(1));
				r.setTitle((String)row.get(2));
				r.setPrepTime(Integer.parseInt((String)row.get(3)));
				r.setServings(Integer.parseInt((String)row.get(4)));
				r.setNutrition(Integer.parseInt((String)row.get(5)));
				r.setDescription((String)row.get(6));
				r.setLink((String)row.get(7));
				r.setEthnicity((String)row.get(8));
				
				final Recipe rr = r.build();
				//System.out.println(rr.getTitle());
				recipies.put(rr.getId(),  rr);
			}
			
		}
		return recipies;
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
