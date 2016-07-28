package com.metadesignsoftware.refactoring.bad.config;

import java.util.HashMap;

public class SalesForceParams {

	private static HashMap<String, Object> salesForceParams = new HashMap<String, Object>();

	static {

		salesForceParams.put("urlPost",   "https://salesforce.com/servlet/servlet.WebToLead?encoding=UTF-8");
		salesForceParams.put("urlReturn", "https://company.com/salesForceResponse");
		salesForceParams.put("oid", "00D200000000JBH");
		salesForceParams.put("fieldRoleId", "00Nw00000076Dk7");
		salesForceParams.put("fieldNoteId", "00N20000003J3dfEAC");
		salesForceParams.put("fieldInterestedId", "00N20000000trXk");
		salesForceParams.put("fieldTalkToUsId", "00Nw00000076Djd");
		salesForceParams.put("fieldProductInterestedIn", "00N200000029eNU");
		salesForceParams.put("fieldMeetingDate", "00Nw0000008NF0m");
		salesForceParams.put("oid", "00D200000000JBH");
		
		HashMap<String,String> contactFormData = new HashMap<String,String>();
		contactFormData.put("recordType", "012200000004TNw"); //Business Corporate Lead
		contactFormData.put("leadSource", "Corporate Web to Lead");
		salesForceParams.put("contactForm", contactFormData);
		
		HashMap<String,String> ctaFormData = new HashMap<String,String>();
		contactFormData.put("recordType", "012200000004TNw"); //Business Corporate Lead
		contactFormData.put("leadSource", "Corporate Web to Lead");
		salesForceParams.put("ctaForm", ctaFormData);
		
		HashMap<String,String> emailMeFormData = new HashMap<String,String>();
		contactFormData.put("recordType", "012200000004TNw"); //Business Corporate Lead
		contactFormData.put("leadSource", "Corporate Web to Lead");
		salesForceParams.put("emailmeForm", emailMeFormData);
		
		HashMap<String,String> eventRegFormData = new HashMap<String,String>();
		contactFormData.put("recordType", "012w0000000QcxG"); //Business Corporate Lead
		contactFormData.put("leadSource", "Corporate Web to Lead");
		salesForceParams.put("eventRegForm",eventRegFormData);
		
		if (Configuration.getValueOf("deploymentType") != "live"){
			salesForceParams.put("urlPost","https://test.salesforce.com/servlet/servlet.WebToLead?encoding=UTF-8");
			salesForceParams.put("oid","00D170000009T9z"); // oid can be change on salesforce sandbox refresh
		}
	}

	public static Object get(String key) {
		return salesForceParams.get(key);
	}
}
