/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2017 The Fakturama Team
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.webshopimport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.javamoney.moneta.FastMoney;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.calculate.NumberGenerator;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ProductCategoriesDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dao.ShippingCategoriesDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dao.WebshopDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.migration.CategoryBuilder;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingCategory;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.WebshopStateMapping;
import com.sebulli.fakturama.parts.DebitorEditor;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.util.ProductUtil;
import com.sebulli.fakturama.webshopimport.type.AttributeType;
import com.sebulli.fakturama.webshopimport.type.CommentType;
import com.sebulli.fakturama.webshopimport.type.ContactType;
import com.sebulli.fakturama.webshopimport.type.ItemType;
import com.sebulli.fakturama.webshopimport.type.ObjectFactory;
import com.sebulli.fakturama.webshopimport.type.OrderType;
import com.sebulli.fakturama.webshopimport.type.PaymentType;
import com.sebulli.fakturama.webshopimport.type.ProductType;
import com.sebulli.fakturama.webshopimport.type.ProductsType;
import com.sebulli.fakturama.webshopimport.type.ShippingType;
import com.sebulli.fakturama.webshopimport.type.Webshopexport;

public class WebShopDataImporter implements IRunnableWithProgress {
	
	private static final String WEBSHOP_IMPORT_LOGFILE = "WebShopImport.log";

	private static final String PREFERENCE_LASTWEBSHOPIMPORT_DATE = "lastwebshopimport";

	@Inject
	@Translation
	private Messages msg;

	@Inject
	private IPreferenceStore preferences;
    
    @Inject 
    private Logger log;

    @Inject 
    private IEclipseContext context;

    @Inject 
    private VatsDAO vatsDAO;
    
    @Inject 
    private DocumentsDAO documentsDAO;
    
    @Inject 
    private ProductsDAO productsDAO;
    
    @Inject 
    private ContactsDAO contactsDAO;
    
    @Inject 
    private ShippingCategoriesDAO shippingCategoriesDAO;
    
    @Inject 
    private ShippingsDAO shippingsDAO;
    
    @Inject 
    private PaymentsDAO paymentsDAO;
    
	@Inject
	private WebshopDAO webshopStateMappingDAO;

    @Inject 
    private ProductCategoriesDAO productCategoriesDAO;
    
    @Inject
    private IDateFormatterService dateFormatterService;

    private OrderSyncManager orderSyncManager;

	private MathContext mathContext = new MathContext(5);

	private ProductUtil productUtil;
	
	private String generalWorkspace;
	
	@Inject
    private ILocaleService localeUtil;
    
	@Inject
	private INumberFormatterService numberFormatterService;

	
	private WebShopConnector connector;
	private String runResult = "";

	// true, if the product's EAN number is imported as item number
	private Boolean useEANasItemNr = false;
	private String productImagePath = "";
	private int worked = 0;

	private IProgressMonitor localMonitor;
	private CurrencyUnit currencyCode;
	private final FakturamaModelFactory fakturamaModelFactory = new FakturamaModelFactory();

