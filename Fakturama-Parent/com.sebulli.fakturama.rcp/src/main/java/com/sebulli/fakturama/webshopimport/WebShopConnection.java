/**
 * 
 */
package com.sebulli.fakturama.webshopimport;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sebulli.fakturama.handlers.WebshopCommand;

/**
 * WebShopConfigVO contains all the necessary information
 * for connection to a web shop.
 *
 */
public class WebShopConnection {
	/**
	 * Main URL for the shop.
	 */
	protected String shopURL = "";
	protected String user;
	protected String password;
	protected Boolean useAuthorization;
	protected String authorizationUser;
	protected String authorizationPassword;
	protected WebshopCommand webshopCommand;
	
	/**
	 * The URL for shop connector.
	 */
	protected String scriptURL;
	
	// Configuration of the web shop request
	private boolean getProducts, getOrders;

	// List of all orders which are out of sync with the web shop.
	private Properties orderstosynchronize = null;

	public URLConnection createConnection() throws IOException {
		
// hier abhängig vom Shopsystem und vom Command den richtigen API call bauen		
		
		
		
		
	    URLConnection conn = null;
	    URL url = new URL(scriptURL);
	    conn = url.openConnection();
	    conn.setDoInput(true);
	    conn.setConnectTimeout(4000);
	    if (!scriptURL.toLowerCase().startsWith("file://")) {
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
	 * @return the user
	 */
	public String getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public WebShopConnection withUser(String user) {
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
	public WebShopConnection withPassword(String password) {
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
	public WebShopConnection withUseAuthorization(Boolean useAuthorization) {
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
	public WebShopConnection withAuthorizationUser(String authorizationUser) {
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
	public WebShopConnection withAuthorizationPassword(String authorizationPassword) {
		this.authorizationPassword = authorizationPassword;
		return this;
	}

	/**
	 * @return the getProducts
	 */
	public boolean isGetProducts() {
		return getProducts;
	}

	/**
	 * @param getProducts the getProducts to set
	 */
	public void setGetProducts(boolean getProducts) {
		this.getProducts = getProducts;
	}

	/**
	 * @return the getOrders
	 */
	public boolean isGetOrders() {
		return getOrders;
	}

	/**
	 * @param getOrders the getOrders to set
	 */
	public void setGetOrders(boolean getOrders) {
		this.getOrders = getOrders;
	}

	/**
	 * @return the orderstosynchronize
	 */
	public Properties getOrderstosynchronize() {
		return orderstosynchronize;
	}

	/**
	 * @param orderstosynchronize the orderstosynchronize to set
	 */
	public void setOrderstosynchronize(Properties orderstosynchronize) {
		this.orderstosynchronize = orderstosynchronize;
	}

	/**
	 * @return the scriptURL
	 */
	public String getScriptURL() {
		return scriptURL;
	}

	/**
	 * @param scriptURL the scriptURL to set
	 */
	public void setShopURL(String shopURL) {
		this.shopURL = shopURL;
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
	public WebShopConnection withScriptURL(String scriptURL) {
		this.scriptURL = scriptURL;
		return this;
	}

	/**
	 * Prepare the web shop import to request products and orders.
	 */
	public void prepareGetProductsAndOrders() {
		setGetProducts(true);
		setGetOrders(true);
	}

	/**
	 * Prepare the web shop import to change the state of an order.
	 */
	public void prepareChangeState() {
		setGetProducts(false);
		setGetOrders(false);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	public WebShopConnection withCommand(WebshopCommand cmd) {
		this.webshopCommand = cmd;
		return this;
	}

}
