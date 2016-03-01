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

package org.fakturama.wizards;


/**
 * This class contains static functions to format the output
 * 
 * @author Gerd Bartelt
 */
public class ExporterHelper {

	public static String inQuotes(String s) {
		if(s == null) {
			return "";
		}
		
		// Replace all illegal characters
		s = s.replaceAll("\"", "”");
		s = s.replaceAll("\\n\\r","\n");
		s = s.replaceAll("\\n"," ");
		s = s.replaceAll("\\r"," ");
		
		// Add the quotes
		return "\"" + s + "\"";
	}

}
