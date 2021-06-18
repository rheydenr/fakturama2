/**
 * 
 */
package com.sebulli.fakturama.webshopimport;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.Document;

/**
 * Handles synchronization of web shop orders with Fakturama database.
 *
 */
public class OrderSyncManager {

    @Inject @Optional
	private IPreferenceStore preferences;
    
    @Inject 
    private ILogger log;

	private WebShopConnection conn;

	/**
	 * file name for the temporary sync info file.
	 */
	public static final String FILENAME_ORDERS2SYNC = "orders2sync.txt";

    /**
     * Mark all orders as "in sync" with the web shop
     */
    public void allOrdersAreInSync() {
    	getConn().setOrderstosynchronize(new Properties());
    	Path f = Paths.get(preferences.getString(Constants.GENERAL_WORKSPACE), OrderSyncManager.FILENAME_ORDERS2SYNC);
    	try {
            Files.deleteIfExists(f);
        }
        catch (IOException e) {
            log.error(e, "can't delete " +  f.toString());
        }
    }

	/**
	 * Update the progress of an order.
	 * 
	 * @param uds
	 *            The UniDataSet with the new progress value
	 */
	public void updateOrderProgress(Document uds, String comment, boolean notify) {

		// Get the progress value of the UniDataSet
		String orderId = uds.getWebshopId();
		int progress = uds.getProgress();
		String webshopState;

		// Get the orders that are out of sync with the shop
		readOrdersToSynchronize();

		// Convert a percent value of 0..100% to a state of 1,2,3
		if (progress >= OrderState.SHIPPED.getState())
			webshopState = "3";
		else if (progress >= OrderState.PROCESSING.getState())
			webshopState = "2";
		else
			webshopState = "1";

		// Set the new progress state 
		// Add an "*" to mark the ID as "notify customer"

		//Replace the "," by "&comma;
		//Replace the "=" by "&equal;
		comment = StringUtils.replace(comment, "%2C", "%26comma%3B").replace("%3D", "%26equal%3B");
		if (notify)	webshopState += "*" + comment;

		getConn().getOrderstosynchronize().setProperty(orderId, webshopState);
		saveOrdersToSynchronize();
	}

	/**
     * Read the list of all orders, which are out of sync with the web shop
     * from the file system
     * 
     */
	public void readOrdersToSynchronize() {
		getConn().setOrderstosynchronize(new Properties());
		String generalWorkspace = preferences.getString(Constants.GENERAL_WORKSPACE);
        Path orders2sync = Paths.get(generalWorkspace, FILENAME_ORDERS2SYNC);
        try (InputStream reader = Files.newInputStream(orders2sync)) {
        	getConn().getOrderstosynchronize().load(reader);
        } catch (NoSuchFileException fnex) {
            // log.warn(fnex, "file not found: orders2sync.txt (will be created next time)");
        	// it's not really important...
        } catch (IOException e) {
            log.error(e);
        }
    }

	/**
	 * Save the list of all orders, which are out of sync with the web shop to
	 * file system
	 * 
	 */
	public void saveOrdersToSynchronize() {
		if (getConn().getOrderstosynchronize().isEmpty())
			return;
		
		Path orders2sync = Paths.get(preferences.getString(Constants.GENERAL_WORKSPACE), FILENAME_ORDERS2SYNC);

		try (Writer writer = Files.newBufferedWriter(orders2sync)) {
			getConn().getOrderstosynchronize().store(writer, "Orders not in sync with Webshop");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the conn
	 */
	public WebShopConnection getConn() {
		if(conn == null) {
			String scriptURL = preferences.getString(Constants.PREFERENCES_WEBSHOP_URL);
	        conn = new WebShopConnection()
	        		.withScriptURL(StringUtils.prependIfMissingIgnoreCase(scriptURL, "http://", "https://", "file://"))
	        		.withUseAuthorization(preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_ENABLED))
	        		.withAuthorizationUser(preferences.getString(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_USER))
	        		.withAuthorizationPassword(preferences.getString(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_PASSWORD))
	        		.withUser(preferences.getString(Constants.PREFERENCES_WEBSHOP_USER))
	        		.withPassword(preferences.getString(Constants.PREFERENCES_WEBSHOP_PASSWORD));

		}
		return conn;
	}

	/**
	 * @param conn the conn to set
	 */
	public void setConn(WebShopConnection conn) {
		this.conn = conn;
	}

}
