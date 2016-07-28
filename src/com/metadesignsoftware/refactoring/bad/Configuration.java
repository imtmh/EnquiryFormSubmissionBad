package com.metadesignsoftware.refactoring.bad;

import java.util.HashMap;

public class Configuration {

	static HashMap<String, String> properties = new HashMap<String, String>();

	static {
		properties.put("templateDirectory", "./src/emailtemplates");
		properties.put("deploymentType", "live");
		properties.put("mail.user", "test@metadesignsoftware.com");
		properties.put("mail.password", "Test123$");
		properties.put("mail.smtp.host", "secure.emailsrvr.com");
	}

	public static String getValueOf(String key) {
		return properties.get(key);
	}
}
