/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.office;

import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for {@link TemplateProcessor}
 */
public class TemplateProcessorHelper {
    
    private static final String PARAMETER_SEPARATOR = "$";

    /**
     * Get a part of the telephone number
     * 
     * @param pre
     *      <code>true</code>, if the area code should be returned
     * @return
     *      Part of the telephone number
     */
    String getTelPrePost(final String phoneNo, final boolean pre) {
        String phoneNumber = StringUtils.defaultString(phoneNo);
        // if phoneNo contains "/" or " " (space) then split there
        if(phoneNumber.startsWith("+")) {
            // phoneNo has a country dialing number, therefore we have to chain it with area code
            phoneNumber = phoneNumber.replaceFirst(" ", "_");
        }
        String parts[] = phoneNumber.trim().split("[ |/]", 2);
        
        // Split the number
        if (parts.length < 2) {
            String tel = parts[0];
            // divide the number at the 4th position
            if (tel.length() > 4) {
                if (pre)
                    return tel.substring(0, 4);
                else
                    return tel.substring(4);
            }
            // The number is very short
            return pre ? "" : tel;
        }
        // return the first or the second part
        else {
            String[] area = StringUtils.split(parts[0], "_");  // phone number with country code
            return pre ? StringUtils.join(area, " ") : parts[1];
        }
    }
    
    
    /**
     * Extracts the placeholder name, separated by a $
     * 
     * @param s
     *      The placeholder with parameters
     * @return
     *      The placeholder name without paramater
     */
    String extractPlaceholderName(String s) {
        return s.split("\\$", 2)[0];
    }

    Integer extractLengthFromParameter(String par, Integer defaultValue) {
        Integer length;
        try {
            length = Integer.parseInt(par);
        } catch (NumberFormatException e) {
            length = defaultValue;
        }
        return length;
    }

    /**
     * Decode the special characters
     * 
     * @param s
     *  String to convert
     * @return
     *  Converted
     */
    String encodeEntities(String s) {
        if (StringUtils.length(s) > 0) {
            s = s.replaceAll("%LT", "<");
            s = s.replaceAll("%GT", ">");
            s = s.replaceAll("%NL", "\n");
            s = s.replaceAll("%TAB", "\t");
            s = s.replaceAll("%SPACE", " ");
            s = s.replaceAll("%DOLLAR", Matcher.quoteReplacement(PARAMETER_SEPARATOR));
            s = s.replaceAll("%COMMA", Matcher.quoteReplacement(","));
            s = s.replaceAll("%EURO", Matcher.quoteReplacement("€"));
            s = s.replaceAll("%A_GRAVE", Matcher.quoteReplacement("À"));
            s = s.replaceAll("%A_ACUTE", Matcher.quoteReplacement("Á"));
            s = s.replaceAll("%A_CIRC", Matcher.quoteReplacement("Â"));
            s = s.replaceAll("%A_TILDE", Matcher.quoteReplacement("Ã"));
            s = s.replaceAll("%A_RING", Matcher.quoteReplacement("Å"));
            s = s.replaceAll("%C_CED", Matcher.quoteReplacement("Ç"));
            s = s.replaceAll("%E_GRAVE", Matcher.quoteReplacement("È"));
            s = s.replaceAll("%E_ACUTE", Matcher.quoteReplacement("É"));
            s = s.replaceAll("%E_CIRC", Matcher.quoteReplacement("Ê"));
            s = s.replaceAll("%I_GRAVE", Matcher.quoteReplacement("Ì"));
            s = s.replaceAll("%I_ACUTE", Matcher.quoteReplacement("Í"));
            s = s.replaceAll("%I_CIRC", Matcher.quoteReplacement("Î"));
            s = s.replaceAll("%O_GRAVE", Matcher.quoteReplacement("Ò"));
            s = s.replaceAll("%O_ACUTE", Matcher.quoteReplacement("Ó"));
            s = s.replaceAll("%O_CIRC", Matcher.quoteReplacement("Ô"));
            s = s.replaceAll("%O_TILDE", Matcher.quoteReplacement("Õ"));
            s = s.replaceAll("%O_STROKE", Matcher.quoteReplacement("Ø"));
            s = s.replaceAll("%U_GRAVE", Matcher.quoteReplacement("Ù"));
            s = s.replaceAll("%U_ACUTE", Matcher.quoteReplacement("Ú"));
            s = s.replaceAll("%U_CIRC", Matcher.quoteReplacement("Û"));
            s = s.replaceAll("%a_GRAVE", Matcher.quoteReplacement("à"));
            s = s.replaceAll("%a_ACUTE", Matcher.quoteReplacement("á"));
            s = s.replaceAll("%a_CIRC", Matcher.quoteReplacement("â"));
            s = s.replaceAll("%a_TILDE", Matcher.quoteReplacement("ã"));
            s = s.replaceAll("%a_RING", Matcher.quoteReplacement("å"));
            s = s.replaceAll("%c_CED", Matcher.quoteReplacement("ç"));
            s = s.replaceAll("%e_GRAVE", Matcher.quoteReplacement("è"));
            s = s.replaceAll("%e_ACUTE", Matcher.quoteReplacement("é"));
            s = s.replaceAll("%e_CIRC", Matcher.quoteReplacement("ê"));
            s = s.replaceAll("%i_GRAVE", Matcher.quoteReplacement("ì"));
            s = s.replaceAll("%i_ACUTE", Matcher.quoteReplacement("í"));
            s = s.replaceAll("%i_CIRC", Matcher.quoteReplacement("î"));
            s = s.replaceAll("%n_TILDE", Matcher.quoteReplacement("ñ"));
            s = s.replaceAll("%o_GRAVE", Matcher.quoteReplacement("ò"));
            s = s.replaceAll("%o_ACUTE", Matcher.quoteReplacement("ó"));
            s = s.replaceAll("%o_CIRC", Matcher.quoteReplacement("ô"));
            s = s.replaceAll("%o_TILDE", Matcher.quoteReplacement("õ"));
            s = s.replaceAll("%u_GRAVE", Matcher.quoteReplacement("ù"));
            s = s.replaceAll("%u_ACUTE", Matcher.quoteReplacement("ú"));
            s = s.replaceAll("%u_CIRC", Matcher.quoteReplacement("û"));
            s = s.replaceAll("%%", Matcher.quoteReplacement("%"));
        }
        return s;
    }   

