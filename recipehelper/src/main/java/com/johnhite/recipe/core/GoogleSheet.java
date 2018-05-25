package com.johnhite.recipe.core;

import com.google.api.services.sheets.v4.Sheets;

public class GoogleSheet {

	private Sheets service;
	private String spreadsheetId;
	
	public GoogleSheet(Sheets service, String spreadsheetId) {
		this.service = service;
		this.spreadsheetId = spreadsheetId;
	}
}
