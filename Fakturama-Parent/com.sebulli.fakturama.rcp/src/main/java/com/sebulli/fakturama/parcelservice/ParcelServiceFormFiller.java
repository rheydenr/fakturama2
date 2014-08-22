/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.parcelservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.IEditorInput;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.misc.DataUtils;

/**
 * Fills the form of the parcel service
 * 
 * @author Gerd Bartelt
 *
 */
public class ParcelServiceFormFiller {
	
	// The webbrowser
	private Browser browser;
	
	// String that contains the name of all form fields
	private String allFields = null;
	
	// True, if form was filled
	private boolean filled;

	
	/**
	 * Constructor
	 */
	public ParcelServiceFormFiller() {
		filled = false;
	}

	/**
	 * Fill a field with a value
	 * 
	 * @param fieldName
	 * 		The name of the field
	 * @param value
	 * 		The value
	 */
	private void fillFormField (String fieldName, String pvalue) {
		
		if (fieldName == null)
			return;

		if (pvalue == null)
			return;
		
		if (fieldName.isEmpty())
			return;
		
		//System.out.println("fill: " + fieldName + " with: "+ pvalue);
		String value = pvalue.trim();
		
		// Script that counts the fields with this name
		String script = "" +
			
			// Trim the string 
			"function trim (s) {" +
			"  return s.replace (/^\\s+/, '').replace (/\\s+$/, '');" +
			"}" +

			// Select an item of a drop box
			"function setSelectedIndex(s, v) {" +
			"    for ( var i = 0; i < s.options.length; i++ ) {" +
			"        if ( s.options[i].value == v ) {" +
			"            s.options[i].selected = true;" +
			"            return;" +
			"        }" +
			"    }" +
			"}" +
			
			// Fill the form field
			"function fillField() {"+
			"  var cnt = 0;" +
			"  documentForms = document.getElementsByTagName('form');" +
			"  for (var i = 0; i < documentForms.length; i++) {" +
			"    for(var ii = 0; ii < documentForms[i].elements.length; ii++) {" +
			"      var e = documentForms[i].elements[ii];" +
			"      if (e.name == '" + fieldName +"') {" +
			"        if ((e.type == 'select-one') || (e.type == 'select')) {" +
			"           setSelectedIndex(e, \"" + value + "\");" +
			"        } else {" + 
			"          e.value = '" + value + "';" +
			"        }" + 
			"      }" + 
			"    }" +
			"  }" +
			" return String(cnt);" +
			"};" +
			"fillField();"; 

			// Execute the script to fill the field.
			browser.execute(script);
	}
	
	/**
	 * Tests, whether a form field exists
	 * 
	 * @param fieldName
	 * 		The name of the field to test
	 * @return
	 * 		TRUE, if it exists.
	 */
	private boolean formFieldExists (String fieldName) {
		
		
		if (allFields == null) {
			// Script that counts the fields with this name
			String script = "" +
				"function getAllFields() {"+
				"  var s = ':';" +
				"  documentForms = document.getElementsByTagName('form');" +
				"  for (var i = 0; i < documentForms.length; i++) {" +
				"    for(var ii = 0; ii < documentForms[i].elements.length; ii++) {" +
				"      if (documentForms[i].elements[ii].name) {" +
				"        s = s + documentForms[i].elements[ii].name + ':' ;" +
				"      }" + 
				"    }" +
				"  }" +
				" return s;" +
				"};" +
				"return getAllFields();"; 

				// Convert the result to an integer
				allFields = (String)browser.evaluate(script);
		}
		
		return allFields.contains(":" + fieldName + ":");
		
	}
	
	/**
	 * Tests, whether a form field exists
	 * 
	 * @param fieldName
	 * 		The name of the field to test
	 * @return
	 * 		TRUE, if it exists.
	 */

	
	
