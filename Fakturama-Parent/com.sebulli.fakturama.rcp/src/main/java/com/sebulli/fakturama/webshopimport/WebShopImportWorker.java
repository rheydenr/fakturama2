package com.sebulli.fakturama.webshopimport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.javamoney.moneta.FastMoney;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.migration.CategoryBuilder;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.OrderState;
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
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.WebshopStateMapping;
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
import com.sebulli.fakturama.webshopimport.type.ShippingType;
import com.sebulli.fakturama.webshopimport.type.Webshopexport;

public class WebShopImportWorker extends AbstractWebshopImporter implements IRunnableWithProgress {
        private IWebshopConnection webShopImportManager;
        private MathContext mathContext = new MathContext(5);
        private ProductUtil productUtil;
    	private String generalWorkspace;

        // true, if the product's EAN number is imported as item number
        private Boolean useEANasItemNr = false;

        public WebShopImportWorker(IWebshopConnection webShopImportManager) {
        	super(webShopImportManager.getPreferences(), webShopImportManager.getMsg());
        	this.webShopImportManager = webShopImportManager;
    		productUtil = ContextInjectionFactory.make(ProductUtil.class, EclipseContextFactory.getServiceContext(Activator.getContext()));
    		generalWorkspace = webShopImportManager.getPreferences().getString(Constants.GENERAL_WORKSPACE);
	}