    /**
     * Extract the value of the parameter of a placeholder
     * 
     * @param placeholder
     *  The placeholder name
     * 
     * @param param
     *  Name of the parameter to extract
     * 
     * @return
     *  The extracted value
     */
    String extractParam(String placeholder, String param) {
        String s;
        
        // A parameter starts with "$" and ends with ":"
        param = PARAMETER_SEPARATOR + param + ":";
        
        // Return, if parameter was not in placeholder's name
        if (!placeholder.contains(param))
            return "";

        // Extract the string after the parameter name
        s = placeholder.substring(placeholder.indexOf(param)+param.length());

        // Extract the string until the next parameter, or the end
        int i;
        i = s.indexOf(PARAMETER_SEPARATOR);
        if ( i>0 )
            s= s.substring(0, i);
        else if (i == 0)
            s = "";
        
        i = s.indexOf(">");
        if ( i>0 )
            s= s.substring(0, i);
        else if (i == 0)
            s = "";

        // Return the value
        return s;
    }

    
    
    /* * * TEST ONLY * * */

    public static void main(String[] args) {
        
        TemplateProcessorHelper ph = new TemplateProcessorHelper();     
//      ph.extractPlaceholderName("$INONELINE:,$DOCUMENT.ADDRESS");
//      ph.interpretParameters("$INONELINE:,$DOCUMENT.ADDRESS", "Erdrich\nTester\nFakestreet 22");
        
//      System.out.println("is 'DOCUMENT.ADDRESS' placeholder? " + ph.isPlaceholder("DOCUMENT.ADDRESS"));
//      System.out.println("is 'NO.PLACEHOLDER' placeholder? " + ph.isPlaceholder("NO.PLACEHOLDER"));
//      
//      System.out.println("mit null: " + ph.censorAccountNumber(null));
//      System.out.println("mit 12: " + ph.censorAccountNumber("12"));
//      System.out.println("mit 123: " + ph.censorAccountNumber("123"));
//      System.out.println("mit 123456789: " + ph.censorAccountNumber("123456789"));
//      System.out.println("mit 9999999999999999999999999999999999: " + ph.censorAccountNumber("9999999999999999999999999999999999"));
        
        // test phone numbers
        String[] testphones = new String[]{
                "02031/4775864",
                "+49 (0)2031/4775864",
                "020315 75864",
                "020315/75864",
                "+49 (0)20315 75864",
                "02031 4775864",
                "030/44775864",
                "030 44775864",
                "+49 (0)30 44775864",
                "03726 2824",
                "03726 781-0",
                "03726 781",
        };
        for (String phone : testphones) {
            System.out.println(String.format("PHONE: [%s]; PRE: [%s]; POST: [%s]", phone, ph.getTelPrePost(phone, true), ph.getTelPrePost(phone, false)));
        }
    }

}
