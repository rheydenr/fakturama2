package com.sebulli.fakturama.webshopimport;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import javax.money.CurrencyUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.FakturamaModelFactory;

public abstract class AbstractWebshopImporter {

	// Imported data
//	private String shopSystem ="";
	protected String shopURL = "";
	protected String productImagePath = "";
	protected int   worked = 0;

	protected IProgressMonitor localMonitor;
	protected CurrencyUnit currencyCode;
	protected final FakturamaModelFactory fakturamaModelFactory = new FakturamaModelFactory();
	protected String user;
	protected String password;
	protected Boolean useAuthorization;
	protected String authorizationUser;
	protected String authorizationPassword;
	protected Messages msg;

	public AbstractWebshopImporter(IPreferenceStore preferences, Messages msg) {
		shopURL = preferences.getString(Constants.PREFERENCES_WEBSHOP_URL);
        user = preferences.getString(Constants.PREFERENCES_WEBSHOP_USER);
        password = preferences.getString(Constants.PREFERENCES_WEBSHOP_PASSWORD);
        useAuthorization = preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_ENABLED);
        authorizationUser = preferences.getString(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_USER);
        authorizationPassword = preferences.getString(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_PASSWORD);
        this.msg = msg;
	}

	public URLConnection createConnection(String address, boolean useAuthorization, String authorizationUser, String authorizationPassword) throws IOException {
	    URLConnection conn = null;
	    URL url = new URL(address);
	    conn = url.openConnection();
	    conn.setDoInput(true);
	    conn.setConnectTimeout(4000);
	    if (!address.toLowerCase().startsWith("file://")) {
	        conn.setDoOutput(true);
	
	        // Use password for password protected web shops
	        if (useAuthorization) {
	        	String encodedPassword = Base64.getEncoder().encodeToString((authorizationUser + ":" + authorizationPassword).getBytes());
	            conn.setRequestProperty( "Authorization", "Basic " + encodedPassword );
	        }
	    }
	    return conn;
	}

	/**
	 * Sets the progress of the job in percent
	 * 
	 * @param percent
	 */
	protected void setProgress(int percent) {
	    if (percent > worked) {
	        localMonitor.worked(percent - worked);
	        worked = percent;
	    }
	}
}