		@Override
	    public void run(IProgressMonitor pMonitor) throws InvocationTargetException, InterruptedException  {
			useEANasItemNr = webShopImportManager.getPreferences().getBoolean(Constants.PREFERENCES_WEBSHOP_USE_EAN_AS_ITEMNR);

            Integer maxProducts = Integer.parseInt(webShopImportManager.getPreferences().getString(Constants.PREFERENCES_WEBSHOP_MAX_PRODUCTS));
            Boolean onlyModifiedProducts = webShopImportManager.getPreferences().getBoolean(Constants.PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS);
	        localMonitor = pMonitor;
            webShopImportManager.setRunResult("");
    		Webshopexport webshopexport = null;
            
   			currencyCode = DataUtils.getInstance().getDefaultCurrencyUnit();
            
            // Check empty URL
            if (address.isEmpty()) {
                //T: Status message importing data from web shop
            	webShopImportManager.setRunResult(msg.importWebshopErrorUrlnotset);
                return;
            }
            
            // Add "http://" if no protocol is given
            address = StringUtils.prependIfMissingIgnoreCase(address, "http://", "https://", "file://");
    
            // Get the open order IDs that are out of sync with the webshop
			// from the file system
			webShopImportManager.readOrdersToSynchronize();
            BufferedWriter logBuffer = null;

            try {
                // Connect to web shop
                worked = 0;
                //T: Status message importing data from web shop
                localMonitor.beginTask(msg.importWebshopInfoConnection, 100);
                //T: Status message importing data from web shop
                localMonitor.subTask(msg.importWebshopInfoConnected + " " + address);
                setProgress(10);

                // Send user name , password and a list of unsynchronized orders to
                // the shop
                URLConnection conn = createConnection(address, useAuthorization, authorizationUser, authorizationPassword);
                if(conn != null) {
                	OutputStream outputStream = conn.getOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                    setProgress(20);
                    String postString = "username=" + URLEncoder.encode(user, "UTF-8") + "&password=" +URLEncoder.encode(password, "UTF-8") ;
    
                    String actionString = "";
                    if (webShopImportManager.isGetProducts())
                        actionString += "_products";
                    if (webShopImportManager.isGetOrders())
                        actionString += "_orders";
                    if (!actionString.isEmpty())
                        actionString = "&action=get" + actionString;
    
                    postString += actionString + "&setstate=" + webShopImportManager.getOrderstosynchronize().toString();
                    
                    if (maxProducts > 0) {
                        postString += "&maxproducts=" + maxProducts.toString();
                    }
    
                    if (onlyModifiedProducts) {
                        String lasttime = webShopImportManager.getPreferences().getString("lastwebshopimport");
                        if (! lasttime.isEmpty())
                            postString += "&lasttime=" + lasttime.toString();
                    }
                
                    webShopImportManager.getLog().info("POST-String: " + postString);
                    writer.write(postString);
                    writer.flush();
                    writer.close();
    
                }
                setProgress(30);
                
                // Start a connection in an extra thread
                InterruptConnection interruptConnection = new InterruptConnection(conn);
                new Thread(interruptConnection).start();
                while (!localMonitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError());
    
                // If the connection was interrupted and not finished: return
                if (!interruptConnection.isFinished()) {
                    ((HttpURLConnection)conn).disconnect();
                    if (interruptConnection.isError()) {
                        //T: Status error message importing data from web shop
                    	webShopImportManager.setRunResult(msg.importWebshopErrorCantconnect);
                    }
                    return;
                }
    
                // If there was an error, return with error message
                if (interruptConnection.isError()) {
                    ((HttpURLConnection)conn).disconnect();
                    //T: Status message importing data from web shop
                    webShopImportManager.setRunResult(msg.importWebshopErrorCantread);
                    return;
                }
                
        		// 1. We need to create JAXBContext instance
        		JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

        		/* if we have larger documents we have to use SAX.         		*/
//                // 2. create a new XML parser
//                SAXParserFactory factory = SAXParserFactory.newInstance();
//                factory.setNamespaceAware(true);
//                XMLReader reader = factory.newSAXParser().getXMLReader();
        		
//        		// 2. Use JAXBContext instance to create the Unmarshaller.
        		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

				// 3. Use the Unmarshaller to unmarshal the XML document to get
				// an instance of JAXBElement.
    
                //T: Status message importing data from web shop
                localMonitor.subTask(msg.importWebshopInfoLoading);
    
                // Get the directory of the workspace
                String filename = generalWorkspace;
    
                Path logFile = null;
    
                // Do not save log files if there is no workspace set
                if (!filename.isEmpty()) {
    
                    // Create a sub folder "Log" if it does not exist yet.
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
                    logBuffer = Files.newBufferedWriter(logFile, Charset.forName("UTF-8"));
                }
                
				// 4. Get the instance of the required JAXB Root Class from the
				// JAXBElement.
				webshopexport = (Webshopexport) unmarshaller
        					.unmarshal(interruptConnection.getInputStream());
                
				// alternatively (for large responses)
//                // prepare a Splitter
//                Splitter splitter = new Splitter(jaxbContext);
//
//                // connect two components
//                reader.setContentHandler(splitter);
//                
//                // note that XMLReader expects an URL, not a file name.
//                // so we need conversion.
//                reader.parse(new InputSource(interruptConnection.getInputStream()));
                
        		setProgress(50);
        		
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
                		webShopImportManager.setRunResult(msg.importWebshopErrorNodata + "\n" + address);
                        return;
                    }
    
                    // Clear the list of orders to sync, if the data was sent
                	// NodeList ndList = document.getElementsByTagName("webshopexport");
					if (webshopexport.getOrders() != null) {
						webShopImportManager.setOrderstosynchronize(new Properties());
					} else {
						webShopImportManager.setRunResult("import NOT ok");
					}
    
                    // Get the error elements and add them to the run result list
                    //ndList = document.getElementsByTagName("error");
                    if (StringUtils.isNotEmpty(webshopexport.getError()) ) {
                    	webShopImportManager.setRunResult(webshopexport.getError());
                    }
                }
                // else cancel the download process
    
                // Interpret the imported data (and load the product images)
                if (webShopImportManager.getRunResult().isEmpty()) {
                    // If there is no error - interpret the data.
                    interpretWebShopData(localMonitor, webshopexport);
                }
    
                // Store the time of now
                String now = DataUtils.getInstance().DateAsISO8601String();
                webShopImportManager.getPreferences().putValue("lastwebshopimport", now);
                
