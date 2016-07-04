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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.IParcelService;

/**
 * Loads the parcel service configurations files into properties lists.  
 * 
 * @author Gerd Bartelt
 *
 */
public class ParcelServiceManager implements IParcelService {
    
    @Inject
    @Translation
    private Messages msg;

    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;
	
	// All properties lists of all parcel services
	private List<Properties> propertiesList;
	
	// The active parcel service
	private int active = -1;
	
	public void setMsg(Messages msg) {
		this.msg = msg;
	}
	
	public void setPrefs(IEclipsePreferences pref) {
		this.eclipsePrefs = pref;
	}
	
	/**
	 * Loads all the parcel service lists from a specified path
	 * 
	 * @param templatePath
	 * 		The path where all the files are listed
	 */
	@PostConstruct
	public void initialize() {
		
		// Clear all, and create a new array list
		propertiesList = new ArrayList<Properties>();
		
		// Get the template path
		String templatePath = getTemplatePath();
		
		// Get the directory and find all files
		Path dir = Paths.get(templatePath);
		DirectoryStream<Path> newDirectoryStream;
        try {
            newDirectoryStream = Files.newDirectoryStream(dir);
            newDirectoryStream.forEach(file -> {
			
    			// It's used as a parcel service file, if it ends with a *.txt
    		    if(file.toString().endsWith(".txt")) {
    
    					// Load the file into a properties object
    					Properties properties = new Properties();
    					try(BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
    						Reader in = new InputStreamReader(stream, "UTF8");
    						properties.load(in);
    
    						// Use it only, if there is at least a name and a url key
    						if (properties.containsKey("name") && properties.containsKey("url")) {
    							propertiesList.add(properties);
    							
    							// Select this as active
    							active = propertiesList.size()-1;
    						}
    					} catch (IOException e) {
    					}
    		    }
		 });
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	/**
	 * Returns the template path for parcel service templates
	 * @return
	 */
	public String getTemplatePath () {

		String workspace = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, "");
		
		//T: Folder name of the parcel service. MUST BE ONE WORD 
		String templatePath = workspace + "/" + getRelativeTemplatePath();

		return templatePath;
	}
	
	/**
	 * Returns the template path for parcel service templates
	 * relative to the workspace
	 * @return
	 */
	@Override
    public String getRelativeTemplatePath () {
		return msg.configWorkspaceTemplatesName + "/" + msg.commandParcelserviceName + "/";
	}
	
	/**
	 * Getter for the number of properties entries
	 * 
	 * @return
	 * 	The number of properties entries
	 */
	@Override
	public int size() {
		return propertiesList.size();
	}
	
	/**
	 * Get the name of the parcel service
	 * 
	 * @param i
	 * 		The number of the properties object
	 * @return
	 * 		The name of the parcel service
	 */
	@Override
	public String getName(int i) {
		return propertiesList.get(i).getProperty("name");
	}

	/**
	 * Get the name of the active parcel service
	 * 
	 * @return
	 * 		The name of the active parcel service
	 */
	@Override
	public String getName() {
		return propertiesList.get(active).getProperty("name");
	}

	/**
	 * Get the URL of the parcel service
	 * 
	 * @param i
	 * 		The number of the properties object
	 * @return
	 * 		The URL of the parcel service
	 */
	public String getUrl(int i) {
		return propertiesList.get(i).getProperty("url");
	}

	/**
	 * Get the URL of the active parcel service
	 * 
	 * @return
	 * 		The URL of the active parcel service
	 */
	@Override
	public String getUrl() {
		return propertiesList.get(active).getProperty("url");
	}
	
	/**
	 * Set the active properties object
	 * 
	 * @param i
	 * 		Number of the properties object
	 */
	@Override
	public void setActive (int i) {
		if ( i< propertiesList.size() && i>=0)
			active = i;
	}
	
	/**
	 * Get the active properties object
	 * 
	 * @return
	 * 		The active properties object
	 */
	@Override
	public Properties getProperties () {
		return propertiesList.get(active);
	}
}
