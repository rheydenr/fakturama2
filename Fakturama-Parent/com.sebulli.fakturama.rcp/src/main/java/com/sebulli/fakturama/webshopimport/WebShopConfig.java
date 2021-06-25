/**
 * 
 */
package com.sebulli.fakturama.webshopimport;

import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sebulli.fakturama.handlers.WebshopCommand;

/**
 * Contains all the necessary information
 * for connection to a web shop.
 *
 */
public class WebShopConfig {
	/**
	 * Main URL for the shop.
	 */
	protected String shopURL = "";
	protected String user;
	protected String password;
	protected Boolean useAuthorization;
	protected String authorizationUser;
	protected String authorizationPassword;
	
	// Configuration of the web shop request
	protected WebshopCommand webshopCommand;

    private Webshop selectedWebshop;
	
	/**
	 * The URL for shop connector.
	 */
	protected String scriptURL;

	// List of all orders which are out of sync with the web shop.
	private Properties orderstosynchronize = null;

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public WebShopConfig withUser(String user) {
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
	public WebShopConfig withPassword(String password) {
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
	public WebShopConfig withUseAuthorization(Boolean useAuthorization) {
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
	public WebShopConfig withAuthorizationUser(String authorizationUser) {
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
	public WebShopConfig withAuthorizationPassword(String authorizationPassword) {
		this.authorizationPassword = authorizationPassword;
		return this;
	}

	/**
	 * @return the getProducts
	 */
	public boolean isGetProducts() {
        return webshopCommand == WebshopCommand.GET_PRODUCTS_AND_ORDERS_AND_SYNCHRONIZEORDERS;
	}

	/**
	 * @return the getOrders
	 */
	public boolean isGetOrders() {
		return webshopCommand == WebshopCommand.GET_PRODUCTS_AND_ORDERS_AND_SYNCHRONIZEORDERS;
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
	public WebShopConfig withScriptURL(String scriptURL) {
		this.scriptURL = scriptURL;
		return this;
	}

	/**
	 * Prepare the web shop import to request products and orders.
	 */
	public void prepareGetProductsAndOrders() {
		setWebshopCommand(WebshopCommand.GET_PRODUCTS_AND_ORDERS_AND_SYNCHRONIZEORDERS);
	}

	/**
	 * Prepare the web shop import to change the state of an order.
	 */
	public void prepareChangeState() {
	    setWebshopCommand(WebshopCommand.CHANGE_STATE);
	}
	
    public void setWebshopCommand(WebshopCommand webshopCommand) {
        this.webshopCommand = webshopCommand;
    }

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	public WebShopConfig withCommand(WebshopCommand cmd) {
		this.webshopCommand = cmd;
		return this;
	}
    public WebshopCommand getWebshopCommand() {
        return webshopCommand;
    }
    
    public WebShopConfig withShopSystem(Webshop selectedShopsystem) {
        this.selectedWebshop = selectedShopsystem;
        return this;
    }
    
    public Webshop getSelectedWebshop() {
        return selectedWebshop;
    }

    /**
     * Create action string for HTTP requests.
     * 
     * @return
     */
    public String getActionString() {
        String actionString = "";
        if (isGetProducts()) {
            actionString  += "_products";
        }
        if (isGetOrders()) {
            actionString += "_orders";
        }
        return "get" + actionString;
    }

}