                localMonitor.done();
            }
            catch (MarshalException mex) {
                //T: Status message importing data from web shop
            	webShopImportManager.setRunResult(msg.importWebshopErrorNodata + "\n" + address + "\n" + mex.getMessage());
			}
            catch (Exception e) {
                //T: Status message importing data from web shop
            	webShopImportManager.setRunResult(msg.importWebshopErrorCantopen + "\n" + address + "\n");
            	webShopImportManager.setRunResult(webShopImportManager.getRunResult()+"Message: " + e.getLocalizedMessage()+ "\n");
                if (e.getStackTrace().length > 0)
                	webShopImportManager.setRunResult(webShopImportManager.getRunResult()+"Trace: " + e.getStackTrace()[0].toString()+ "\n");

                if (webshopexport != null)
                	webShopImportManager.setRunResult(webShopImportManager.getRunResult()+"\n\n" + webshopexport);
                }
            finally {
            	if(logBuffer != null) {
            		try {
						logBuffer.close();
					} catch (IOException e) {
						webShopImportManager.getLog().error(e, "couldn't close output stream for webshopimport logfile.");
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
        
        	//shopSystem ="";
        	shopURL = "";
        	productImagePath = "";
        
        	// Mark all orders as "in sync with the web shop"
        	allOrdersAreInSync();
        
        	// There is no order
        	if (webshopexport == null)
        		return;
        
        	// Get the general shop data
        	if (webshopexport.getWebshop() != null) {
        		//shopSystem = webshopexport.getWebshop().getShop();
        		shopURL = webshopexport.getWebshop().getUrl();
        	}
        
        	// Get the general products data
        	if (webshopexport.getProducts() != null) {
        		productImagePath = webshopexport.getProducts().getImagepath();
        	}
        
        	// Get all products and import them
        	List<ProductType> productList = webshopexport.getProducts().getProduct();
			int producListSize = productList.size();
			for (int productIndex = 0; productIndex < producListSize; productIndex++) {
        		//T: Status message importing data from web shop
        		monitor.subTask(msg.importWebshopInfoLoading + " " + Integer.toString(productIndex + 1) + "/" + Integer.toString(producListSize));
        		setProgress(50 + 40 * (productIndex + 1) / producListSize);
        		ProductType product = productList.get(productIndex);
        		createProductFromXMLOrderNode(product);
        		
        		// Cancel the product picture import process
        		if ( monitor.isCanceled() )
        			return;
        	}
        
        	// Get order by order and import it
        	//T: Status message importing data from web shop
        	monitor.subTask(msg.importWebshopInfoImportorders);
        	setProgress(95);
        	List<OrderType> orderList = webshopexport.getOrders().getOrder();
        	int orderListSize = orderList.size();
        	for (int orderIndex = 0; orderIndex < orderListSize; orderIndex++) {
        		OrderType order = orderList.get(orderIndex);
        		createOrderFromXMLOrderNode(order, webshopexport.getWebshop().getLang());
        	}
        
        	// Save the new list of orders that are not in synch with the shop
        	webShopImportManager.saveOrdersToSynchronize();
        	
        }

        /**
         * Mark all orders as "in sync" with the web shop
         */
        private void allOrdersAreInSync() {
        	webShopImportManager.setOrderstosynchronize(new Properties());
        	Path f = Paths.get(generalWorkspace, "/orders2sync.txt");
        	try {
                Files.deleteIfExists(f);
            }
            catch (IOException e) {
                webShopImportManager.getLog().error(e, "can't delete orders2sync.txt");
            }
        }

        /**
         * Parse an XML node and create a new order for each order entry
         * 
         * @param order
         *            The node with the orders to import
         * @throws SQLException 
         */
        private void createOrderFromXMLOrderNode(OrderType order, String lang) throws FakturamaStoringException {
        	ContactUtil contactUtil = ContextInjectionFactory.make(ContactUtil.class, this.webShopImportManager.getContext());   			
            Date today = Date.from(Instant.now());
        	
    		// Order data
    		String webshopId;
    		String webshopDate;

    		// Comments
    		String commentDate;
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
            if(!this.webShopImportManager.getDocumentsDAO().findByDocIdAndDocDate(DocumentType.ORDER, webshopId, calendarWebshopDate).isEmpty()) {
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
        
            CategoryBuilder<ContactCategory> contactCatBuilder = new CategoryBuilder<>(webShopImportManager.getLog());
   
        	// First get all contacts. Normally there is only one
            ContactType contact = order.getContact();        

			Contact contactItem = fakturamaModelFactory.createDebitor();
			// Convert a gender character "m" or "f" to the gender number 
			// 1 or 2
			if (contact.getGender().equals("m"))
			    contactItem.setGender(Integer.valueOf(1));
			if (contact.getGender().equals("f"))
                contactItem.setGender(Integer.valueOf(2));
			
			contactItem.setValidFrom(Date.from(instant));

			// Get the category for new contacts from the preferences
			String shopCategory = webShopImportManager.getPreferences().getString(Constants.PREFERENCES_WEBSHOP_CONTACT_CATEGORY);
			if(StringUtils.isNotEmpty(shopCategory)) {
    			ContactCategory contactCat = contactCatBuilder.buildCategoryFromString(shopCategory, ContactCategory.class);
    			// later we have more than one category per contact
//    			contactItem.addToCategories(contactCat);
                contactItem.setCategories(contactCat);
			}
			
			// set explicit the customers data
			contactItem.setCustomerNumber(contact.getId());
            contactItem.setFirstName(contact.getFirstname());
            contactItem.setName(contact.getLastname());
            contactItem.setCompany(contact.getCompany());
            contactItem.setPhone(contact.getPhone());
            contactItem.setEmail(contact.getEmail());
			contactItem.setValidFrom(today);
            
            Address address = fakturamaModelFactory.createAddress();
            address.setStreet(contact.getStreet());
            address.setZip(contact.getZip());
            address.setCity(contact.getCity());
            address.setValidFrom(today);
            String countryCode = LocaleUtil.getInstance(lang).findCodeByDisplayCountry(contact.getCountry());
            address.setCountryCode(countryCode);
            
            contactItem.setAddress(address);
            contactItem = this.webShopImportManager.getContactsDAO().findOrCreate(contactItem);
//            contactItem.setSupplierNumber(contact.get); ==> is not transfered from connector!!!

            Address deliveryAddress = fakturamaModelFactory.createAddress();
            deliveryAddress.setStreet(contact.getDeliveryStreet());
            deliveryAddress.setZip(contact.getDeliveryZip());
            deliveryAddress.setCity(contact.getDeliveryCity());
            deliveryAddress.setValidFrom(today);
            countryCode = LocaleUtil.getInstance(lang).findCodeByDisplayCountry(contact.getDeliveryCountry());
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
                deliveryContact = this.webShopImportManager.getContactsDAO().findOrCreate(deliveryContact);
                //   contactItem.getDeliveryContacts().add(deliveryContact);
                contactItem.setAlternateContacts(deliveryContact);
            }
        
            dataSetDocument.setBillingContact(contactItem);
//            dataSetDocument.setAddress(contactItem.getAddress(false)); // included in contact
//            dataSetDocument.setDeliveryaddress(deliveryContact); // included in contact
            dataSetDocument.setAddressFirstLine(contactUtil.getNameWithCompany(contactItem));			
        
        	// Get the comments
        	for (CommentType commentType : order.getComments()) {
        		// Get the comment text
    			commentDate = DataUtils.getInstance().DateAndTimeAsLocalString(commentType.getDate());
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
    				webShopImportManager.getLog().error(e, String.format(webShopImportManager.getMsg().importWebshopErrorCantconvertnumber, 
    						vatPercent, " (vatPercent)" ));
    			}
    
    			// If one item has a vat value, reset the noVat flag
    			if (vatPercent > 0.0) {
    				noVat = false;
    			} else {
    				// Use the vat name
    				if (noVatName.isEmpty() && !itemType.getVatname().isEmpty()) {
    					noVatName = itemType.getVatname();
    				}
    			}
    
    			// Calculate the net value of the price
        		MonetaryAmount priceNet = FastMoney.of(0.0, currencyCode);
				MonetaryAmount priceGross = FastMoney.of(itemType.getGross(), currencyCode);
				priceNet = priceGross.divide(1 + vatPercent);
    
                // Add the VAT value to the data base, if it is a new one
    			VAT vat = getOrCreateVAT(itemType.getVatname(), vatPercent);
    
    			// Get the category of the imported products from the preferences
    			shopCategory = webShopImportManager.getPreferences().getString(Constants.PREFERENCES_WEBSHOP_PRODUCT_CATEGORY);
    			shopCategory = StringUtils.appendIfMissing(shopCategory, "/", "/");
    
                // Import the item as a new product
    			// Use item name as item model, if model is empty
    			if (itemType.getModel().isEmpty() && !itemType.getName().isEmpty()) {
    				itemModel = itemType.getName();
    			}

    			// Use item model as item name, if name is empty
    			if (itemType.getName().isEmpty() && !itemType.getModel().isEmpty()) {
    				itemName = itemType.getModel();
    			}
    
    			// Import the product attributes
    			itemDescription = new StringBuffer();
    			for (AttributeType attribute : itemType.getAttribute()) {
    				// Get all attributes
					if (itemDescription.length() > 0) {
						itemDescription.append(", ");
					}
					itemDescription.append(attribute.getOption()).append(": ");
					itemDescription.append(attribute.getValue());
    			}
    
    			// Create a new product
    			Product product = fakturamaModelFactory.createProduct();
    			// itemName, itemModel, shopCategory + itemCategory, itemDescription, priceNet, vat, "", "", 1.0, productID, itemQUnit
    			product.setName(itemName);
    			product.setItemNumber(itemModel);
                ProductCategory productCategory = this.webShopImportManager.getProductCategoriesDAO().getCategory(shopCategory + itemType.getCategory(), true);
    			product.setCategories(productCategory);
    			
    			product.setDescription(itemDescription.toString());
    			product.setPrice1(priceNet.getNumber().numberValue(Double.class));
    			product.setVat(vat);
    			product.setValidFrom(today);
    			//product.setProductId(itemType.getProductid());
    
    			// Add the new product to the data base, if it's not existing yet
    			Product newOrExistingProduct = this.webShopImportManager.getProductsDAO().findOrCreate(product);
    			// Get the picture from the existing product  ==> TODO WHY???
//    			product.setPictureName(newOrExistingProduct.getPictureName());
    
    			// Add this product to the list of items
    			DocumentItem item = fakturamaModelFactory.createDocumentItem();
    			item.setPosNr(itemIndex++);
    			//(Double.valueOf(itemQuantity), product, itemDiscountDouble);
    			/*
    			 * per default some other values are set from product
        this(-1, product.getStringValueByKey("name"), product.getIntValueByKey("id"), product.getStringValueByKey("itemnr"), false, "", -1, false, quantity,
                product.getStringValueByKey("description"), product.getPriceByQuantity(quantity), product.getIntValueByKey("vatid"), discount, 0.0, "", "", false,
                product.getStringValueByKey("picturename"), false, product.getStringValueByKey("qunit"));

    			 */
    			item.setName(newOrExistingProduct.getName());
    			item.setItemNumber(newOrExistingProduct.getItemNumber());
    			item.setDescription(newOrExistingProduct.getDescription());
    			item.setQuantity(Double.valueOf(itemType.getQuantity()));
    			item.setQuantityUnit(StringUtils.isBlank(itemType.getQunit()) ? newOrExistingProduct.getQuantityUnit() : itemType.getQunit());
    			item.setValidFrom(today);
    			item.setProduct(newOrExistingProduct);
    			item.setItemVat(vat);
    			item.setPrice(productUtil.getPriceByQuantity(newOrExistingProduct, item.getQuantity()));
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
    			shopCategory = webShopImportManager.getPreferences().getString(Constants.PREFERENCES_WEBSHOP_SHIPPING_CATEGORY);   
    			VAT shippingvat = getOrCreateVAT(shippingType.getVatname(), shippingVatPercent);//vatsDAO.findOrCreate(shippingvat);
    
    			// Add the shipping to the data base, if it's a new shipping
    			Shipping shipping = fakturamaModelFactory.createShipping();
    			shipping.setName(shippingType.getName());
    			ShippingCategory newShippingCategory = this.webShopImportManager.getShippingCategoriesDAO().getCategory(shopCategory, true);
//    			shipping.addToCategories(newShippingCategory);
    			shipping.setCategories(newShippingCategory);
    			shipping.setDescription(shippingType.getName());
    			shipping.setShippingValue(shippingGross);
    			shipping.setShippingVat(shippingvat);
    			shipping.setAutoVat(ShippingVatType.SHIPPINGVATFIX);
    			shipping.setValidFrom(today);
    			shipping = this.webShopImportManager.getShippingsDAO().findOrCreate(shipping);
    
    			// Set the document entries for the shipping
                dataSetDocument.setShipping(shipping);
                dataSetDocument.setShippingAutoVat(ShippingVatType.SHIPPINGVATFIX);
                dataSetDocument.setShippingValue(shippingGross);
    			String s = this.webShopImportManager.getMsg().importWebshopInfoWebshopno + " ";
    
    			// Use the order ID of the web shop as customer reference for
    			// imports web shop orders
    			s += StringUtils.leftPad(webshopId, 5, '0');
    			//T: Text of the web shop reference
    			dataSetDocument.setCustomerRef(s);
    		}
        
        	// Get the payment (s)
    		PaymentType paymentType = order.getPayment();
			if (paymentType != null) {
    			// Add the payment to the data base, if it's a new one
    			Payment payment = fakturamaModelFactory.createPayment();
    			payment.setName(paymentType.getName());
    			payment.setDescription(paymentType.getName() + " (" + paymentType.getType() + ")");
    			payment.setPaidText(msg.dataDefaultPaymentPaidtext);
    			payment = this.webShopImportManager.getPaymentsDAO().findOrCreate(payment);  // here the validFrom is also set
            	dataSetDocument.setPayment(payment);
    		}
        
        	// Set the progress of an imported order to "pending"
        	WebshopStateMapping mappedStatus = webShopImportManager.getWebshopStateMappingDAO().findByName(order.getStatus());
        	if(mappedStatus != null) {
        		dataSetDocument.setProgress(OrderState.valueOf(mappedStatus.getOrderState()).getState());
        	} else {
        		dataSetDocument.setProgress(OrderState.PENDING.getState());
        	}
        
        	// Set the document data
//        	dataSetDocument.setOrderDate(Date.from(instant)); // TODO which date is meant?
        	dataSetDocument.setDocumentDate(Date.from(instant));
        	dataSetDocument.setDateAdded(today);
        	dataSetDocument.setMessage(StringUtils.defaultString(dataSetDocument.getMessage()) + comment.toString());
    	    dataSetDocument.setItemsRebate(paymentType.getDiscount() != null ? paymentType.getDiscount().doubleValue() : 0.0);
        	dataSetDocument.setTotalValue(paymentType.getTotal().doubleValue());
        	dataSetDocument.setPaidValue(Double.valueOf(0.0));
        	dataSetDocument.setPaid(Boolean.FALSE);
        
        	// There is no VAT used
        	if (noVat) {
        		// Set the no-VAT flag in the document and use the name and description
        		VAT noVatReference = this.webShopImportManager.getVatsDAO().findByName(noVatName);
        		if (noVatReference != null) {
        			dataSetDocument.setNoVatReference(noVatReference);
        		}
        	}
        	
        	// Update the data base with the new document data
        	this.webShopImportManager.getDocumentsDAO().save(dataSetDocument);
        
        	// Re-calculate the document's total sum and check it.
        	// It must be the same total value as in the web shop
//        	dataSetDocument.calculate();
        	DocumentSummary summary = new DocumentSummaryCalculator(currencyCode).calculate(dataSetDocument);
			MonetaryAmount calcTotal = summary.getTotalGross();
			MonetaryAmount totalFromWebshop = FastMoney.of(paymentType.getTotal(), currencyCode); 
        	// If there is a difference, show a warning.
        	if (!calcTotal.isEqualTo(totalFromWebshop)) {
        		//T: Error message importing data from web shop
        		//T: Format: ORDER xx TOTAL SUM FROM WEB SHOP: xx IS NOT EQUAL TO CALCULATED ONE: xx. PLEASE CHECK
        		String error = msg.toolbarNewOrderName + ":";
        		error += " " + webshopId + "\n";
        		error += msg.importWebshopInfoTotalsum;
        		error += "\n" + DataUtils.getInstance().DoubleToFormatedPriceRound(paymentType.getTotal().doubleValue()) + "\n";
        		error += msg.importWebshopErrorTotalsumincorrect;
        		error += "\n" + DataUtils.getInstance().formatCurrency(calcTotal) + "\n";
        		error += msg.importWebshopErrorTotalsumcheckit;
        		webShopImportManager.setRunResult(error);
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
                vat = this.webShopImportManager.getVatsDAO().addIfNew(vat);
            }
            catch (FakturamaStoringException e1) {
                webShopImportManager.getLog().error(e1);
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
            MonetaryAmount priceNet = FastMoney.of(0.0, currencyCode);

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
            String shopCategory = webShopImportManager.getPreferences().getString(Constants.PREFERENCES_WEBSHOP_PRODUCT_CATEGORY);
            shopCategory = StringUtils.appendIfMissing(shopCategory, "/", "/");

            // Use the EAN number
            if (useEANasItemNr) {
                if (product.getEan() != null && !product.getEan().isEmpty())
                    productModel = product.getEan();
            }

            // Use product name as product model, if model is empty
            if (productModel.isEmpty() && !product.getName().isEmpty())
                productModel = product.getName();

            // Use product model as product name, if name is empty
            if (product.getName().isEmpty() && !productModel.isEmpty())
                productName = productModel;

            // Create the URL to the product image
            byte[] picture = null;
            if (!product.getImage().isEmpty()) {
            	picture = downloadImageFromUrl(shopURL + productImagePath + product.getImage());
            }

            // Convert the quantity string to a double value
            Double quantity = product.getQuantity() != null ? product.getQuantity().doubleValue() : NumberUtils.DOUBLE_ZERO;
            // Create a new product object
            productItem = fakturamaModelFactory.createProduct();
            productItem.setName(productName);
            productItem.setItemNumber(productModel);

            // save ProductCategory
            ProductCategory productCategoryFromBuilder = this.webShopImportManager.getProductCategoriesDAO().getCategory(shopCategory + product.getCategory(), true);
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
            Product existingProduct = this.webShopImportManager.getProductsDAO().findOrCreate(productItem);
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
                this.webShopImportManager.getProductsDAO().save(existingProduct);
            }
        }

        /**
         * Download an image and return it as byte array
         * 
         * @param address
         *            The URL of the image
         */
        private byte[] downloadImageFromUrl(String address) {            
            String filePath = generalWorkspace + Constants.PRODUCT_PICTURE_FOLDER;
            
        	// Cancel if address or filename is empty
        	if (address.isEmpty() || filePath.isEmpty())
        		return null;

        	// always get the image from server, we don't store it in file system anymore
            // Connect to the web server
            URI u = URI.create(address);
            try (InputStream in = u.toURL().openStream()) {
                return IOUtils.toByteArray(in); 
            }
            catch (MalformedURLException e) {
                //T: Status message importing data from web shop
                webShopImportManager.getLog().error(e, this.webShopImportManager.getMsg().importWebshopErrorMalformedurl + " " + address);
            }
            catch (IOException e) {
                //T: Status message importing data from web shop
                webShopImportManager.getLog().error(e, this.webShopImportManager.getMsg().importWebshopErrorCantopenpicture + " " + address);
            }
            return null;
        }

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

    		BufferedReader br = null;
    		StringBuilder sb = new StringBuilder();

    		String line;
    		try {

    			br = new BufferedReader(new InputStreamReader(is));
    			while ((line = br.readLine()) != null) {
    				sb.append(line);
    			}

    		} catch (IOException e) {
    			e.printStackTrace();
    		} finally {
    			if (br != null) {
    				try {
    					br.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    		}
    		return sb.toString();
    	}
	 }