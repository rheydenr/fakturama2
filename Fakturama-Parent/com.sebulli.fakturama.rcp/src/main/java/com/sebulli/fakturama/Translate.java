/**
 * 
 */
package com.sebulli.fakturama;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.equinox.log.Logger;

/**
 * @author Administrator
 *@deprecated use eclipse l10n instead!!!
 */
public class Translate {

	private static Properties messages = null;

	private enum states {
	    IDLE, MSGCTXT, MSGID, MSGSTR 
	}

	/**
	 * Replace a string by the translated string.
	 * If no translation is available, return the original one.
	 * 
	 * @param s
	 * 			String to translate
	 * @return
	 * 			The translated String
	 */
	public static String _(String s) {
		
		String sout;
		
		if (messages == null) {
			messages = new Properties();
			loadPoFile();
		}

		if (!messages.containsKey(s))
			return s;
		else {
			sout = messages.getProperty(s);
			if (sout.isEmpty())
				return s;
			else
				return sout;
		}
	}

	/**
	 * Replace a string by the translated string.
	 * If no translation is available, return the original one.
	 * 
	 * @param s
	 * 			String to translate
	 * @param translate
	 * 			TRUE, if the string should be translated
	 * @return
	 * 			The translated String
	 */
	public static String _(String s, boolean translate) {
		
		if (translate)
			return _(s);
		else
			return s;
	}

	/**
	 * Replace a string in a context by the translated string.
	 * If no translation is available, return the original one.
	 * 
	 * @param s
	 * 			String to translate
	 * @param context
	 * 			Context of the string
	 * @return
	 * 			The translated String
	 */
	public static String _(String s, String context) {

		// Context and string are added and separated by a vertical line
		String sWithContext = context + "|" + s;
		String sout;
		
		if (messages == null) {
			messages = new Properties();
			loadPoFile();
		}

		if (!messages.containsKey(sWithContext))
			return s;
		else {
			sout = messages.getProperty(sWithContext);
			if (sout.isEmpty())
				return s;
			else
				return sout;
		}
	}
	
	/**
	 * Replace a string by the translated string.
	 * If no translation is available, return the original one.
	 * 
	 * @param s
	 * 			String to translate
	 * @param context
	 * 			Context of the string
	 * @param translate
	 * 			TRUE, if the string should be translated
	 * @return
	 * 			The translated String
	 */
	public static String _(String s, String context, boolean translate) {
		
		if (translate)
			return _(s, context);
		else
			return s;
	}

