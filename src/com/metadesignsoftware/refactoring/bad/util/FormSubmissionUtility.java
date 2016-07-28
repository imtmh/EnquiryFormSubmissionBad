package com.metadesignsoftware.refactoring.bad.util;

import java.util.ArrayList;
import java.util.Map;

import com.metadesignsoftware.refactoring.bad.config.SalesForceParams;

public class FormSubmissionUtility {


	static ArrayList<String> validateForm(Map<String, String> _POST) {
		ArrayList<String> error = new ArrayList<String>();
		String firstName = _POST.get("first_name");
		if (firstName != null && firstName.length() > 20) {
			error.add("FirstName length > 20!");
		}
		String lastName = _POST.get("last_name");
		if (lastName != null && lastName.length() > 20) {
			error.add("LastName length > 20!");
		}
		String email = _POST.get("email");
		if (email != null && email.length() > 50) {
			error.add("Email length > 50!");
		}

		String phone = _POST.get("phone");
		if (phone != null && phone.length() > 20) {
			error.add("Phone length > 20!");
		}
		String company = _POST.get("company");
		if (company != null && company.length() > 40) {
			error.add("Company length > 40!");
		}
		String role = _POST.get(SalesForceParams.get("fieldRoleId"));
		if (role != null && role.length() > 100) {
			error.add("Role length > 100!");
		}
		return error;
	}
}
