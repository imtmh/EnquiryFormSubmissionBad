package com.metadesignsoftware.refactoring.bad.config;

import java.util.HashMap;

public class CompanyEmails {

	private static HashMap<String, String> properties = new HashMap<String, String>();

	static {
		properties.put("from","test@metadesignsoftware.com") ;
		properties.put("debug","thedeveloper@company.com") ;
		properties.put("business","business@company.com") ;
		properties.put("sales","sales@company.com") ;
	}

	public static String get(String key) {
		return properties.get(key);
	}
}
