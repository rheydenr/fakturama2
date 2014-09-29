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

package com.sebulli.fakturama.webshopimport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sebulli.fakturama.dao.ContactDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ProductCategoriesDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dao.ShippingCategoriesDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.migration.CategoryBuilder;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.misc.ProductUtil;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingCategory;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * Web shop import manager. This class provides the functionality to connect to
 * the web shop and import the data, which is transmitted as a XML File. This 
 * file is created by a connector, which is individual for each shop system.
 * Look at Fakturama download page for further information. 
 * The WebshopImporter creates the missing products, VATs and documents (orders in this case). 
 * 
 */
public class WebShopImportManager {

    @Inject
    @Translation
    private Messages msg;
    
    @Inject
    private Logger log;
    
    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;
    
//    @Inject
//    private IEclipseContext context;
    
//    @Inject
//    private VatCategoriesDAO vatCategoriesDAO;
    
    @Inject
    private VatsDAO vatsDAO;
    
    @Inject
    private DocumentsDAO documentsDAO;
    
    @Inject
    private ContactDAO contactsDAO;
    
    @Inject
    private ProductsDAO productsDAO;
    
    @Inject
    private ShippingCategoriesDAO shippingCategoriesDAO;
    
    @Inject
    private ShippingsDAO shippingsDAO;
    
    @Inject
    private PaymentsDAO paymentsDAO;
    
    @Inject
    private ProductCategoriesDAO productCategoriesDAO;
    
	/**
	 * Runs the reading of a http stream in an extra thread.
	 * So it can be interrupted by clicking the cancel button. 
	 * 
	 * @author Gerd Bartelt
	 */
	public class InterruptConnection implements Runnable {
	    
		// The connection 
		private URLConnection conn;
		
		// Reference to the input stream data
	    private InputStream inputStream = null;
	    
	    // true, if the reading was successful
	    private boolean isFinished = false;

	    // true, if there was an error
	    private boolean isError = false;
	    
	    
	    /**
	     * Constructor. Creates a new connection to use it in an extra thread
	     * 
	     * @param conn
	     * 			The connection
	     */
	    public InterruptConnection(URLConnection conn) {
	        this.conn = conn;
	    }

	    /**
	     * Return whether the reading was successful
	     * 
	     * @return
	     * 		True, if the stream was read completely
	     */
	    public boolean isFinished() {
	    	return isFinished;
	    }

	    /**
	     * Return whether the was an error
	     * 
	     * @return
	     * 		True, if there was an error
	     */
	    public boolean isError() {
	    	return isError;
	    }
	    
	    	    
	    /**
	     * Returns a reference to the input stream
	     * 
	     * @return
	     * 		Reference to the input stream
	     */
	    public InputStream getInputStream() {
	    	return inputStream;
	    }
	    
	    /**
	     * Start reading the input stream 
	     */
	    public void run() {
	        try {
	        	inputStream = conn.getInputStream();
	        	isFinished = true;
	        } catch (IOException e) {
	        	isError = true;
			}
	    }
	}

	
	// Data model
	private DocumentBuilderFactory factory = null;
	private DocumentBuilder builder = null;
	private org.w3c.dom.Document document = null;

	// The XML data
	private String importXMLContent = "";

	// List of all orders, which are out of sync with the web shop.
	private Properties orderstosynchronize = null;

	// The result of this import process
	private String runResult = "";

	// Imported data
//	private String shopSystem ="";
	private String shopURL = "";
	private String productImagePath = "";

	private int worked;

	// Configuration of the web shop request
	private boolean getProducts;
	private boolean getOrders;

    // true, if the product's EAN number is imported as item number
    private Boolean useEANasItemNr = false;

	/**
	 * Prepare the web shop import to request products and orders.
	 */
	public void prepareGetProductsAndOrders() {
		getProducts = true;
		getOrders = true;
	}