	@PostConstruct
	public void init() {
		generalWorkspace = preferences.getString(Constants.GENERAL_WORKSPACE);
        orderSyncManager = ContextInjectionFactory.make(OrderSyncManager.class, context);
		useEANasItemNr = preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_USE_EAN_AS_ITEMNR);
        productUtil = ContextInjectionFactory.make(ProductUtil.class, context);
//    	this.localeUtil = ContextInjectionFactory.make(LocaleUtil.class, EclipseContextFactory.getServiceContext(Activator.getContext()));
	}

	@Override
    public void run(IProgressMonitor pMonitor) throws InvocationTargetException, InterruptedException  {
        if(connector == null) {
        	runResult = "no connection information provided";
        	return;
        }
        
        orderSyncManager.setConn(connector);

        Integer maxProducts = preferences.getInt(Constants.PREFERENCES_WEBSHOP_MAX_PRODUCTS);
        Boolean onlyModifiedProducts = preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS);
        localMonitor = pMonitor;
        setRunResult("");
		Webshopexport webshopexport = null;
        
		currencyCode = DataUtils.getInstance().getDefaultCurrencyUnit();
        String scriptUrl = connector.getScriptURL();
        
        // Check empty URL
        if (scriptUrl.isEmpty()) {
            //T: Status message importing data from web shop
        	setRunResult(msg.importWebshopErrorUrlnotset);
            return;
        }

        // Get the open order IDs that are out of sync with the webshop
		// from the file system. Store them in the WebShopConnector for further using.
        orderSyncManager.readOrdersToSynchronize();
        BufferedWriter logBuffer = null;

        try {
            // Connect to web shop
            worked = 0;
            //T: Status message importing data from web shop
            localMonitor.beginTask(msg.importWebshopInfoConnection, 100);
            //T: Status message importing data from web shop
            localMonitor.subTask(msg.importWebshopInfoConnected + " " + scriptUrl);
            setProgress(10);

            // Send user name, password and a list of unsynchronized orders to
            // the shop
	        URLConnection connection = connector.createConnection();
            if(connection != null && connection.getDoOutput()) {
            	OutputStream outputStream = connection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                setProgress(20);
                StringBuilder postStringSb = new StringBuilder("username=")
    					.append(URLEncoder.encode(connector.getUser(), "UTF-8"))
						.append("&password=")
						.append(URLEncoder.encode(connector.getPassword(), "UTF-8"));

                String actionString = "";
                if (connector.isGetProducts())
                    actionString += "_products";
                if (connector.isGetOrders())
                    actionString += "_orders";
                if (!actionString.isEmpty())
                    actionString = "&action=get" + actionString;

                postStringSb.append(actionString)
                	.append("&setstate=").append(connector.getOrderstosynchronize().toString());
				if (maxProducts > 0) {
                	postStringSb.append("&maxproducts=").append(maxProducts.toString());
                }

                if (onlyModifiedProducts) {
                    String lasttime = preferences.getString(PREFERENCE_LASTWEBSHOPIMPORT_DATE);
                    if (! lasttime.isEmpty()) {
						postStringSb.append("&lasttime=").append(lasttime.toString());
					}
                }
            
                log.debug("POST-String: " + postStringSb.toString());
                writer.write(postStringSb.toString());
                writer.flush();
                writer.close();
            }
            setProgress(30);
            
            // Start a connection in an extra thread
            InterruptConnection interruptConnection = new InterruptConnection(connection);
            new Thread(interruptConnection).start();
            while (!localMonitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError());

            // If the connection was interrupted and not finished: return
            if (!interruptConnection.isFinished()) {
                ((HttpURLConnection)connection).disconnect();
                if (interruptConnection.isError()) {
                    //T: Status error message importing data from web shop
                	setRunResult(msg.importWebshopErrorCantconnect);
                }
                return;
            }

            // If there was an error, return with error message
            if (interruptConnection.isError()) {
                ((HttpURLConnection)connection).disconnect();
                //T: Status message importing data from web shop
                setRunResult(msg.importWebshopErrorCantread);
                return;
            }
            
    		// 1. We need to create JAXBContext instance
    		JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

    		/* if we have larger documents we have to use SAX.         		*/
            // 2. create a new XML parser
//                SAXParserFactory factory = SAXParserFactory.newInstance();
//                factory.setNamespaceAware(true);
//                XMLReader reader = factory.newSAXParser().getXMLReader();
    		
    		// 2. Use JAXBContext instance to create the Unmarshaller.
    		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            //T: Status message importing data from web shop
            localMonitor.subTask(msg.importWebshopInfoLoading);

            // Get the directory of the workspace
            Path logFile = null;

            // Do not save log files if there is no workspace set
            if (!generalWorkspace.isEmpty()) {

                // Create a sub folder "Log" if it does not exist yet.
                // Name of the log file
                // Create a File object
                logFile = Paths.get(generalWorkspace, "Log", WEBSHOP_IMPORT_LOGFILE);
                if (!Files.isDirectory(logFile.getParent())) {
                    Files.createDirectories(logFile.getParent());
                }

                // Create a new file
//                    Files.deleteIfExists(logFile);
//                    Files.createFile(logFile);
//    
                // Create a buffered writer to write the imported data to the file system
                logBuffer = Files.newBufferedWriter(logFile, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            
            // 3. Use the Unmarshaller to unmarshal the XML document to get
            // an instance of JAXBElement.
            // 4. Get the instance of the required JAXB Root Class from the
            // JAXBElement.
			webshopexport = (Webshopexport) unmarshaller
    					.unmarshal(interruptConnection.getInputStream());
            
			// alternatively (for large responses)
            // prepare a Splitter
//                Splitter splitter = new Splitter(jaxbContext);

            // connect two components
//                reader.setContentHandler(splitter);
            
			// TODO surround with try-catch! This is the main part for reading the stream.
            // note that XMLReader expects an URL, not a file name.
            // so we need conversion.
//                reader.parse(new InputSource(interruptConnection.getInputStream()));
            
    		setProgress(40);
    		
    		// Write the web shop log file
            if (logBuffer != null) {
            	Marshaller marshaller = jaxbContext.createMarshaller(); 
            	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            	marshaller.marshal(webshopexport, logBuffer);
                logBuffer.close();
            }
            
            // parse the XML stream
            if (!localMonitor.isCanceled()) {
            	if(webshopexport.getWebshop() == null) {
                    //T: Status message importing data from web shop
            		setRunResult(msg.importWebshopErrorNodata + "\n" + scriptUrl);
                    return;
                }

                // Clear the list of orders to sync, if the data was sent
            	// NodeList ndList = document.getElementsByTagName("webshopexport");
				if (webshopexport.getOrders() != null) {
					connector.setOrderstosynchronize(new Properties());
				} else {
					setRunResult("import NOT ok");
				}

                // Get the error elements and add them to the run result list
                //ndList = document.getElementsByTagName("error");
                if (StringUtils.isNotEmpty(webshopexport.getError()) ) {
                	setRunResult(webshopexport.getError());
                }

                // Interpret the imported data (and load the product images)
                if (getRunResult().isEmpty()) {
                    // If there is no error - interpret the data.
                    interpretWebShopData(localMonitor, webshopexport);
                }
        
                // Store the time of now
                String now = dateFormatterService.DateAsISO8601String();
                preferences.putValue(PREFERENCE_LASTWEBSHOPIMPORT_DATE, now);
            }
            // else cancel the download process
            localMonitor.done();
        }
        catch (MarshalException mex) {
            //T: Status message importing data from web shop
        	setRunResult(msg.importWebshopErrorNodata + "\n" + scriptUrl + "\n" + mex.getMessage());
		}
        catch (Exception e) {
            //T: Status message importing data from web shop
        	setRunResult(msg.importWebshopErrorCantopen + "\n" + scriptUrl + "\n");
        	setRunResult(getRunResult()+"Message: " + e.getLocalizedMessage()+ "\n");
            if (e.getStackTrace().length > 0)
            	setRunResult(getRunResult()+"\nTrace: " + e.getStackTrace()[0].toString()+ "\n");

            if (webshopexport != null)
            	setRunResult(getRunResult()+"\n\n" + webshopexport);
            }
        finally {
        	if(logBuffer != null) {
        		try {
					logBuffer.close();
				} catch (IOException e) {
					log.error(e, String.format("couldn't close output stream for logfile '%s'.", WEBSHOP_IMPORT_LOGFILE));
				}
        	}
        }
    }

    /**
     * Interpret the complete node of all orders and import them
     * @param webshopexport 
     * @throws SQLException 
     */
    private void interpretWebShopData(IProgressMonitor monitor, Webshopexport webshopexport) throws FakturamaStoringException {
    
    	// There is no order
    	if (webshopexport == null) return;
    	
    	connector.setShopURL(webshopexport.getWebshop().getUrl());
    	productImagePath = "";
    
    	// Mark all orders as "in sync with the web shop"
    	orderSyncManager.allOrdersAreInSync();
    
    	// Get all products and import them
    	ProductsType products = webshopexport.getProducts();
    	// sometimes there are no products...
		if (products != null) {
			// Get the general products data
			productImagePath = products.getImagepath();

			List<ProductType> productList = products.getProduct();
			int producListSize = productList.size();
			for (int productIndex = 0; productIndex < producListSize; productIndex++) {
				// T: Status message importing data from web shop
				monitor.subTask(msg.importWebshopInfoLoading + " " + Integer.toString(productIndex + 1) + "/"
						+ Integer.toString(producListSize));
				setProgress(40 + 40 * (productIndex + 1) / producListSize);
				ProductType product = productList.get(productIndex);
				createProductFromXMLOrderNode(product);

				// Cancel the product picture import process
				if (monitor.isCanceled())
					return;
			}
		}
   
        	// Get order by order and import it
    	//T: Status message importing data from web shop
    	monitor.subTask(msg.importWebshopInfoImportorders);
    	setProgress(95);
        List<OrderType> orderList = webshopexport.getOrders().getOrder();
    	int orderListSize = orderList.size();
    	
    	// create some constants _before_ the loop begins
        ContactUtil contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);              
        Date today = Date.from(Instant.now());
    	for (int orderIndex = 0; orderIndex < orderListSize; orderIndex++) {
    		OrderType order = orderList.get(orderIndex);
    		createOrderFromXMLOrderNode(order, webshopexport.getWebshop().getLang(), contactUtil, today);
    	}
    
    	// Save the new list of orders that are not in sync with the shop
    	orderSyncManager.saveOrdersToSynchronize();
    }

    /**
     * Parse an XML node and create a new order for each order entry.
     * 
     * @param order
     *            The node with the orders to import
     * @param contactUtil {@link ContactUtil}
     * @param today the todays date
     * @param lang the language for interpreting the country code correctly
     * @throws SQLException if an error occurs while writing the data to the database
     */
    private void createOrderFromXMLOrderNode(OrderType order, String lang, ContactUtil contactUtil, Date today) throws FakturamaStoringException {
    	
		// Order data
		String webshopId;
		String webShopName = webshopStateMappingDAO.createWebShopIdentifier(preferences.getString(Constants.PREFERENCES_WEBSHOP_URL));
		String webshopDate;

		// Comments
		LocalDateTime commentDate;
		StringBuilder comment = new StringBuilder();
		String commentText;

		// Item data
		String itemModel = "";
		String itemName = "";
		StringBuffer itemDescription;

		// Remember the vat name, of there is no vat calculated
    	boolean noVat = true;
    	String noVatName = "";
    	
    	// Get the attributes ID and date of this order
    	webshopId = order.getId();
    	webshopDate = order.getDate();
    
    	// Check, if this order is still existing
    	// date="2011-08-04 15:35:52"
    	LocalDateTime calendarWebshopDate = LocalDateTime.parse(webshopDate, DateTimeFormatter.ISO_DATE_TIME);
        if(!documentsDAO.findByDocIdAndDocDate(DocumentType.ORDER, webshopId, calendarWebshopDate).isEmpty()) {
    		return;
    	}
    
    	// Create a new order
        Document dataSetDocument = fakturamaModelFactory.createOrder();
    	dataSetDocument.setBillingType(BillingType.ORDER); // DocumentType.ORDER
    
    	// Set name, web shop order id and date
    	// currency = order.getCurrency();
    	dataSetDocument.setName(webshopId);
    	dataSetDocument.setWebshopId(webshopId);
    	Instant instant = calendarWebshopDate.atZone(ZoneId.systemDefault()).toInstant();
    	dataSetDocument.setWebshopDate(Date.from(instant));
    	dataSetDocument.setValidFrom(Date.from(instant));
    
        CategoryBuilder<ContactCategory> contactCatBuilder = new CategoryBuilder<>(log);
   
        // First get all contacts. Normally there is only one
        ContactType contact = order.getContact();        

		Contact contactItem = fakturamaModelFactory.createDebitor();
		
		// Convert a gender character "m" or "f" to the gender number 
		// 1 or 2
	    contactItem.setGender(contactUtil.getGenderIdFromString(contact.getGender()));
		
		contactItem.setValidFrom(Date.from(instant));

		// Get the category for new contacts from the preferences
		String shopCategory = preferences.getString(Constants.PREFERENCES_WEBSHOP_CONTACT_CATEGORY);
		if(StringUtils.isNotEmpty(shopCategory)) {
			ContactCategory contactCat = contactCatBuilder.buildCategoryFromString(shopCategory, ContactCategory.class);
			// later on we have more than one category per contact
//    			contactItem.addToCategories(contactCat);
            contactItem.setCategories(contactCat);
		}
		
		// set explicitly the customers data
		// only set the number if it's not empty
		// see Bug FAK-510
		if(StringUtils.isNotBlank(contact.getId())) {
		    contactItem.setCustomerNumber(contact.getId());
		}
        contactItem.setFirstName(contact.getFirstname());
        contactItem.setName(contact.getLastname());
        contactItem.setCompany(contact.getCompany());
        contactItem.setPhone(contact.getPhone());
        contactItem.setEmail(contact.getEmail());
        contactItem.setWebshopName(contact.getWebshopName());
		contactItem.setValidFrom(today);
        
        Address address = fakturamaModelFactory.createAddress();
        address.setStreet(contact.getStreet());
        address.setZip(contact.getZip());
        address.setCity(contact.getCity());
        address.setValidFrom(today);
        String countryCode = localeUtil.findCodeByDisplayCountry(contact.getCountry(), lang);
        address.setCountryCode(countryCode);
        
        contactItem.setAddress(address);
        contactItem = contactsDAO.findOrCreate(contactItem);
        // Attention: If the contact is new then we have to create a new number for it!
        if(StringUtils.isBlank(contactItem.getCustomerNumber())) {
    		NumberGenerator numberProvider = ContextInjectionFactory.make(NumberGenerator.class, context);
            numberProvider.setEditorID(DebitorEditor.class.getSimpleName());
            String nextNr = numberProvider.getNextNr();
            contactItem.setCustomerNumber(nextNr);
            contactItem = contactsDAO.update(contactItem);
            numberProvider.setNextFreeNumberInPrefStore(nextNr);
        }
//            contactItem.setSupplierNumber(contact.get???); ==> is not transferred from connector!!!

        Address deliveryAddress = fakturamaModelFactory.createAddress();
        deliveryAddress.setStreet(contact.getDeliveryStreet());
        deliveryAddress.setZip(contact.getDeliveryZip());
        deliveryAddress.setCity(contact.getDeliveryCity());
        deliveryAddress.setValidFrom(today);
        countryCode = localeUtil.findCodeByDisplayCountry(contact.getDeliveryCountry(), lang);
        deliveryAddress.setCountryCode(countryCode);
        
        // if delivery contact is equal to main contact we don't need to persist it
        if (!address.isSameAs(deliveryAddress) 
                || !StringUtils.equals(contact.getDeliveryGender(), contact.getGender())
                || !StringUtils.equals(contact.getDeliveryFirstname(), contactItem.getFirstName())
                || !StringUtils.equals(contact.getDeliveryLastname(), contactItem.getName())
                || !StringUtils.equals(contact.getDeliveryCompany(), contactItem.getCompany())) {
            Contact deliveryContact = fakturamaModelFactory.createDebitor();

            deliveryContact.setFirstName(contact.getDeliveryFirstname());
            deliveryContact.setName(contact.getDeliveryLastname());
            deliveryContact.setCompany(contact.getDeliveryCompany());

            if (contact.getDeliveryGender().equals("m"))
                deliveryContact.setGender(Integer.valueOf(1));
            if (contact.getDeliveryGender().equals("f"))
                deliveryContact.setGender(Integer.valueOf(2));
            
            deliveryContact.setValidFrom(Date.from(instant));

            deliveryContact.setAddress(deliveryAddress);
            contactItem.setAlternateContacts(deliveryContact);
            contactItem = contactsDAO.update(contactItem);
        }
    
        dataSetDocument.setBillingContact(contactItem);
//            dataSetDocument.setAddress(contactItem.getAddress(false)); // included in contact
//            dataSetDocument.setDeliveryaddress(deliveryContact); // included in contact
        dataSetDocument.setAddressFirstLine(contactUtil.getNameWithCompany(contactItem));			
    
    	// Get the comments
    	for (CommentType commentType : order.getComments()) {
    		// Get the comment text
    		if(commentType.getDate() != null) {
    			commentDate = LocalDateTime.parse(commentType.getDate(), DateTimeFormatter.ISO_DATE_TIME);
    		} else {
    			commentDate = null;
    		}
			commentText = commentType.getTextcontent();
			if (comment.length() > 0) {
				comment.append('\n');
			}
			// Add the date
			comment.append(commentDate).append(" :\n");
			comment.append(commentText).append("\n");
    	}
    
    	// Get all the items of this order
    	int itemIndex = 1;
    	for (ItemType itemType : order.getItem()) {
    	    itemModel = itemType.getModel();
    	    itemName = itemType.getName();
    	    
			// Convert VAT percent value to a factor (100% -> 1.00)
			Double vatPercent = NumberUtils.DOUBLE_ZERO;
			try {
				vatPercent = Double.valueOf(itemType.getVatpercent()).doubleValue() / 100;
			}
			catch (NumberFormatException e) {
				log.error(e, String.format(msg.importWebshopErrorCantconvertnumber, 
						vatPercent, " (vatPercent)" ));
			}

			// If one item has a vat value, reset the noVat flag
			if (vatPercent.compareTo(NumberUtils.DOUBLE_ZERO) > 0) {
				noVat = false;
			} else {
				// Use the vat name
				if (noVatName.isEmpty() && !itemType.getVatname().isEmpty()) {
					noVatName = itemType.getVatname();
				}
			}

			// Calculate the net value of the price
			MonetaryAmount priceGross = FastMoney.of(itemType.getGross(), currencyCode);
			MonetaryAmount priceNet = priceGross.divide(1 + vatPercent);

            // Add the VAT value to the data base, if it is a new one
			VAT vat = getOrCreateVAT(itemType.getVatname(), vatPercent);

			// Get the category of the imported products from the preferences
			shopCategory = preferences.getString(Constants.PREFERENCES_WEBSHOP_PRODUCT_CATEGORY);
			shopCategory = StringUtils.appendIfMissing(shopCategory, "/", "/");

            // Import the item as a new product
			// Use item name as item model, if model is empty
			if (StringUtils.isBlank(itemType.getModel()) && StringUtils.isNotBlank(itemType.getName())) {
				itemModel = itemType.getName();
			}

			// Use item model as item name, if name is empty
			if (StringUtils.isNotBlank(itemType.getModel()) && StringUtils.isBlank(itemType.getName())) {
				itemName = itemType.getModel();
			}

			// Import the product attributes
			itemDescription = new StringBuffer();
			// store additional prices for attributes
			/*
			 * Currently, there's no possibility for storing prices of attributes / optional features.
			 * Therefore we only can put the attribute string as description into a product.
			 * The price and the prefix are ignored, since I don't know where I have to store it.
			 * A model change is required.
			 */
//    			Float attrPrice = NumberUtils.FLOAT_ZERO;
			StringBuilder prefixSb = new StringBuilder();
			for (AttributeType attribute : itemType.getAttribute()) {
				// Get all attributes
				if (itemDescription.length() > 0) {
					itemDescription.append(", ");
				}
				itemDescription.append(attribute.getOption()).append(": ")
				               .append(attribute.getValue());

// TODO implement!
//					attrPrice += attribute.getPrice();
//					if(StringUtils.isNotBlank(attribute.getPrefix())) {
//					    prefixSb.append("ATTR_").append(attribute.getPrefix()).append(";");
//					}
			}

			// Create a new product
			Product product = fakturamaModelFactory.createProduct();
			// OLD call: itemName, itemModel, shopCategory + itemCategory, itemDescription, priceNet, vat, "", "", 1.0, productID, itemQUnit
			product.setName(itemName);
			product.setItemNumber(itemModel);
            ProductCategory productCategory = productCategoriesDAO.getCategory(shopCategory + itemType.getCategory(), true);
			product.setCategories(productCategory);
			
			product.setDescription(itemDescription.toString());
			product.setPrice1(priceNet.getNumber().numberValue(Double.class));
			product.setVat(vat);
            // item.setTara?
			// ProductOptions?
			product.setValidFrom(today);
			//product.setProductId(itemType.getProductid());

			// Add the new product to the data base, if it's not existing yet
			Product newOrExistingProduct = productsDAO.findOrCreate(product);
			// Get the picture from the existing product  ==> TODO WHY???
//    			product.setPictureName(newOrExistingProduct.getPictureName());

			// Add this product to the list of items
			DocumentItem item = fakturamaModelFactory.createDocumentItem();
			item.setPosNr(itemIndex++);
			/*
			 * per default some other values are set from product
    this(-1, product.getStringValueByKey("name"), product.getIntValueByKey("id"), product.getStringValueByKey("itemnr"), false, "", -1, false, quantity,
            product.getStringValueByKey("description"), product.getPriceByQuantity(quantity), product.getIntValueByKey("vatid"), discount, 0.0, "", "", false,
            product.getStringValueByKey("picturename"), false, product.getStringValueByKey("qunit"));

			 */
			item.setName(newOrExistingProduct.getName());
			item.setItemNumber(newOrExistingProduct.getItemNumber());
			item.setDescription(newOrExistingProduct.getDescription() + prefixSb.toString());
			item.setQuantity(Double.valueOf(itemType.getQuantity()));
			item.setQuantityUnit(StringUtils.isBlank(itemType.getQunit()) ? newOrExistingProduct.getQuantityUnit() : itemType.getQunit());
			item.setValidFrom(today);
			item.setProduct(newOrExistingProduct);
			item.setPicture(newOrExistingProduct.getPicture());
			item.setItemVat(vat);
			item.setItemType(com.sebulli.fakturama.model.ItemType.POSITION);
			item.setPrice(productUtil.getPriceByQuantity(newOrExistingProduct, item.getQuantity()));
			// add prices from attributes
			// TODO cannot get option price from an item (it has no such field)
			// item.setPrice(item.getPrice() + attrPrice * item.getQuantity());
			
			if(itemType.getDiscount() != null) {
    			double discount = new BigDecimal(itemType.getDiscount()).round(mathContext).doubleValue();
    			item.setItemRebate(discount);
			}
            // search for owning document
//    			item.setOwningDocument((CustomDocument) dataSetDocument);

			// Update the modified item data
			dataSetDocument.addToItems(item);
    	}
    	
    	// Get the shipping(s)
    	ShippingType shippingType = order.getShipping();
		// Import the shipping data
		if (shippingType != null) {
			// Get the VAT value as double
			Double shippingVatPercent = NumberUtils.DOUBLE_ZERO;
			shippingVatPercent = Double.valueOf(shippingType.getVatpercent()).doubleValue() / 100;

			// Get the shipping gross value
			Double shippingGross = Double.valueOf(shippingType.getGross());

			// Get the category of the imported shipping from the preferences
			shopCategory = preferences.getString(Constants.PREFERENCES_WEBSHOP_SHIPPING_CATEGORY);   
			VAT shippingvat = getOrCreateVAT(shippingType.getVatname(), shippingVatPercent);//vatsDAO.findOrCreate(shippingvat);

			// Add the shipping to the data base, if it's a new shipping
			Shipping shipping = fakturamaModelFactory.createShipping();
			shipping.setName(shippingType.getName());
			ShippingCategory newShippingCategory = shippingCategoriesDAO.getCategory(shopCategory, true);
//    			shipping.addToCategories(newShippingCategory);
			shipping.setCategories(newShippingCategory);
			shipping.setDescription(shippingType.getName());
			shipping.setShippingValue(shippingGross);
			shipping.setShippingVat(shippingvat);
			shipping.setAutoVat(ShippingVatType.SHIPPINGVATFIX);
			shipping.setValidFrom(today);
			shipping = shippingsDAO.findOrCreate(shipping);

			// Set the document entries for the shipping
            dataSetDocument.setShipping(shipping);
            dataSetDocument.setShippingAutoVat(ShippingVatType.SHIPPINGVATFIX);
            dataSetDocument.setShippingValue(shippingGross);
			String webShopNo = msg.importWebshopInfoWebshopno + " ";

			// Use the order ID of the web shop as customer reference for
			// the import of web shop orders
			webShopNo += StringUtils.leftPad(webshopId, 5, '0');
			//T: Text of the web shop reference
			dataSetDocument.setCustomerRef(webShopNo);
		}
    
    	// Get the payment(s)
		PaymentType paymentType = order.getPayment();
		if (paymentType != null) {
			// Add the payment to the data base, if it's a new one
			Payment payment = fakturamaModelFactory.createPayment();
//    			payment.setCode(Constants.TAX_DEFAULT_CODE);
			payment.setName(paymentType.getName());
			payment.setDescription(paymentType.getName() + " (" + paymentType.getType() + ")");
			payment.setPaidText(msg.dataDefaultPaymentPaidtext);
			payment = paymentsDAO.findOrCreate(payment);  // here the validFrom is also set
        	dataSetDocument.setPayment(payment);
		}
    
    	// Set the progress of an imported order to "pending"
    	Optional<WebshopStateMapping> mappedStatus = webshopStateMappingDAO.findOrderState(webShopName, order.getStatus());
    	if(mappedStatus.isPresent()) {
    		dataSetDocument.setProgress(OrderState.valueOf(mappedStatus.get().getFakturamaOrderState()).getState());
    	} else {
    		dataSetDocument.setProgress(OrderState.PENDING.getState());
    	}
    
    	// Set the document data.
    	// since we import "Orders" the order date is set here
    	dataSetDocument.setOrderDate(Date.from(calendarWebshopDate.toInstant(ZoneOffset.UTC))); 
    	dataSetDocument.setDocumentDate(Date.from(instant));
    	dataSetDocument.setDateAdded(today);
    	dataSetDocument.setMessage(StringUtils.defaultString(dataSetDocument.getMessage()) + comment.toString());
    	if(paymentType != null) {
    	    dataSetDocument.setItemsRebate(paymentType.getDiscount() != null ? paymentType.getDiscount().doubleValue() : NumberUtils.DOUBLE_ZERO);
        	dataSetDocument.setTotalValue(paymentType.getTotal().doubleValue());
    	} else {
            dataSetDocument.setItemsRebate(NumberUtils.DOUBLE_ZERO);
            dataSetDocument.setTotalValue(NumberUtils.DOUBLE_ZERO);
    	}
    	dataSetDocument.setPaidValue(NumberUtils.DOUBLE_ZERO);
    	dataSetDocument.setPaid(Boolean.FALSE);
    
    	// There is no VAT used
    	if (noVat) {
    		// Set the no-VAT flag in the document and use the name and description
    		VAT noVatReference = vatsDAO.findByName(noVatName);
    		if (noVatReference != null) {
    			dataSetDocument.setNoVatReference(noVatReference);
    		}
    	}
    	
    	// Update the data base with the new document data
    	dataSetDocument = documentsDAO.save(dataSetDocument);
    
    	// Re-calculate the document's total sum and check it.
    	// It must be the same total value as in the web shop
//        	dataSetDocument.calculate();
    	DocumentSummary summary = new DocumentSummaryCalculator(currencyCode).calculate(dataSetDocument);
		MonetaryAmount calcTotal = summary.getTotalGross();
		MonetaryAmount totalFromWebshop = Money.of(paymentType != null ? paymentType.getTotal() : NumberUtils.DOUBLE_ZERO, currencyCode);
		totalFromWebshop = DataUtils.getInstance().getDefaultRounding().apply(totalFromWebshop);
    	// If there is a difference, show a warning.
    	if (!calcTotal.isEqualTo(totalFromWebshop)) {
    		//T: Error message importing data from web shop
    		//T: Format: ORDER xx TOTAL SUM FROM WEB SHOP: xx IS NOT EQUAL TO CALCULATED ONE: xx. PLEASE CHECK
    		String error = MessageFormat.format(msg.toolbarNewOrderName + ": " + webshopId + "\n"
    		+ msg.importWebshopErrorTotalsumincorrect, numberFormatterService.DoubleToFormatedPriceRound(paymentType.getTotal().doubleValue()),
    		numberFormatterService.formatCurrency(calcTotal));
    		setRunResult(error);
    	}        
    }

    /**
     * Add the VAT value to the data base, if it is a new one.
     * 
     * @param itemType
     * @param vatPercent
     * @return
     * @throws SQLException
     */
    private VAT getOrCreateVAT(String vatName, Double vatPercent) {
        VAT vat = fakturamaModelFactory.createVAT();
        vat.setName(vatName);
        vat.setDescription(vatName);
        vat.setTaxValue(vatPercent);
        vat.setValidFrom(new Date());
        try {
            vat = vatsDAO.addIfNew(vat);
        }
        catch (FakturamaStoringException e1) {
            log.error(e1);
        }
        return vat;
    }

    /**
     * Parse an XML node and create a new product for each product entry
     * 
     * @param product
     *            The node with the products to import
     * @throws SQLException
     */
    private void createProductFromXMLOrderNode(ProductType product) throws FakturamaStoringException {
        // Get the product description as plain text.
        String productDescription = product.getShortDescription();
        String productModel = product.getModel();
        String productName = product.getName();
        // Convert VAT percent value to a factor (100% -> 1.00)
        Double vatPercentDouble = NumberUtils.DOUBLE_ZERO;
        vatPercentDouble = Double.valueOf(product.getVatpercent()).doubleValue() / 100;

        // Convert the gross or net string to a money value
        MonetaryAmount priceNet = FastMoney.of(NumberUtils.DOUBLE_ZERO, currencyCode);

        // Use the net string, if it is set
        // => net string is *never* set! The connectors don't deliver it!

        // Use the gross string, if it is set
        if (product.getGross() != null) {
            MonetaryAmount priceGross = FastMoney.of(product.getGross(), currencyCode);
            priceNet = priceGross.divide(1 + vatPercentDouble);
        }

        VAT vat = getOrCreateVAT(product.getVatname(), vatPercentDouble);
        // Import the item as a new product
        Product productItem;

        // Get the category of the imported products from the preferences
        String shopCategory = preferences.getString(Constants.PREFERENCES_WEBSHOP_PRODUCT_CATEGORY);
        shopCategory = StringUtils.appendIfMissing(shopCategory, "/");

        // Use the EAN number
        if (useEANasItemNr && product.getEan() != null && !product.getEan().isEmpty())
			productModel = product.getEan();

        // Use product name as product model, if model is empty
        if (productModel.isEmpty() && !product.getName().isEmpty())
            productModel = product.getName();

        // Use product model as product name, if name is empty
        if (product.getName().isEmpty() && !productModel.isEmpty())
            productName = productModel;

        // Create the URL to the product image
        byte[] picture = null;
        if (!product.getImage().isEmpty()) {
        	picture = downloadImageFromUrl(connector.getShopURL() + productImagePath + product.getImage());
        }

        // Convert the quantity string to a double value
        Double quantity = product.getQuantity() != null ? product.getQuantity().doubleValue() : NumberUtils.DOUBLE_ZERO;
        // Create a new product object
        productItem = fakturamaModelFactory.createProduct();
        productItem.setName(productName);
        productItem.setItemNumber(productModel);

        // save ProductCategory
        ProductCategory productCategoryFromBuilder = productCategoriesDAO.getCategory(shopCategory + product.getCategory(), true);
        productItem.setCategories(productCategoryFromBuilder);
        productItem.setDescription(productDescription);
        productItem.setPrice1(priceNet.getNumber().numberValue(Double.class));
        productItem.setVat(vat);
        productItem.setPicture(picture);
        productItem.setQuantity(quantity);
        productItem.setWebshopId(product.getId() != null ? product.getId().longValue() : Long.valueOf(0));
        productItem.setQuantityUnit(product.getQunit());
        productItem.setDateAdded(Date.from(Instant.now()));
        productItem.setValidFrom(Date.from(Instant.now()));

        // Add a new product to the data base, if it not exists yet	
        Product existingProduct = productsDAO.findOrCreate(productItem);
        if (existingProduct != null) {
            // Update data
          //  existingProduct.clearCategories();
       //     productItem.getCategories().forEach(cat -> existingProduct.addToCategories(cat));
            existingProduct.setName(productItem.getName());
            existingProduct.setItemNumber(productItem.getItemNumber());
            existingProduct.setDescription(productItem.getDescription());
            existingProduct.setPrice1(productItem.getPrice1());
            existingProduct.setVat(productItem.getVat());
            existingProduct.setPicture(productItem.getPicture());
            existingProduct.setQuantity(productItem.getQuantity());
            existingProduct.setWebshopId(productItem.getWebshopId());
            existingProduct.setQuantityUnit(productItem.getQuantityUnit());

            // Update the modified product data
            productsDAO.save(existingProduct);
        }
    }

    /**
     * Download an image and return it as byte array
     * 
     * @param address
     *            The URL of the image
     */
    private byte[] downloadImageFromUrl(String address) {            
    	// Cancel if address is empty
    	if (address.isEmpty())
    		return null;

    	// always get the image from server, we don't store it in file system anymore
        // Connect to the web server
        URI u = URI.create(address);
        try (InputStream in = u.toURL().openStream()) {
            return IOUtils.toByteArray(in); 
        }
        catch (MalformedURLException e) {
            //T: Status message importing data from web shop
            log.error(e, msg.importWebshopErrorMalformedurl + " " + address);
        }
        catch (IOException e) {
            //T: Status message importing data from web shop
            log.error(e, msg.importWebshopErrorCantopenpicture + " " + address);
        }
        return null;
    }
