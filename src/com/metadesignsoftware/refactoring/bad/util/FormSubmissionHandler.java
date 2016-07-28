package com.metadesignsoftware.refactoring.bad.util;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import com.google.common.io.Files;
import com.metadesignsoftware.refactoring.bad.config.CompanyEmails;
import com.metadesignsoftware.refactoring.bad.config.Configuration;
import com.metadesignsoftware.refactoring.bad.config.SalesForceParams;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.PasswordAuthentication;

public class FormSubmissionHandler {

	public String submitForm(Map<String,String> _SERVER, Map<String,String> _POST) {

		FormSubmissionResult formSubmissionResult = new FormSubmissionResult();
		switch(_POST.get("ajaxBusiness")){
			case "contactForm":
			case "ctaForm":
				String firstName =_POST.get("first_name"); 
				if ( firstName != null && firstName.length() >20){
					formSubmissionResult.error.add("FirstName length > 20!")  ;
				}
				String lastName =_POST.get("last_name");
				if (lastName != null && lastName.length() > 20){
					formSubmissionResult.error.add("LastName length > 20!")  ;
				}
				String email = _POST.get("email");
				if (email!=null && email.length()>50){
					formSubmissionResult.error.add("Email length > 50!")  ;
				}

				String phone = _POST.get("phone");
				if (phone != null && phone.length() >20){
					formSubmissionResult.error.add("Phone length > 20!")  ;
				}
				String company = _POST.get("company");
				if (company != null && company.length() >40){
					formSubmissionResult.error.add("Company length > 40!")  ;
				}
				String role = _POST.get(SalesForceParams.get("fieldRoleId"));
				if (role!= null && role.length()>100){
					formSubmissionResult.error.add("Role length > 100!")  ;
				}
				if (formSubmissionResult.error == null){
					Map<String,String> postData = new HashMap<String,String>(_POST);
					postData.put("debug", "1");
					//$postData['debugEmail'] = $magnetEmails['debug'];
					postData.remove("ajaxBusiness");
					
					//func_print_r($postData);
					String attachment = null;
					if (_POST.get("ajaxBusiness").equals("ctaForm")){
						postData.put((String)SalesForceParams.get("fieldTalkToUsId"),(String)_POST.get("form_type")) ;
						postData.put((String)SalesForceParams.get("fieldProductInterestedIn"),(String)_POST.get("product_interested_in")) ;
						postData.put((String)SalesForceParams.get("fieldRoleId"),(String)_POST.get("role")) ;
						attachment = postData.get("attach");
					}
				
					boolean emailTo1 = false, emailTo2 = false;
					
					if (postData.get((String)SalesForceParams.get("fieldTalkToUsId")) != null ){
						if (postData.get(SalesForceParams.get("fieldTalkToUsId")).equals("Get a test drive") || postData.get(SalesForceParams.get("fieldTalkToUsId")).equals("Get a quote")){
							emailTo1 = true;
						}
						if (postData.get(SalesForceParams.get("fieldTalkToUsId")).equals("Get a meeting")){
							emailTo2 = true;
						}
					}

					String result = postCurlContent((String)SalesForceParams.get("urlPost"),postData);
					formSubmissionResult.success = result;
					// send email to sales, bizdev and debug
					String mailBody = result;
					String subject = "Contact Form";
					if(postData.get((String)SalesForceParams.get("fieldTalkToUsId")) != null){
						subject = subject + " " +postData.get((String)SalesForceParams.get("fieldTalkToUsId"));
					}
					subject = subject + "Company " + Configuration.getValueOf("deploymentType");

					String billFile = null; 
					if (((String)postData.get("form_type")).equals("Health Check")){
						if (postData.get("bill") != null){
							billFile = (String)postData.get("bill");
						}
					}
					String from = CompanyEmails.get("from");

					int recipientCount = 0;
					if (emailTo1 && emailTo2){
						recipientCount =2;
					}else 
						recipientCount =1;
					String to[] = new String[recipientCount];
					int emailIndex = 0;
					if (emailTo1)
						to[emailIndex++] = CompanyEmails.get("business");
					if (emailTo2)
						to[emailIndex++] = CompanyEmails.get("sales");
					if (emailIndex == 0)
						to[emailIndex++] = CompanyEmails.get("sales");
						
					
					// send email to sales, debug and bizdev
					String emailBody = result;
					sendEmail(from, to,subject, emailBody,billFile);
					
					// send reply back to consumer.
					// from remains the same
					// to is picked up from form
					to = new String[1];
					to[0] = _POST.get("email");
					String emailTemplateFile = Configuration.getValueOf("templateDirectory")+ _POST.get("emailTemplate");
					String emailTemplate;
					try {
						emailTemplate = Files.toString(new File(emailTemplateFile), Charsets.UTF_8);
					} catch (IOException e) {
						emailTemplateFile = Configuration.getValueOf("templateDirectory")+ "/default.html";
						try {
							emailTemplate = Files.toString(new File(emailTemplateFile), Charsets.UTF_8);
						} catch (IOException e1) {
							return "could not find default template";
						}
					}
					emailBody = emailTemplate.replaceAll("\\{EmailMeName\\}", firstName+" "+ lastName);
					String messageHeader = "<html>" +
							"<head>" +
							"<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />" +
							"<title>Email Me</title>" +
							"</head>" +
							"<body>" +
							"<div style='font-size: 13px; font-family: arial, helvetica, sans-serif;'>"+"\n";
					String messageFooter = "</div></body></html>";
					String message = messageHeader + emailBody + messageFooter;
					sendEmail(from, to,subject, message,attachment);
					
				}
				break;
			case "eventReg":
				formSubmissionResult.error=FormSubmissionUtility.validateForm(_POST);
				
				String result = postCurlContent((String)SalesForceParams.get("urlPost"),_POST);
				String eventName = _POST.get(SalesForceParams.get("event_name"));
				String from = CompanyEmails.get("from");				
				String to[] = new String[1];
				to[0] = CompanyEmails.get("sales");
				String subject = "Event registration form";
				sendEmail(from, to,subject, result,null);
				break;
		}
			if (formSubmissionResult.error != null){
				Iterator<String> formSubmissionErrorIterator = formSubmissionResult.error.iterator();
				StringBuffer htmlError = new StringBuffer(); 
				while(formSubmissionErrorIterator.hasNext())
				{
					htmlError.append("<BR/>");
					htmlError.append(formSubmissionErrorIterator.next());
				}
				formSubmissionResult.htmlError = htmlError.toString();
			}

		
		return toJSONString(formSubmissionResult);

		
	}
	private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username = Configuration.getValueOf("mail.user");
           String password = Configuration.getValueOf("mail.password");
           return new PasswordAuthentication(username, password);
        }
    }
	private void sendEmail(String from, String to[],String subject, String body,String attachmentFileName) {
		      // Assuming you are sending email from localhost
		      String host = Configuration.getValueOf("mail.smtp.host");

		      // Get system properties
		      Properties properties = System.getProperties();

		      // Setup mail server
		      properties.setProperty("mail.smtp.host", host);
		      properties.setProperty("mail.smtp.port", "587");
		      properties.put("mail.smtp.auth", "true");
		      // Get the default Session object.
		      Authenticator auth = new SMTPAuthenticator();
		      Session session = Session.getDefaultInstance(properties,auth);

		      try{
		         // Create a default MimeMessage object.
		         MimeMessage message = new MimeMessage(session);

		         // Set From: header field of the header.
		         message.setFrom(new InternetAddress(from));

		         // Set To: header field of the header.
		         for (int i=0;i< to.length;i++){
			         message.addRecipient(Message.RecipientType.TO,
                             new InternetAddress(to[i]));
		        	 
		         }

		         // Set Subject: header field
		         message.setSubject(subject);

		         // Create the message part 
		         BodyPart messageBodyPart = new MimeBodyPart();

		         // Fill the message
		         messageBodyPart.setContent(body, "text/html");
		         
		         // Create a multipar message
		         Multipart multipart = new MimeMultipart();

		         // Set text message part
		         multipart.addBodyPart(messageBodyPart);

		         // Part two is attachment
		         messageBodyPart = new MimeBodyPart();
		         if (attachmentFileName != null){
			         DataSource source = new FileDataSource(attachmentFileName );
			         messageBodyPart.setDataHandler(new DataHandler(source));
			         messageBodyPart.setFileName(attachmentFileName );
			         multipart.addBodyPart(messageBodyPart);
		         }

		         // Send the complete message parts
		         message.setContent(multipart );

		         // Send message
		         Transport.send(message);
		         System.out.println("Sent message successfully....");
		      }catch (MessagingException mex) {
		         mex.printStackTrace();
		      }
	}
	
	
	private String postCurlContent(String postUrl,Map<String,String> postData){

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(postUrl);
        BufferedReader reader = null;
 
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        Iterator it = postData.entrySet().iterator();
        while(it.hasNext()){
        	Map.Entry<String,String> pair = (Map.Entry<String, String>)it.next();
        	urlParameters.add(new BasicNameValuePair(pair.getKey(), pair.getValue()));	
        }
        try{
            HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
            httpPost.setEntity(postParams);
     
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
     
            reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent()));
     
            String inputLine;
            StringBuffer response = new StringBuffer();
     
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
     
            // print result
            return response.toString();
        	
        }catch (Exception e)
        {
        	return "Error while sending to SalesForce! " + e.getMessage();
        }finally{
        	try{
        		reader.close();
                httpClient.close();
        	}catch (Exception e){}
            
        }
   }
 
		
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String,String> _SERVER= null;
		FormSubmissionHandler formSubmissionHandler = new FormSubmissionHandler();
		String result = formSubmissionHandler.submitForm(_SERVER, getPostData());
		System.out.println("result is " + result);
	}
	
	private static HashMap<String,String> getPostData(){
		HashMap<String,String> data = new HashMap<String,String>();
		data.put("submitted_from_page", "http://www.company.com/business/free_trial_office_inabox/");
		data.put("oid","00D170000009T9z");
		data.put("retURL","http://www.company.com/business/thanks-for-your-interest/");
		data.put("recordType","012200000004TNw");
		data.put("lead_source","Corporate Web to Lead");
		data.put("ajaxBusiness","ctaForm");
		data.put("action","ctaform");
		data.put("actonURL","http://marketing.company.com/acton/eform/18103/0005/d-ext-0001");
		data.put("params","[]");
		data.put("form_type","Get a test drive");
		data.put("product_interested_in","Office in a box");
		data.put("first_name","Pradeep");
		data.put("last_name","CK");
		data.put("email","pradeep.ck@gmail.com");
		data.put("company","Magnet");
		data.put("role","Programmer");
		data.put("phone","1234567890");
		return data;
	}

	public String toJSONString(FormSubmissionResult formSubmissionResult)
	{
		JSONObject jsonObject = new JSONObject();
		if (formSubmissionResult.htmlError != null)
			jsonObject.put("error",formSubmissionResult.htmlError);
		else
			jsonObject.put("succcess", formSubmissionResult.success);
		return jsonObject.toJSONString();
	}
	class FormSubmissionResult {
		String success;
		ArrayList<String> error;
		String htmlError;
	}
}