	/**
	 * Fills the form of the parcel service with the address data
	 */
	public void fillForm(Browser browser, IEditorInput editorInput, boolean forceFill) {
//		this.browser = browser;
//
//		Properties inputProperties = ((ParcelServiceBrowserEditorInput)editorInput).getProperties();
//		DataSetDocument document =((ParcelServiceBrowserEditorInput)editorInput).getDocument();
//		Properties p = new Properties();
//		
//		// Switch key and value
//		for (Map.Entry<Object, Object> propItem : inputProperties.entrySet())
//		{
//		    String key = (String) propItem.getKey();
//
//		    String value = (String) propItem.getValue();
//			if ((!key.equalsIgnoreCase("url")) && (!key.equalsIgnoreCase("url")) && (!value.isEmpty()) ) {
//				p.put(value, key);
//			}
//		}
//		
//		// Fill the fields
//		// At least this fields must exist in the website's form
//		if (( ( formFieldExists(p.getProperty("DELIVERY.ADDRESS.NAME")) ||
//			   formFieldExists(p.getProperty("DELIVERY.ADDRESS.LASTNAME")) ||
//			   formFieldExists(p.getProperty("DELIVERY.ADDRESS.COMPANY")) ||
//			   formFieldExists(p.getProperty("YOURCOMPANY.COMPANY")) ||
//			   formFieldExists(p.getProperty("YOURCOMPANY.OWNER")) ||
//			   formFieldExists(p.getProperty("YOURCOMPANY.OWNER.FIRSTNAME")) ||
//			   formFieldExists(p.getProperty("YOURCOMPANY.OWNER.LASTNAME")) ||
//			   formFieldExists(p.getProperty("ADDRESS.NAME")) ||
//			   formFieldExists(p.getProperty("ADDRESS.LASTNAME"))   )&&
//				!filled ) || forceFill){
//			filled = true;
//
//			// get all entries
//			for (Map.Entry<Object, Object> propItem : inputProperties.entrySet())
//			{
//			    String key = (String) propItem.getKey();
//			    String value = ((String) propItem.getValue()).trim();
//
//			    if ((!key.equalsIgnoreCase("url")) && (!key.equalsIgnoreCase("url")) && (!value.isEmpty()) ) {
//
//			    	// It is a placeholder
//			    	if (Placeholders.isPlaceholder(value)) {
//						fillFormField(key, Placeholders.getDocumentInfo(document, value));
//			    	}
//			    	// It is a constant String
//			    	else if (value.startsWith("\"") && value.endsWith("\"")) {
//
//			    		// Remove trailing and leading ""
//			    		value = value.substring(1, value.length()-1);
//			    		fillFormField(key, value);
//			    		
//			    	}
//				}
//			}
//		}
	}
	
	/**
	 * 
	 * @param title
	 * 		Title of the website
	 * @return
	 * 		The file name
	 */
	private static String getParcelServiceFileName(String title) {
//		// Get the directory of the workspace
//		String filename = Activator.getDefault().getPreferenceStore().getString("GENERAL_WORKSPACE");
//
//		// Do not save parcel service files, if there is no workspace set
//		if (filename.isEmpty())
//			return "";
//
//		// Do not save  parcel service files, if workspace is not created
//		File directory = new File(filename);
//		if (!directory.exists())
//			return "";
//
//		// Create a sub folder "ParcelService", if it does not exist yet.
//		filename += "/" + ParcelServiceManager.getRelativeTemplatePath();
//		directory = new File(filename);
//		if (!directory.exists())
//			directory.mkdirs();
//
//		if (new File(filename + title+".txt").exists()) {
//			// Add date and time, if file exists
//			title+= "_"+DataUtils.DateAndTimeOfNowAsISO8601String();
//			title = title.replaceAll(":", "");
//			title = title.replaceAll(" ", "_");
//			title = title.replaceAll("\\.", "");
//		}
//
//		filename += title+".txt";
//
//		return filename;
	    return null;
	}

	
	