	/**
	 * Prepare the web shop import to change the state of an order.
	 */
	public void prepareChangeState() {
		getProducts = false;
		getOrders = false;
	}

	
	/**
	 * This is the central execution entry point for the Webshop import process.
	 * 
	 * @param parent
	 * @return
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	@Execute
	public ExecutionResult execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {
	    ExecutionResult result = null;
	    prepareGetProductsAndOrders();
        try {
            ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
            IRunnableWithProgress op = new WebShopImportWorker();
            progressMonitorDialog.run(true, true, op);
            result = new ExecutionResult(runResult, 0);
          }
          catch (InvocationTargetException e) {
              log.error(e, "Error running web shop import manager.");
              result = new ExecutionResult("Error running web shop import manager.", 1);
          }
          catch (InterruptedException e) {
              log.error(e, "Web shop import manager was interrupted.");
              result = new ExecutionResult("Web shop import manager was interrupted.", 2);
          }
        return result;
	}

	/**
	 * Save the list of all orders, which are out of sync with the web shop to
	 * file system
	 * 
	 */
	private void saveOrdersToSynchronize() {
		if (orderstosynchronize.isEmpty())
			return;

		try (Writer writer = new FileWriter(eclipsePrefs.get("GENERAL_WORKSPACE", "") + "/orders2sync.txt")) {
			orderstosynchronize.store(writer, "OrdersNotInSyncWithWebshop");
		}
		catch (IOException e) {
			e.printStackTrace();
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
		int webshopState;

		// Get the orders that are out of sync with the shop
		readOrdersToSynchronize();

		// Convert a percent value of 0..100% to a state of 1,2,3
		if (progress >= OrderState.SHIPPED.getState())
			webshopState = 3;
		else if (progress >= OrderState.PROCESSING.getState())
			webshopState = 2;
		else
			webshopState = 1;

		// Set the new progress state 
		// Add an "*" to mark the ID as "notify customer"
		String value = Integer.toString(webshopState);

		//Replace the "," by "&comma;
		comment = comment.replace("%2C", "%26comma%3B");
		//Replace the "=" by "&equal;
		comment = comment.replace("%3D", "%26equal%3B");
		
		if (notify)
			value += "*" + comment;

		orderstosynchronize.setProperty(orderId, value);
		saveOrdersToSynchronize();
	}

	/**
     * Read the list of all orders, which are out of sync with the web shop
     * from the file system
     * 
     */
    private void readOrdersToSynchronize() {
        orderstosynchronize = new Properties();
        try (Reader reader = new FileReader(eclipsePrefs.get("GENERAL_WORKSPACE", "") + "/orders2sync.txt")) {
            orderstosynchronize.load(reader);
        } catch (FileNotFoundException fnex) {
            log.warn(fnex, "file not found: orders2sync.txt (will be created next time)");
        } catch (IOException e) {
            log.error(e);
        }
    }

    class WebShopImportWorker implements IRunnableWithProgress {
	    private IProgressMonitor localMonitor;
	    
	    @Override
	    public void run(IProgressMonitor pMonitor) throws InvocationTargetException, InterruptedException  {
	        localMonitor = pMonitor;
	        
            runResult = "";
            
            // Get ULR, user name and password from the preference store
            String address = eclipsePrefs.get("WEBSHOP_URL", "");
            String user = eclipsePrefs.get("WEBSHOP_USER", "");
            String password = eclipsePrefs.get("WEBSHOP_PASSWORD", "");
            Integer maxProducts  = eclipsePrefs.getInt("WEBSHOP_MAX_PRODUCTS", 1);
            Boolean onlyModifiedProducts  = eclipsePrefs.getBoolean("WEBSHOP_ONLY_MODIFIED_PRODUCTS", true);
            useEANasItemNr  = eclipsePrefs.getBoolean("WEBSHOP_USE_EAN_AS_ITEMNR", true);
            Boolean useAuthorization = eclipsePrefs.getBoolean("WEBSHOP_AUTHORIZATION_ENABLED", false); 
            String authorizationUser = eclipsePrefs.get("WEBSHOP_AUTHORIZATION_USER", "");
            String authorizationPassword = eclipsePrefs.get("WEBSHOP_AUTHORIZATION_PASSWORD", "");
            
            // Check empty URL
            if (address.isEmpty()) {
                //T: Status message importing data from web shop
                runResult = msg.importWebshopErrorUrlnotset;
                return;
            }
            
            // Add "http://"
            if (!address.toLowerCase().startsWith("http://") 
                    && !address.toLowerCase().startsWith("https://") 
                    && !address.toLowerCase().startsWith("file://")) {
                address = "http://" + address;
            }
    
            try {
                // Get the open order IDs that are out of sync with the webshop
                // from the file system
                readOrdersToSynchronize();

                // Create a new document builder
                factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
            }
            catch (ParserConfigurationException e) {
                log.error(e, "Cannot parse ordersToSynchronize");
            }

            try {
    
                // Connect to web shop
                worked = 0;
                URLConnection conn = null;
                //T: Status message importing data from web shop
                localMonitor.beginTask(msg.importWebshopInfoConnection, 100);
                //T: Status message importing data from web shop
                localMonitor.subTask(msg.importWebshopInfoConnected + " " + address);
                setProgress(10);
                URL url = new URL(address);
                conn = url.openConnection();
                conn.setDoInput(true);
                conn.setConnectTimeout(4000);
                if (!address.toLowerCase().startsWith("file://")) {
                    
                    conn.setDoOutput(true);
    
                    // Use password for password protected web shops
                    if (useAuthorization) {
                        String encodedPassword = Base64Coder.encodeString(authorizationUser + ":" + authorizationPassword );
                        conn.setRequestProperty( "Authorization", "Basic " + encodedPassword );
                    }
    
                    // Send user name , password and a list of unsynchronized orders to
                    // the shop
                    OutputStream outputStream = null;
                    outputStream = conn.getOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                    setProgress(20);
                    String postString = "username=" + URLEncoder.encode(user, "UTF-8") + "&password=" +URLEncoder.encode(password, "UTF-8") ;
    
                    String actionString = "";
                    if (getProducts)
                        actionString += "_products";
                    if (getOrders)
                        actionString += "_orders";
                    if (!actionString.isEmpty())
                        actionString = "&action=get" + actionString;
    
                    postString += actionString;
    
                    postString += "&setstate=" + orderstosynchronize.toString();
                    
                    if (maxProducts > 0) {
                        postString += "&maxproducts=" + maxProducts.toString();
                    }
    
                    if (onlyModifiedProducts) {
                        String lasttime = eclipsePrefs.get("lastwebshopimport","");
                        if (! lasttime.isEmpty())
                            postString += "&lasttime=" + lasttime.toString();
                    }
                
                    log.debug("POST-String: " + postString);
                    writer.write(postString);
                    writer.flush();
                    writer.close();
    
                }
                String line;
                setProgress(30);
    
                // read the xml answer (the orders)
                importXMLContent = "";
                
                // Start a connection in an extra thread
                InterruptConnection interruptConnection = new InterruptConnection(conn);
                new Thread(interruptConnection).start();
                while (!localMonitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError());
    
                // If the connection was interrupted and not finished: return
                if (!interruptConnection.isFinished()) {
                    ((HttpURLConnection)conn).disconnect();
                    if (interruptConnection.isError()) {
                        //T: Status error message importing data from web shop
                        runResult = msg.importWebshopErrorCantconnect;
                    }
                    return;
                }
    
                // If there was an error, return with error message
                if (interruptConnection.isError()) {
                    ((HttpURLConnection)conn).disconnect();
                    //T: Status message importing data from web shop
                    runResult = msg.importWebshopErrorCantread;
                    return;
                }
                
                
                // Read the input stream
                BufferedReader reader = new BufferedReader(new InputStreamReader(interruptConnection.getInputStream()));
    
                //T: Status message importing data from web shop
                localMonitor.subTask(msg.importWebshopInfoLoading);
                double progress = worked;
    
                // Get the directory of the workspace
                String filename = eclipsePrefs.get("GENERAL_WORKSPACE", "");
    
                Path logFile = null;
                BufferedWriter logBuffer = null;
    
                // Do not save log files, of there is no workspace set
                if (!filename.isEmpty()) {
    
                    // Create a sub folder "Log", if it does not exist yet.
                    filename += "/Log/";
                    Path directory = Paths.get(filename);
                    if (!Files.isDirectory(directory)) {
                        Files.createDirectories(directory);
                    }
    
                    // Name of the log file
                    filename += "WebShopImport.log";
    
                    // Create a File object
                    logFile = Paths.get(filename);
    
                    // Create a new file
                    Files.deleteIfExists(logFile);
                    Files.createFile(logFile);
    
                    // Create a buffered writer to write the imported data to the file system
                    logBuffer = Files.newBufferedWriter(logFile);
                }
                    
                // Use string buffer for large content
                StringBuffer sb = new StringBuffer();
                
                // read line by line and set the progress bar
                while (((line = reader.readLine()) != null) && (!localMonitor.isCanceled())) {
                    
                    // Write the imported data to the log file
                    sb.append(line);
                    sb.append("\n");
    
                    // exponential function to 50%
                    progress += (50 - progress) * 0.01;
                    setProgress((int) progress);
                }
                
                // Convert the string buffer to a string
                importXMLContent = sb.toString();
    
                // Write the web shop log file
                if (logBuffer != null) {
                    logBuffer.write(importXMLContent);
                    logBuffer.close();
                }
    
                
                // parse the XML stream
                if (!localMonitor.isCanceled()) {
    
                    if (!importXMLContent.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
                        //T: Status message importing data from web shop
                        runResult = msg.importWebshopErrorNodata + "\n" + address +
                                    importXMLContent;
                        return;
                    }
    
                    
                    ByteArrayInputStream importInputStream = new ByteArrayInputStream(importXMLContent.getBytes());
                    //XMLErrorHandler errorHandler = new XMLErrorHandler();
                    //builder.setErrorHandler(errorHandler);
                    document = builder.parse(importInputStream);
                    
    
                    NodeList ndList = document.getElementsByTagName("webshopexport");
    
                    // Clear the list of orders to sync, if the data was sent
                    if (ndList.getLength() != 0) {
                        orderstosynchronize = new Properties();
                    }
                    else {
                        runResult = importXMLContent;
                    }
    
                    // Get the error elements and add them to the run result list
                    ndList = document.getElementsByTagName("error");
                    if (ndList.getLength() > 0) {
                        runResult = ndList.item(0).getTextContent();
                    }
                }
                // cancel the download process
                else {
                }
    
                reader.close();
    
                // Interpret the imported data (and load the product images)
                if (runResult.isEmpty()) {
                    // If there is no error - interpret the data.
                    interpretWebShopData(localMonitor);
                }
    
                // Store the time of now
                String now = DataUtils.DateAsISO8601String();
                eclipsePrefs.put("lastwebshopimport", now);
                
                localMonitor.done();
    
            }
            catch (SAXException e) {
                runResult = "Error parsing XML content:\n" +
                        e.getLocalizedMessage()+"\n"+
                        importXMLContent;
            }
            catch (Exception e) {
                //T: Status message importing data from web shop
                runResult = msg.importWebshopErrorCantopen + "\n" + address + "\n";
                runResult += "Message:" + e.getLocalizedMessage()+ "\n";
                if (e.getStackTrace().length > 0)
                    runResult += "Trace:" + e.getStackTrace()[0].toString()+ "\n";
                
                //runResult += e.getCause().getLocalizedMessage();
                if (!importXMLContent.isEmpty())
                    runResult += "\n\n" + importXMLContent;
                }
            }

	    /**
	     * Sets the progress of the job in percent
	     * 
	     * @param percent
	     */
	    void setProgress(int percent) {
	        if (percent > worked) {
	            localMonitor.worked(percent - worked);
	            worked = percent;
	        }
	    }

        /**
         * Interpret the complete node of all orders and import them
         * @throws SQLException 
         */
        private void interpretWebShopData(IProgressMonitor monitor) throws SQLException {
        
        	//shopSystem ="";
        	shopURL = "";
        	productImagePath = "";
        
        	// Mark all orders as "in sync with the web shop"
        	allOrdersAreInSync();
        
        	// There is no order
        	if (document == null)
        		return;
        
        	NodeList ndList;
        
        	// Get the general shop data
        	ndList = document.getElementsByTagName("webshop");
        	if (ndList.getLength() == 1) {
        		Node webshop = ndList.item(0);
        		//shopSystem = getAttributeAsString(webshop.getAttributes(), "shop");
        		shopURL = getAttributeAsString(webshop.getAttributes(), "url");
        	}
        
        	// Get the general products data
        	ndList = document.getElementsByTagName("products");
        	if (ndList.getLength() == 1) {
        		Node products = ndList.item(0);
        		productImagePath = getAttributeAsString(products.getAttributes(), "imagepath");
        	}
        
        	// Get all products and import them
        	ndList = document.getElementsByTagName("product");
        	for (int productIndex = 0; productIndex < ndList.getLength(); productIndex++) {
        		//T: Status message importing data from web shop
        		monitor.subTask(msg.importWebshopInfoLoading + " " + Integer.toString(productIndex + 1) + "/" + Integer.toString(ndList.getLength()));
        		setProgress(50 + 40 * (productIndex + 1) / ndList.getLength());
        		Node product = ndList.item(productIndex);
        		createProductFromXMLOrderNode(product);
        		
        		// Cancel the product picture import process
        		if ( monitor.isCanceled() )
        			return;
        	}
        
        	// Get order by order and import it
        	//T: Status message importing data from web shop
        	monitor.subTask(msg.importWebshopInfoImportorders);
        	setProgress(95);
        	ndList = document.getElementsByTagName("order");
        	for (int orderIndex = 0; orderIndex < ndList.getLength(); orderIndex++) {
        		Node order = ndList.item(orderIndex);
        		createOrderFromXMLOrderNode(order);
        	}
        
        	// Save the new list of orders that are not in synch with the shop
        	saveOrdersToSynchronize();
        	
        }

        /**
         * Mark all orders as "in sync" with the web shop
         */
        private void allOrdersAreInSync() {
        	orderstosynchronize = new Properties();
        	Path f = Paths.get(eclipsePrefs.get("GENERAL_WORKSPACE", ""), "/orders2sync.txt");
        	try {
                Files.deleteIfExists(f);
            }
            catch (IOException e) {
                log.error(e, "can't delete orders2sync.txt");
            }
        }

        /**
         * Get an attribute's value and return an empty string, if the attribute is
         * not specified
         * 
         * @param attributes
         *            Attributes node
         * @param name
         *            Name of the attribute
         * @return Attributes value
         */
        private String getAttributeAsString(NamedNodeMap attributes, String name) {
        	Attr attribute;
        	String value = "";
        	attribute = (Attr) attributes.getNamedItem(name);
        	if (attribute != null) {
        		value = attribute.getValue();
        	}
        	return value;
        }

        /**
         * Returns the text of a specified child node.
         * 
         * @param parentNode
         *            The parent node.
         * @param name
         *            Name of the child
         * @return The text, or an empty string
         */
        private String getChildTextAsString(Node parentNode, String name) {
        
        	String retVal = "";
        
        	// Search all child nodes and find the node with the name "name"
        	for (int index = 0; index < parentNode.getChildNodes().getLength(); index++) {
        		Node child = parentNode.getChildNodes().item(index);
        
        		// Node found
        		if (child.getNodeName().equals(name))
        			retVal = child.getTextContent();
        	}
        
        	return retVal;
        }

        /**
         * Get an attribute's value and return -1 if the attribute is
         * not specified
         * 
         * @param attributes
         *            Attributes node
         * @param name
         *            Name of the attribute
         * @return Attributes value
         */
        private int getAttributeAsID(NamedNodeMap attributes, String name) {
        	int id = -1;
        	String s = getAttributeAsString(attributes, name);
        	try {
        		if (!s.isEmpty()) {
        			id = Integer.valueOf(s);
        		}
        	}
        	catch (Exception e) {
        	}
        
        	return id;
        }

        /**
         * Parse an XML node and create a new order for each order entry
         * 
         * @param orderNode
         *            The node with the orders to import
         * @throws SQLException 
         */
        private void createOrderFromXMLOrderNode(Node orderNode) throws SQLException {
        
        	// Temporary variables to store the contact data which will be imported
        	String firstname;
//        	String id;
        	String genderString;
        	int genderInt = 0;
        	String deliveryGenderString;
        	int deliveryGenderInt = 0;
        	String lastname;
        	String company;
        	String street;
        	String zip;
        	String city;
        	String country;
        	String phone;
        	String email;
        	String suppliernumber;
        
        	// The delivery data
        	String delivery_firstname;
        	String delivery_lastname;
        	String delivery_company;
        	String delivery_street;
        	String delivery_zip;
        	String delivery_city;
        	String delivery_country;
        
        	// Item data
        	String itemQuantity;
        	String itemDescription;
        	String itemModel;
        	String itemName;
        	String itemGross;
        	String itemDiscount;
        	Double itemDiscountDouble = 0.0;
        	String itemCategory;
        	String itemVatpercent;
        	String itemVatname;
        	String itemQUnit;
        
        	// Order data
        	String webshopId;
        	String webshopDate;
        	// String order_status;
        	String paymentCode;
        	String paymentName;
        	// String currency;
        	String order_total;
        	Double order_totalDouble = 0.0;
        	String order_discount;
        	Double order_discountDouble = 0.0;
        	// Shipping data
        	String shipping_vatpercent;
        	String shipping_vatname;
        	String shipping_name;
        	String shipping_gross;
        
        	// Comments
        	String commentDate;
        	String comment;
        	String commentText;
        
        	// Remember the vat name, of there is no vat calculated
        	boolean noVat = true;
        	String noVatName = "";
        	
        	
        	// Get the attributes ID and date of this order
        	NamedNodeMap attributes = orderNode.getAttributes();
        	webshopId = getAttributeAsString(attributes, "id");
        	webshopDate = getAttributeAsString(attributes, "date");
        
        	// Check, if this order is still existing
        	Date calendarWebshopDate = DataUtils.getCalendarFromDateString(webshopDate).getTime();
            if(!documentsDAO.findDocumentByDocIdAndDocDate(DocumentType.ORDER, webshopId, calendarWebshopDate).isEmpty()) {
        		return;
        	}
        
        	// Create a new order
        	Document dataSetDocument = new Document();
        	dataSetDocument.setBillingType(BillingType.ORDER); // DocumentType.ORDER
        
        	// Set name, web shop order id and date
        	// order_status = getAttributeAsString(attributes,"status");
        	// currency = getAttributeAsString(attributes,"currency");
        	dataSetDocument.setName(webshopId);
        	dataSetDocument.setWebshopId(webshopId);
        	dataSetDocument.setWebshopDate(calendarWebshopDate);
        	dataSetDocument = documentsDAO.save(dataSetDocument);
        
        	NodeList childnodes = orderNode.getChildNodes();
            CategoryBuilder<ContactCategory> contactCatBuilder = new CategoryBuilder<ContactCategory>();
   
        	// First get all contacts. Normally there is only one
        	for (int childnodeIndex = 0; childnodeIndex < childnodes.getLength(); childnodeIndex++) {
        		Node childnode = childnodes.item(childnodeIndex);
        		attributes = childnode.getAttributes();
        
        		// Get the contact data
        		if (childnode.getNodeName().equalsIgnoreCase("contact")) {
//        			id = getAttributeAsString(attributes, "id");
        			genderString = getChildTextAsString(childnode, "gender");
        			firstname = getChildTextAsString(childnode, "firstname");
        			lastname = getChildTextAsString(childnode, "lastname");
        			company = getChildTextAsString(childnode, "company");
        			street = getChildTextAsString(childnode, "street");
        			zip = getChildTextAsString(childnode, "zip");
        			city = getChildTextAsString(childnode, "city");
        			country = getChildTextAsString(childnode, "country");
        			deliveryGenderString = getChildTextAsString(childnode, "delivery_gender");
        			delivery_firstname = getChildTextAsString(childnode, "delivery_firstname");
        			delivery_lastname = getChildTextAsString(childnode, "delivery_lastname");
        			delivery_company = getChildTextAsString(childnode, "delivery_company");
        			delivery_street = getChildTextAsString(childnode, "delivery_street");
        			delivery_zip = getChildTextAsString(childnode, "delivery_zip");
        			delivery_city = getChildTextAsString(childnode, "delivery_city");
        			delivery_country = getChildTextAsString(childnode, "delivery_country");
        			phone = getChildTextAsString(childnode, "phone");
        			email = getChildTextAsString(childnode, "email");
        			suppliernumber = getChildTextAsString(childnode, "suppliernumber");
        
        			// Convert a gender character "m" or "f" to the gender number 
        			// 1 or 2
        			if (genderString.equals("m"))
        				genderInt = 1;
        			if (genderString.equals("f"))
        				genderInt = 2;
        			if (deliveryGenderString.equals("m"))
        				deliveryGenderInt = 1;
        			if (deliveryGenderString.equals("f"))
        				deliveryGenderInt = 2;
        
        			// Get the category for new contacts from the preferences
        			String shopCategory = eclipsePrefs.get("WEBSHOP_CONTACT_CATEGORY", "");
        
        			// use existing contact, or create new one
        			Contact contact = new Contact();
        			if(StringUtils.isNotEmpty(shopCategory)) {
            			ContactCategory contactCat = contactCatBuilder.buildCategoryFromString(shopCategory, ContactCategory.class);
            			contact.addToCategories(contactCat);
        			}
        			
        			Address address = new Address();
        			Contact deliveryContact = new Contact();
        			Address deliveryAddress = new Address();
        
        			// set explicit the customers data
                    contact.setGender(genderInt);
                    contact.setFirstName(firstname);
                    contact.setName(lastname);
                    contact.setCompany(company);
                    contact.setPhone(phone);
                    contact.setEmail(email);
                    address.setStreet(street);
                    address.setZip(zip);
                    address.setCity(city);
                    String countryCode = LocaleUtil.findByName(country);
                    
                    address.setCountry(countryCode);
                    contact.setAddress(address);
                    contact.setSupplierNumber(suppliernumber);
        
                    deliveryContact.setGender(deliveryGenderInt);
                    deliveryContact.setFirstName(delivery_firstname);
                    deliveryContact.setName(delivery_lastname);
                    deliveryContact.setCompany(delivery_company);
                    deliveryAddress.setStreet(delivery_street);
                    deliveryAddress.setZip(delivery_zip);
                    deliveryAddress.setCity(delivery_city);
                    countryCode = LocaleUtil.findByName(delivery_country);
                    deliveryAddress.setCountry(countryCode);
                    deliveryContact.setAddress(deliveryAddress);
                    contact.getDeliveryContacts().add(deliveryContact);
                    contactsDAO.save(contact);
        
                    dataSetDocument.setContact(contact);
//                    dataSetDocument.setAddress(contact.getAddress(false)); // included in contact
//                    dataSetDocument.setDeliveryaddress(deliveryContact); // included in contact
                    dataSetDocument.setAddressFirstLine(ContactUtil.getNameWithCompany(deliveryContact, false));			
                }
        	}
        
        	// Get the comments
        	comment = "";
        	for (int childnodeIndex = 0; childnodeIndex < childnodes.getLength(); childnodeIndex++) {
        		Node childnode = childnodes.item(childnodeIndex);
        		attributes = childnode.getAttributes();
        
        		// Get the comment text
        		if (childnode.getNodeName().equalsIgnoreCase("comment")) {
        			commentDate = DataUtils.DateAndTimeAsLocalString(getAttributeAsString(attributes, "date"));
        			commentText = childnode.getTextContent();
        			if (!comment.isEmpty())
        				comment += "\n";
        
        			// Add the date
        			comment += commentDate + " :\n";
        			comment += commentText + "\n";
        		}
        	}
        
        	// Get all the items of this order
        	for (int childnodeIndex = 0; childnodeIndex < childnodes.getLength(); childnodeIndex++) {
        		Node childnode = childnodes.item(childnodeIndex);
        		attributes = childnode.getAttributes();
        
        		// Get the item data
        		if (childnode.getNodeName().equalsIgnoreCase("item")) {
        			itemQuantity = getAttributeAsString(attributes, "quantity");
        			itemGross = getAttributeAsString(attributes, "gross");
        			itemVatpercent = getAttributeAsString(attributes, "vatpercent");
        			itemDiscount = getAttributeAsString(attributes, "discount");
//        			productID = getAttributeAsID(attributes, "productid");
        			itemModel = getChildTextAsString(childnode, "model");
        			itemName = getChildTextAsString(childnode, "name");
        			itemCategory = getChildTextAsString(childnode, "category");
        			itemVatname = getChildTextAsString(childnode, "vatname");
        			itemQUnit = getChildTextAsString(childnode, "qunit");
        			
        			// Convert VAT percent value to a factor (100% -> 1.00)
        			Double vat_percentDouble = 0.0;
        			try {
        				vat_percentDouble = Double.valueOf(itemVatpercent).doubleValue() / 100;
        			}
        			catch (NumberFormatException e) {
        				log.error(e, String.format(msg.importWebshopErrorCantconvertnumber, 
        						vat_percentDouble, "(vat_percentDouble)" ));
        			}
        
        			// If one item has a vat value, reset the noVat flag
        			if (vat_percentDouble > 0.0)
        				noVat = false;
        			else {
        				// Use the vat name
        				if (noVatName.isEmpty() && !itemVatname.isEmpty())
        					noVatName = itemVatname;
        			}
        
        			// Calculate the net value of the price
        			Double priceNet = 0.0;
        			try {
        				priceNet = Double.valueOf(itemGross).doubleValue() / (1 + vat_percentDouble);
        			}
        			catch (NumberFormatException e) {
        			    log.error(e,String.format(msg.importWebshopErrorCantconvertnumber, 
        						+ priceNet + "(priceNet)" ));
        			}
        
        			// Add the VAT value to the data base, if it is a new one
        			VAT vat = new VAT();
        			// itemVatname, "", itemVatname, vat_percentDouble
        			vat.setName(itemVatname);
        			vat.setDescription(itemVatname);
        			vat.setTaxValue(vat_percentDouble);
        			vat = vatsDAO.addIfNew(vat);
        
        			// Import the item as a new product
        			Product product;
        
        			// Get the category of the imported products from the preferences
        			String shopCategory = eclipsePrefs.get("WEBSHOP_PRODUCT_CATEGORY", "");
        
        			// If the category is not set, use the shop category
        			if (!shopCategory.isEmpty())
        				if (!shopCategory.endsWith("/"))
        					shopCategory += "/";
        
        			// Use item name as item model, if model is empty
        			if (itemModel.isEmpty() && !itemName.isEmpty())
        				itemModel = itemName;
        
        			// Use item model as item name, if name is empty
        			if (itemName.isEmpty() && !itemModel.isEmpty())
        				itemName = itemModel;
        
        			// Import the product attributes
        			itemDescription = "";
        			for (int index = 0; index < childnode.getChildNodes().getLength(); index++) {
        				Node itemChild = childnode.getChildNodes().item(index);
        
        				// Get all attributes
        				if (itemChild.getNodeName().equals("attribute")) {
        					attributes = itemChild.getAttributes();
        					if (!itemDescription.isEmpty())
        						itemDescription += ", ";
        					itemDescription += getChildTextAsString(itemChild, "option") + ": ";
        					itemDescription += getChildTextAsString(itemChild, "value");
        				}
        			}
        
        			// Create a new product
        			product = new Product();
        			// itemName, itemModel, shopCategory + itemCategory, itemDescription, priceNet, vat, "", "", 1.0, productID, itemQUnit
        			product.setName(itemName);
        			product.setItemNumber(itemModel);
        			CategoryBuilder<ProductCategory> prodCatBuilder = new CategoryBuilder<ProductCategory>();
        			prodCatBuilder.buildCategoryFromString(shopCategory + itemCategory, ProductCategory.class);
        			
        			product.setDescription(itemDescription);
        			product.setPrice1(BigDecimal.valueOf(priceNet));
        
        			// Add the new product to the data base, if it's not existing yet
        			Product newOrExistingProduct = productsDAO.addIfNew(product);
        // TODO CHECK THIS!!! Was ist denn hier eigentlich gemeint???
        			// Get the picture from the existing product
        			product.setPictureName(newOrExistingProduct.getPictureName());
        
        			// Try to convert discount value to double
        			itemDiscountDouble = DataUtils.StringToDouble(itemDiscount);
        
        			// Add this product to the list of items
        			DocumentItem item = new DocumentItem();
        			//(Double.valueOf(itemQuantity), product, itemDiscountDouble);
        			item.setQuantity(Double.valueOf(itemQuantity));
        			item.setQuantityUnit(itemQUnit);
        			item.setProduct(product);
        			item.setItemRebate(itemDiscountDouble);
        			
                    // search for owning document
        			item.setOwningDocument(dataSetDocument);
        
        			// Update the modified item data
        			dataSetDocument.getItems().add(item);
                
        		}
        	}
        
        	// Get the shipping(s)
        	for (int childnodeIndex = 0; childnodeIndex < childnodes.getLength(); childnodeIndex++) {
        		Node childnode = childnodes.item(childnodeIndex);
        		attributes = childnode.getAttributes();
        
        		// Import the shipping data
        		if (childnode.getNodeName().equalsIgnoreCase("shipping")) {
        			shipping_name = getChildTextAsString(childnode, "name");
        			shipping_gross = getAttributeAsString(attributes, "gross");
        
        			shipping_vatpercent = getAttributeAsString(attributes, "vatpercent");
        			shipping_vatname = getChildTextAsString(childnode, "vatname");
        
        			// Get the VAT value as double
        			Double shippingvat_percentDouble = 0.0;
        			try {
        				shippingvat_percentDouble = Double.valueOf(shipping_vatpercent).doubleValue() / 100;
        			}
        			catch (NumberFormatException e) {
        				log.error(e, String.format(msg.importWebshopErrorCantconvertnumber, 
        						+ shippingvat_percentDouble + " (shippingvat_percentDouble)" ));
        			}
        
        			// Get the shipping gross value
        			Double shippingGross = 0.0;
        			try {
        				shippingGross = Double.valueOf(shipping_gross).doubleValue();
        			}
        			catch (NumberFormatException e) {
        				log.error(e,String.format(msg.importWebshopErrorCantconvertnumber, 
        						+ shippingGross + " (shippingGross)" ));
        			}
        
        			// Get the category of the imported shipping from the preferences
        			String shopCategory = eclipsePrefs.get("WEBSHOP_SHIPPING_CATEGORY", "");
        
        			VAT shippingvat = new VAT();
        			shippingvat.setName(shipping_vatname);
        			shippingvat.setDescription(shipping_vatname);
        			shippingvat.setTaxValue(shippingvat_percentDouble);
        			
        			// Add the VAT entry to the data base, if there is not yet one
        			// with the same values				
        			vatsDAO.addIfNew(shippingvat);
        
        			// Add the shipping to the data base, if it's a new shipping
        			Shipping shipping = new Shipping(); 
        			// shipping_name, shopCategory, shipping_name, shippingGross, vatId, 1
        			shipping.setName(shipping_name);
        			ShippingCategory newShippingCategory = shippingCategoriesDAO.findByName(shopCategory);
        			shipping.getCategories().add(newShippingCategory);
        			shipping.setDescription(shipping_name);
        			shipping.setShippingValue(shippingGross);
        			shipping.setShippingVat(shippingvat);
        			shipping.setAutoVat(Boolean.TRUE);
        			shipping = shippingsDAO.addIfNew(shipping);
        
        			// Set the document entries for the shipping
                    dataSetDocument.setShipping(shipping);
        			String s = "";
        
        			// Use the order ID of the web shop as customer reference for
        			// imports web shop orders
        			if (webshopId.length() <= 5)
        				s = "00000".substring(webshopId.length(), 5);
        			s += webshopId;
        			//T: Text of the web shop reference
        			dataSetDocument.setCustomerRef(msg.importWebshopInfoWebshopno + " " + s);
        		}
        	}
        
        	// Get the payment (s)
        	for (int childnodeIndex = 0; childnodeIndex < childnodes.getLength(); childnodeIndex++) {
        		Node childnode = childnodes.item(childnodeIndex);
        		attributes = childnode.getAttributes();
        
        		// Get the payment data
        		if (childnode.getNodeName().equalsIgnoreCase("payment")) {
        			order_discount = getAttributeAsString(attributes, "discount");
        			order_total = getAttributeAsString(attributes, "total");
        			paymentCode = getAttributeAsString(attributes, "type");
        			paymentName = getChildTextAsString(childnode, "name");
        
        			// Try to convert discount value to double
        			order_discountDouble = DataUtils.StringToDouble(order_discount);
        
        			// Get the value of the payment
        			order_totalDouble = DataUtils.StringToDouble(order_total);
        
        			// Add the payment to the data base, if it's a new one
        			Payment payment = new Payment();
        			payment.setName(paymentName);
        			payment.setDescription(paymentName + " (" + paymentCode + ")");
        			payment.setPaidText(msg.dataDefaultPaymentPaidtext);
        			paymentsDAO.addIfNew(payment);
        		}
        	}
        
        	// Set the progress of an imported order to 10%
        	dataSetDocument.setProgress(10);
        
        	// Set the document data
        	dataSetDocument.setWebshopDate(calendarWebshopDate);
        	comment = dataSetDocument.getMessage() + comment;
        	dataSetDocument.setMessage(comment);
        
//        	dataSetDocument.setStringValueByKey("items", itemString);
        	dataSetDocument.setItemsRebate(order_discountDouble);
        	dataSetDocument.setTotalValue(BigDecimal.valueOf(order_totalDouble));
        
        	// There is no VAT used
        	if (noVat) {
        		// Set the no-VAT flag in the document and use the name and description
        		
//        		dataSetDocument.setBooleanValueByKey("novat", true);
//        		dataSetDocument.setStringValueByKey("novatname", noVatName);
        
        		VAT v = vatsDAO.findByName(noVatName);
        		if (v != null)
        			dataSetDocument.setNoVatReference(v);
        	}
        	
        	// Update the data base with the new document data
        	documentsDAO.save(dataSetDocument);
        
        	// Re-calculate the document's total sum and check it.
        	// It must be the same total value as in the web shop
//        	dataSetDocument.calculate();
        	Double calcTotal = 0.0; //dataSetDocument.getSummary().getTotalGross().asDouble();
        
        	// If there is a difference, show a warning.
        	if (!DataUtils.DoublesAreEqual(order_totalDouble, calcTotal)) {
        		//T: Error message importing data from web shop
        		//T: Format: ORDER xx TOTAL SUM FROM WEB SHOP: xx IS NOT EQUAL TO CALCULATED ONE: xx. PLEASE CHECK
        		String error = msg.toolbarNewOrderName + ":";
        		error += " " + webshopId + "\n";
        		error += msg.importWebshopInfoTotalsum;
        		error += "\n" + DataUtils.DoubleToFormatedPriceRound(order_totalDouble) + "\n";
        		error += msg.importWebshopErrorTotalsumincorrect;
        		error += "\n" + DataUtils.DoubleToFormatedPriceRound(calcTotal) + "\n";
        		error += msg.importWebshopErrorTotalsumcheckit;
        		runResult = error;
        	}        
        }

        /**
        	 * Parse an XML node and create a new product for each product entry
        	 * 
        	 * @param productNode
        	 *            The node with the products to import
         * @throws SQLException 
        	 */
        	private void createProductFromXMLOrderNode(Node productNode) throws SQLException {
        
        		// Temporary variables to store the products data which will be imported
        		String productModel;
        		String productName;
        		String productCategory;
        		String productNet;
        		String productGross;
        		String productVatPercent;
        		String productVatName;
        		String productDescription;
        		String productImage;
        		String pictureName;
        		String productQuantity;
        		String productEAN;
        		String productQUnit;
        		int productID;
        		
        		// Get the attributes ID and date of this order
        		NamedNodeMap attributes = productNode.getAttributes();
        		productNet = getAttributeAsString(attributes, "net");
        		productGross = getAttributeAsString(attributes, "gross");
        		productVatPercent = getAttributeAsString(attributes, "vatpercent");
        		productQuantity = getAttributeAsString(attributes, "quantity");
        		productID = getAttributeAsID(attributes, "id");
        		productModel = getChildTextAsString(productNode, "model");
        		productName = getChildTextAsString(productNode, "name");
        		productCategory = getChildTextAsString(productNode, "category");
        		productVatName = getChildTextAsString(productNode, "vatname");
        		productImage = getChildTextAsString(productNode, "image");
        		productEAN = getChildTextAsString(productNode, "ean");
        		productQUnit = getChildTextAsString(productNode, "qunit");
        
        		// Get the product description as plain text.
        		productDescription = "";
        		for (int index = 0; index < productNode.getChildNodes().getLength(); index++) {
        			Node productChild = productNode.getChildNodes().item(index);
        			if (productChild.getNodeName().equals("short_description"))
        				productDescription += productChild.getTextContent();
        		}
        
        		// Convert VAT percent value to a factor (100% -> 1.00)
        		Double vatPercentDouble = 0.0;
        		try {
        			vatPercentDouble = Double.valueOf(productVatPercent).doubleValue() / 100;
        		}
        		catch (NumberFormatException e) {
        		}
        
        		// Convert the gross or net string to a double value
        		Double priceNet = 0.0;
        		try {
        
        			// Use the net string, if it is set
        			if (!productNet.isEmpty()) {
        				priceNet = Double.valueOf(productNet).doubleValue();
        			}
        
        			// Use the gross string, if it is set
        			if (!productGross.isEmpty()) {
        				priceNet = Double.valueOf(productGross).doubleValue() / (1 + vatPercentDouble);
        			}
        
        		}
        		catch (NumberFormatException e) {
        		}
        
        		// Add the VAT value to the data base, if it is a new one 
        		VAT vat = new VAT();
        		vat.setName(productVatName);
        		vat.setDescription(productVatName);
        		vat.setTaxValue(vatPercentDouble);
        	    try {
                    vat = vatsDAO.addIfNew(vat);
                }
                catch (SQLException e1) {
                    log.error(e1);
                }
        
        		// Import the item as a new product
        		Product product;
        
        		// Get the category of the imported products from the preferences
        		String shopCategory = eclipsePrefs.get("WEBSHOP_PRODUCT_CATEGORY", "");
        
        		// If the category is not set, use the shop category
        		if (!shopCategory.isEmpty())
        			if (!shopCategory.endsWith("/"))
        				shopCategory += "/";
        
        		// Use the EAN number
        		if (useEANasItemNr) {
        			if (!productEAN.isEmpty())
        				productModel = productEAN;
        		}
        		
        		// Use product name as product model, if model is empty
        		if (productModel.isEmpty() && !productName.isEmpty())
        			productModel = productName;
        
        		// Use product model as product name, if name is empty
        		if (productName.isEmpty() && !productModel.isEmpty())
        			productName = productModel;
        
        		pictureName = "";
        
        		// Create the URL to the product image
        		if (!productImage.isEmpty()) {
        			pictureName = ProductUtil.createPictureName(productName, productModel);
        		    String workspace = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null);
        			downloadImageFromUrl(localMonitor, shopURL + productImagePath + productImage, 
        			        workspace + Constants.PRODUCT_PICTURE_FOLDER, pictureName);
        		}
        
        		// Convert the quantity string to a double value
        		Double quantity = 1.0;
        		try {
        			quantity = Double.valueOf(productQuantity).doubleValue();
        		}
        		catch (NumberFormatException e) {
        		}
        
        		// Create a new product object
        		product = new Product();
        		product.setName(productName);
        		product.setItemNumber(productModel);
        		
        		CategoryBuilder<ProductCategory> prodCatBuilder = new CategoryBuilder<ProductCategory>();
        		ProductCategory productCategoryFromBuilder = prodCatBuilder.buildCategoryFromString(shopCategory + productCategory, ProductCategory.class);
        		
        		// save ProductCategory
        		productCategoryFromBuilder = productCategoriesDAO.addIfNew(productCategoryFromBuilder);
        		product.getCategories().add(productCategoryFromBuilder);
        		product.setDescription(productDescription);
        		product.setPrice1(BigDecimal.valueOf(priceNet));
        		product.setVat(vat);
        		product.setPictureName(pictureName);
        		product.setQuantity(quantity);
        		product.setWebshopId(Long.valueOf(productID));
        		product.setQuantityUnit(productQUnit);
        
        		// Add a new product to the data base, if it not exists yet	
        		Product existingProduct = productsDAO.findByExample(product);
                if (existingProduct == null) {
        			productsDAO.save(product);
        		}
        		else {
        			// Update data
        			existingProduct.getCategories().clear();
        			existingProduct.getCategories().addAll(product.getCategories());
        			existingProduct.setName(product.getName());
                    existingProduct.setItemNumber(product.getItemNumber());
                    existingProduct.setDescription(product.getDescription());
                    existingProduct.setPrice1(product.getPrice1());
                    existingProduct.setVat(product.getVat());
                    existingProduct.setPictureName(product.getPictureName());
                    existingProduct.setQuantity(product.getQuantity());
                    existingProduct.setWebshopId(product.getWebshopId());
                    existingProduct.setQuantityUnit(product.getQuantityUnit());
        
        			// Update the modified product data
        			productsDAO.save(existingProduct);
        		}
        
        	}

        /**
         * Download an image and save it to the file system
         * 
         * @param address
         *            The URL of the image
         * @param filePath
         *            The folder to store the image
         * @param fileName
         *            The filename of the image
         */
        private void downloadImageFromUrl(IProgressMonitor monitor, String address, String filePath, String fileName) {
        
        	// Cancel if address or filename is empty
        	if (address.isEmpty() || filePath.isEmpty() || fileName.isEmpty())
        		return;
        
        	// First of all check, if the output file already exists.
        	Path outputFile = Paths.get(filePath, fileName);
        	if (Files.exists(outputFile))
        		return;
        
            // Connect to the web server
            URI u = URI.create(address);
            try (InputStream in = u.toURL().openStream()) {
        
                // Create the destination folder to store the file
                if (!Files.isDirectory(Paths.get(filePath)))
                    Files.createDirectories(outputFile);
                Files.copy(in, outputFile);
            }
            catch (MalformedURLException e) {
                //T: Status message importing data from web shop
                log.error(e, msg.importWebshopErrorMalformedurl + " " + address);
            }
            catch (IOException e) {
                //T: Status message importing data from web shop
                log.error(e, msg.importWebshopErrorCantopenpicture + " " + address);
            }
        }

        /**
         * Convert the payment method to a readable (and localized) text.
         * 
         * @param intext
         *            order status
         * @return payment method as readable (and localized) text
         */
        private String getPaymentMethodText(String intext) {
        	String paymentstatustext = intext;
        
        	if (intext.equalsIgnoreCase("cod"))
        		paymentstatustext = msg.importWebshopDataCashondelivery;
        	else if (intext.equalsIgnoreCase("prepayment"))
        		paymentstatustext = msg.importWebshopDataPrepayment;
        	else if (intext.equalsIgnoreCase("creditcard"))
        		paymentstatustext = msg.importWebshopDataCreditcard;
        	else if (intext.equalsIgnoreCase("check"))
        		paymentstatustext = msg.importWebshopDataCheque;
        
        	return paymentstatustext;
        
        }

        /**
         * Remove the HTML tags from the result
         * 
         * @return The formated run result string
         */
        protected String getRunResult() {
        	return runResult.replaceAll("\\<.*?\\>", "");
        }
	 }

}