	/**
	 * Load a PO file from the resource and fill the properties
	 *
	 * @return
	 * 			url of the resource to load
	 */
	public static void loadPoFile (URL url) {
		states state = states.IDLE;
		String msgCtxt = "";
		String msgId = "";
		String msgStr = "";
		
		try {
			// Open the resource message po file.
			if (url == null)
				return;
			
			InputStream	in = url.openStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF8"));
	        String strLine;
	        
	        //Read file line by line
	        while ((strLine = br.readLine()) != null)   {
	        	
	        	// Search for lines with leading "msgctxt"
	        	if (strLine.startsWith("msgctxt")) {

	        		if (state != states.MSGCTXT)
	        			msgCtxt = "";

	        		// Set the state machine to MSGCTXT
        			state = states.MSGCTXT;
        			// Get the string
        			strLine = strLine.substring(7).trim();
        		} 

	        	// Search for lines with leading "msgid"
	        	if (strLine.startsWith("msgid")) {

	        		if (state != states.MSGID)
	        			msgId = "";

	        		// Set the state machine to MSGID
        			state = states.MSGID;
        			// Get the string
        			strLine = strLine.substring(5).trim();
        		} 
	        	
	        	// Search for lines with leading "msgstr"
        		if (strLine.startsWith("msgstr")) {
	        		
        			if (state != states.MSGSTR)
        				msgStr = "";

	        		// Set the state machine to MSGSTR
	        		state = states.MSGSTR;
        			// Get the string
        			strLine = strLine.substring(6).trim();
        		}
        		
        		// Find lines with no translation information
    			if (!strLine.startsWith("\"")) {
        			state = states.IDLE;
        			msgCtxt = "";
        			msgId = "";
        			msgStr = "";
    			} else {
    				
    				// Assemble the string and set the property
    				if (state == states.MSGCTXT) {
    					msgCtxt += format(strLine);
    				}

    				
    				if (state == states.MSGID) {
    				
    					// Add the context to the message ID, separated by a "|"
    					if (msgId.isEmpty()) {
    						if (!msgCtxt.isEmpty()) {
    							msgId = msgCtxt + "|";
    	    					msgCtxt = "";
    						}
    					}
    					msgId += format(strLine);
    				}

    				if (state == states.MSGSTR) {
    					
    					msgCtxt = "";
    					msgStr += format(strLine);
    					if (!msgId.isEmpty())
    						messages.setProperty(msgId, msgStr);
    				}
    			}
	        }
	        //Close the input stream
	        in.close();

		}
		catch (IOException e) {
//			Logger.logError(e, "Error loading message.po.");
		}
	}

	
	/**
	 * Load a PO file from the resource and fill the properties
	 */
	public static void loadPoFile () {
		// Open the resource message po file.
		URL url;
		boolean loadedLocalFile = false;
//		
//		// Get the language file path
//		String workspace = Activator.getDefault().getPreferenceStore().getString("GENERAL_WORKSPACE");
//		String langFilePath = workspace + "/Language/";
//		
//		// Get the directory and find all files
//		File dir = new File(langFilePath);
//		String[] children = dir.list();
//
//		// Get all files
//		if (children != null) {
//			for (int i = 0; i < children.length; i++) {
//				
//				// Get filename of file or directory
//				DocumentFilename langFileName = new DocumentFilename(langFilePath, children[i]);
//				
//				// It's used as a language file, if it ends with a *.po
//				if (langFileName.getExtension().equalsIgnoreCase(".po")) {
//
//					// Get the file
//					File f = new File(langFileName.getPathAndFilename());
//					
//					if (f.exists()) {
//						try {
//							// Load it
//							loadPoFile( f.toURI().toURL());
//							return;
//						} catch (MalformedURLException e) {
//						}
//					}
//					
//				}
//			}
//		}

//
//		// Use the system language and country
//		String localCode = System.getProperty("osgi.nl");
//		
//		// Try to open the messages with language code
//		url = Activator.getDefault().getBundle().getResource("po/messages_" + localCode.split("_")[0] + ".po");
//		if (url != null) {
//			loadPoFile(url);
//			loadedLocalFile = true;
//		}
//		
//		// Try to open the messages with language and country code
//		url = Activator.getDefault().getBundle().getResource("po/messages_" + localCode + ".po");
//		if (url != null) {
//			loadPoFile(url);
//			loadedLocalFile = true;
//		}
//		
//		
//		if (!loadedLocalFile) {
//			// Try to open the messages with no language code
//			url = Activator.getDefault().getBundle().getResource("po/messages.po");
//			loadPoFile(url);
//		}
		
	}
	
	/**
	 * Remove the trailing and leading quotes and unescape the string.
	 * 
	 * @param sin
	 * 			The input string
	 * @return
	 * 			The formated string
	 */
	static String format (String sin) {
		sin = sin.trim();
		
		//Remove leading quotes
		if (sin.startsWith("\""))
			sin = sin.substring(1);

		//Remove trailing quotes
		if (sin.endsWith("\""))
			sin = sin.substring(0,sin.length()-1);
		
		String sout = "";
		boolean escape = false;
		
		// Get character by character
		for (int i = 0; i< sin.length(); i++) {
			char c = sin.charAt(i);
			
			// Find the escape sequence
			if (c == '\\' && !escape)
				escape = true;
			else {
				if (escape) {
					
					// Replace the escape sequence
					if (c == '\'') sout += '\'';
					if (c == '\"') sout += '\"';
					if (c == '\\') sout += '\\';
					if (c == 'r') sout += '\r';
					if (c == 'n') sout += '\n';
					if (c == 'f') sout += '\f';
					if (c == 't') sout += '\t';
					if (c == 'b') sout += '\b';
					escape = false;
				}
				else {
					sout += c;
				}
			}
		}
		return sout;
	}

}
