/**
 * 
 */
package com.sebulli.fakturama.webshopimport;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

/**
 * WebShopConfigVO contains all the necessary information
 * for connection to a web shop.
 *
 */
public class WebShopConnector {
	protected String shopURL = "";
	protected String user;
	protected String password;
	protected Boolean useAuthorization;
	protected String authorizationUser;
	protected String authorizationPassword;
	

	public URLConnection createConnection() throws IOException {
	    URLConnection conn = null;
	    URL url = new URL(shopURL);
	    conn = url.openConnection();
	    conn.setDoInput(true);
	    conn.setConnectTimeout(4000);
	    if (!shopURL.toLowerCase().startsWith("file://")) {
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
	 * @return the shopURL
	 */
	public String getShopURL() {
		return shopURL;
	}
	/**
	 * @param shopURL the shopURL to set
	 */
	public WebShopConnector withShopURL(String shopURL) {
		this.shopURL = shopURL;
		return this;
	}
	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public WebShopConnector withUser(String user) {
		this.user = user;
		return this;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public WebShopConnector withPassword(String password) {
		this.password = password;
		return this;
	}
	/**
	 * @return the useAuthorization
	 */
	public Boolean getUseAuthorization() {
		return useAuthorization;
	}
	/**
	 * @param useAuthorization the useAuthorization to set
	 */
	public WebShopConnector withUseAuthorization(Boolean useAuthorization) {
		this.useAuthorization = useAuthorization;
		return this;
	}
	/**
	 * @return the authorizationUser
	 */
	public String getAuthorizationUser() {
		return authorizationUser;
	}
	/**
	 * @param authorizationUser the authorizationUser to set
	 */
	public WebShopConnector withAuthorizationUser(String authorizationUser) {
		this.authorizationUser = authorizationUser;
		return this;
	}
	/**
	 * @return the authorizationPassword
	 */
	public String getAuthorizationPassword() {
		return authorizationPassword;
	}
	/**
	 * @param authorizationPassword the authorizationPassword to set
	 */
	public WebShopConnector withAuthorizationPassword(String authorizationPassword) {
		this.authorizationPassword = authorizationPassword;
		return this;
	}
}