//
//        /**
//         * Convert the payment method to a readable (and localized) text.
//         * 
//         * @param intext
//         *            order status
//         * @return payment method as readable (and localized) text
//         */
//        private String getPaymentMethodText(String intext) {
//        	String paymentstatustext = intext;
//        
//        	if (intext.equalsIgnoreCase("cod"))
//        		paymentstatustext = msg.importWebshopDataCashondelivery;
//        	else if (intext.equalsIgnoreCase("prepayment"))
//        		paymentstatustext = msg.importWebshopDataPrepayment;
//        	else if (intext.equalsIgnoreCase("creditcard"))
//        		paymentstatustext = msg.importWebshopDataCreditcard;
//        	else if (intext.equalsIgnoreCase("check"))
//        		paymentstatustext = msg.importWebshopDataCheque;
//        
//        	return paymentstatustext;
//        
//        }
        
	/**
	 * Debug input stream.
	 *
	 * @param is
	 *            the {@link InputStream}
	 */
	@SuppressWarnings("unused")
	private void debugInputStream(InputStream is) {
		String result = getStringFromInputStream(is);
		System.out.println(result);
		System.out.println("Done");
	}

	/**
     * convert InputStream to String.
     *
     * @param is the {@link InputStream}
     * @return the string from input stream
     */
	private String getStringFromInputStream(InputStream is) {
        String line = "";
        try {
            line = IOUtils.toString(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
	}
	
	/**
	 * Sets the progress of the job in percent
	 * 
	 * @param percent
	 */
	private void setProgress(int percent) {
	    if (percent > worked) {
	        localMonitor.worked(percent - worked);
	        worked = percent;
	    }
	}

	/**
	 * @return the runResult
	 */
	public String getRunResult() {
		return runResult;
	}

	/**
	 * @param runResult the runResult to set
	 */
	public void setRunResult(String runResult) {
		this.runResult = runResult;
	}

	/**
	 * @return the connector
	 */
	public WebShopConnector getConnector() {
		return connector;
	}

	/**
	 * @param connector the connector to set
	 */
	public void setConnector(WebShopConnector connector) {
		this.connector = connector;
	}
}