	/**
	 * Test the parcel service form.
	 * Fills all form elements with its names and creates a template file.
	 * 
	 */
	public static void testParcelServiceForm(Browser browser) {
//		// Script that counts the fields with this name
//		String script = "" +
//			"function getAllFields() {" +
//			"  var s = ':';"+
//			"  documentForms = document.getElementsByTagName('form');" +
//			"  for (var i = 0; i < documentForms.length; i++) {" +
//			"    for(var ii = 0; ii < documentForms[i].elements.length; ii++) {" +
//			"		var e = documentForms[i].elements[ii];" +
//			"       e.title = e.name; " +
//			"       if ((e.type == 'text') || (e.type == 'textarea')) {" +
//			"         e.value = e.name;" +
//			"         s = s + e.name + ':'" +
//			"       } " + 
//			"       if ((e.type == 'select-one') || (e.type == 'select')) {" +
//			"         s = s + e.name + ':'" +
//			"       } " + 
//			"    }" +
//			"  }" +
//			"  return s;" +
//			"};" +
//			"return getAllFields();";
//		
//			String result = "";
//		
//			// Get the name of all text form elements
//			if (browser != null) {
//				result = (String)browser.evaluate(script);
//			}
//			
//			// The result string must be at least 10 characters long
//			// If not, this could not be a collection of all form fields of
//			// a parcel service web site
//			if (result.length() > 10) {
//
//				// Get the website's title
//				String title = (String)browser.evaluate("return document.title;");
//				
//				// Generate a file name
//				String filename = getParcelServiceFileName(title);
//
//				// Create a new parcel service template file
//				File file = new File(filename);
//				try {
//					
//					// Create a new file
//					file.createNewFile();
//					BufferedWriter bos = new BufferedWriter(new FileWriter(file, true));
//					
//					// Add name and URL
//					String NL = OSDependent.getNewLine();
//					String s = "";
//					s += "# Name and URL of the parcel service:" + NL;
//					s += "name = "+ title + NL;  
//					s += "url  = "+ (String)browser.evaluate("return document.URL.split('?')[0];") + NL + NL;
//					s += "# Fields:"+ NL;
//					bos.write(s);
//
//					// Add all text fields
//					String fieldNames[] = result.split(":");
//					int longestString = 0;
//					for (String fieldName : fieldNames) {
//						if (fieldName.length() > longestString)
//							longestString = fieldName.length();
//					}
//					for (String fieldName : fieldNames) {
//						fieldName = fieldName.trim();
//						if (!fieldName.isEmpty()) {
//							if (longestString > 24)
//								longestString = 24;
//							
//							int l = longestString - fieldName.length();
//							if (l < 0)
//								l = 0;
//							bos.write(fieldName + "                         ".substring(24-l) + "="+ NL);
//						}
//					}
//					
//					// A an additional help text
//					s = NL+ NL+ NL+ NL + 
//						"# If you have created a template file for a new parcel service," + NL +
//						"# it would be nice to share it with other users on fakturama.sebulli.com" + NL +
//						"# " + NL +
//						"# Syntax:" + NL +
//						"# field = PLACE.HOLDER" + NL +
//						"#" + NL +
//						"# Some of the most significant placeholders are:" + NL + 
//						"#" + NL +
//						"# YOURCOMPANY.COMPANY" + NL +
//						"# YOURCOMPANY.OWNER" + NL +
//						"# YOURCOMPANY.OWNER.FIRSTNAME" + NL +
//						"# YOURCOMPANY.OWNER.LASTNAME" + NL +
//						"# YOURCOMPANY.STREET" + NL +
//						"# YOURCOMPANY.STREETNAME" + NL +
//						"# YOURCOMPANY.STREETNO" + NL +
//						"# YOURCOMPANY.ZIP" + NL +
//						"# YOURCOMPANY.CITY" + NL +
//						"#" + NL +
//						"# DELIVERY.ADDRESS.COMPANY" + NL +
//						"# DELIVERY.ADDRESS.NAME" + NL +
//						"# DELIVERY.ADDRESS.FIRSTNAME" + NL +
//						"# DELIVERY.ADDRESS.LASTNAME" + NL +
//						"# DELIVERY.ADDRESS.COMPANY" + NL +
//						"# DELIVERY.ADDRESS.STREET" + NL +
//						"# DELIVERY.ADDRESS.STREETNAME" + NL +
//						"# DELIVERY.ADDRESS.STREETNO" + NL +
//						"# DELIVERY.ADDRESS.ZIP" + NL +
//						"# DELIVERY.ADDRESS.CITY" +  NL +
//						"#" + NL +
//						"# Read the manual for all placeholders." + NL;
//					bos.write(s);
//					bos.close();
//					
//					// Show a dialog
//					Workspace.showMessageBox(SWT.OK, 
//							//T: Message box title
//							_("File created"),
//							//T: Message box text
//							_("A new parcel service template:\n") + filename + "\n" + 
//							//T: Message box text
//							_("was created"));
//					
//
//				} catch (IOException e) {
//				}
//			}
			
	}


}
