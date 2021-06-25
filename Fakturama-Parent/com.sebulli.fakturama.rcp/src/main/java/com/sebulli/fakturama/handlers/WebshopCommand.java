package com.sebulli.fakturama.handlers;

/**
 * List of available commands for the WebshopConnector
 *
 */
public enum WebshopCommand {
    /**
     * Action for importing new orders and products from web shop.
     */
	/* getProducts = true, getOrder = true; */
	GET_PRODUCTS_AND_ORDERS_AND_SYNCHRONIZEORDERS, // default command
	
	/* getProducts = false, getOrder = false; */
	CHANGE_STATE,
	
    /**
     * Action for getting all the possible status from web shop.
     */
	GET_AVAILABLE_STATES,
	GET_SHOP_VERSION, UPDATE_PROGRESS
}