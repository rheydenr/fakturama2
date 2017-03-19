/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2013 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ralf Heydenreich - initial API and implementation
 */
package com.sebulli.fakturama.migration;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
//import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.dao.ItemAccountTypeDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ProductCategoriesDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dao.PropertiesDAO;
import com.sebulli.fakturama.dao.ReceiptVouchersDAO;
import com.sebulli.fakturama.dao.ShippingCategoriesDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.TextsDAO;
import com.sebulli.fakturama.dao.VatCategoriesDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.dbconnector.OldTableinfo;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.migration.olddao.OldEntitiesDAO;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BankAccount;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.ContactType;
//import com.sebulli.fakturama.model.CustomDocument;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.Dunning;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.ItemListTypeCategory;
import com.sebulli.fakturama.model.ItemType;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.ProductBlockPrice;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.ReliabilityType;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingCategory;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.TextCategory;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.model.UserProperty;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.model.VoucherItem;
import com.sebulli.fakturama.model.VoucherType;
import com.sebulli.fakturama.money.CurrencySettingEnum;
import com.sebulli.fakturama.oldmodel.OldContacts;
import com.sebulli.fakturama.oldmodel.OldDocuments;
import com.sebulli.fakturama.oldmodel.OldExpenditureitems;
import com.sebulli.fakturama.oldmodel.OldExpenditures;
import com.sebulli.fakturama.oldmodel.OldItems;
import com.sebulli.fakturama.oldmodel.OldList;
import com.sebulli.fakturama.oldmodel.OldPayments;
import com.sebulli.fakturama.oldmodel.OldProducts;
import com.sebulli.fakturama.oldmodel.OldProperties;
import com.sebulli.fakturama.oldmodel.OldReceiptvoucheritems;
import com.sebulli.fakturama.oldmodel.OldReceiptvouchers;
import com.sebulli.fakturama.oldmodel.OldShippings;
import com.sebulli.fakturama.oldmodel.OldTexts;
import com.sebulli.fakturama.oldmodel.OldVats;
import com.sebulli.fakturama.parts.itemlist.DocumentItemListTable;
import com.sebulli.fakturama.startup.ConfigurationManager;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;
import com.sebulli.fakturama.views.datatable.payments.PaymentListTable;
import com.sebulli.fakturama.views.datatable.shippings.ShippingListTable;
import com.sebulli.fakturama.views.datatable.vats.VATListTable;

/**
 * Migration Tool for converting old data to new one. This affects only database
 * data, not templates or other plain files.
 * 
 */
public class MigrationManager {
	private static final int MAX_LOGENTRY_WIDTH = 80;

	@Inject
	private IEclipseContext context;

	@Inject
	private Logger log;
	
	private java.util.logging.Logger migLogUser;

	@Inject
	private IEclipsePreferences eclipsePrefs;

// this doesn't work because the IPreferenceStore isn't set at this stage
//	@Inject
//    @Preference(value=InstanceScope.SCOPE)
//    private IPreferenceStore preferences;

	@Inject
	@Translation
	protected Messages msg;

	/*
	 * all available DAO classes
	 */
    @Inject
	private ContactsDAO contactDAO;
    
    @Inject
	private ContactCategoriesDAO contactCategoriesDAO;
    
    private ContactUtil contactUtil;
    
    @Inject
	private DocumentsDAO documentDAO;
    
    @Inject
	private PaymentsDAO paymentsDAO;
    
    @Inject
	private ProductsDAO productsDAO;
    
    @Inject
	private ProductCategoriesDAO productCategoriesDAO;
    
    @Inject
	private PropertiesDAO propertiesDAO;
    
    @Inject
	private ItemAccountTypeDAO itemAccountTypeDAO;
    
    @Inject
	private ExpendituresDAO expendituresDAO;
    
    @Inject
	private ReceiptVouchersDAO receiptVouchersDAO;
    
    @Inject
	private VoucherCategoriesDAO voucherCategoriesDAO;
    
    @Inject
	private ShippingsDAO shippingsDAO;
    
    @Inject
	private ShippingCategoriesDAO shippingCategoriesDAO;
    
    @Inject
	private TextsDAO textDAO;
    
    @Inject
	private VatsDAO vatsDAO;
    
    @Inject
	private VatCategoriesDAO vatCategoriesDAO;
	
	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory;
	
	// These Maps are for assigning the old entities to the new one (needed for lookup)
	private Map<Integer, Long> newContacts  = new HashMap<>();
	private Map<Integer, Long> newVats      = new HashMap<>();
	private Map<Integer, Long> newShippings = new HashMap<>();
	private Map<Integer, Long> newPayments  = new HashMap<>();
	private Map<Integer, Long> newProducts  = new HashMap<>();
	
	/*
	 * there's only one DAO for old data
	 */
 //   @Inject
	private OldEntitiesDAO oldDao;

	@Inject
	private IApplicationContext appContext;

	private GregorianCalendar zeroDate;
    private String generalWorkspace = null;

    private Map<String, ItemAccountType> itemAccountTypes;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 
	 * @param cmdService {@link ECommandService} for creating commands
	 * @param handlerService {@link EHandlerService} for executing commands
     */
    @PostConstruct
    public void init() {
        this.zeroDate = new GregorianCalendar(2000, 0, 1);
        this.modelFactory = FakturamaModelPackage.MODELFACTORY;
        this.generalWorkspace  = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, "");
        this.contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);
	}

	/**
	 * entry point for migration of old data (db only)
	 * 
	 * @param parent the current {@link Shell}
	 * @throws BackingStoreException
	 */
	@Execute
	public void migrateOldData(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) throws BackingStoreException {
		// build JDBC connection string; can't be null at this point since we've checked it above (within the caller)
		final String oldWorkDir = eclipsePrefs.get(ConfigurationManager.MIGRATE_OLD_DATA, null);
		final String hsqlConnectionString;
		
		/*
		 * For convenience, I've introduced a switch for using the server variant of the old
		 * HSQL DB since I have to look at the DB with several tools while Fakturama is started.
		 * This switch is intentionally undocumented in migration documentation.
		 */
		if(System.getProperty("fakturama.use.dbserver") == null) {
		    // hard coded old database name
            hsqlConnectionString = "jdbc:hsqldb:file:" + oldWorkDir + "/Database/Database";
		} else {
		    hsqlConnectionString = "jdbc:hsqldb:hsql://localhost/Fakturama";   // for connection with a HSQLDB Server
		}
		eclipsePrefs.put("OLD_JDBC_URL", hsqlConnectionString);
		eclipsePrefs.flush();
		
		initMigLog(oldWorkDir);

		// old entities only have one DAO for all entities
		oldDao = ContextInjectionFactory.make(OldEntitiesDAO.class, context);
		
		// hide splash screen
		appContext.applicationRunning();
		
		// we have to keep a certain order
		final OldTableinfo orderedTasks[] = new OldTableinfo[]{
	        OldTableinfo.Vats,
	        OldTableinfo.Shippings,
	        OldTableinfo.Payments,
	        OldTableinfo.Properties,
	        OldTableinfo.Texts,
			OldTableinfo.Lists,
	        OldTableinfo.Expenditures,
	        OldTableinfo.Receiptvouchers,
	        OldTableinfo.Contacts,
	        OldTableinfo.Products,
	        OldTableinfo.Documents
		};
		//  orderedTasks[] now contains all tables which have to be converted

		// now start a ProgressMonitorDialog for tracing the progress of migration
		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
		try {
			IRunnableWithProgress op = new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(msg.startMigrationBegin, orderedTasks.length);
					for (OldTableinfo tableinfo : orderedTasks) {
						checkCancel(monitor);
						runMigration(monitor, tableinfo);
//						monitor.worked(1);
					}
					monitor.done();
				}
			};
			progressMonitorDialog.run(true, true, op);
		}
		catch (InvocationTargetException e) {
			log.error("Error: ", e.getMessage());
		}
		catch (InterruptedException e) {
			// handle cancellation
			throw new OperationCanceledException();
		}
		finally {
		    eclipsePrefs.flush();
		    log.info(msg.startMigrationEnd);
		}
        migLogUser.info(StringUtils.repeat('*',  MAX_LOGENTRY_WIDTH));
        String tmpStr = String.format("* %s %s", StringUtils.rightPad("End:", 20), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        migLogUser.info(StringUtils.rightPad(tmpStr, MAX_LOGENTRY_WIDTH-1) + "*");
        migLogUser.info(StringUtils.repeat('*',  MAX_LOGENTRY_WIDTH));
        
        // kindly close all handlers (otherwise some file fragments could remain).
        Arrays.stream(migLogUser.getHandlers()).forEach(handler -> handler.close());
	}
	

	/**
	 * Initialize the information file for the user. This is done by simply using the
	 * java.util.Logging class.  
	 */
    private void initMigLog(String oldWorkDir) {
        // We have to configure the log file output and location.
        // The creation of this (very special) Logger is done manually.
        try {
            LogManager.getLogManager().addLogger(java.util.logging.Logger.getLogger(MigrationManager.class.getName()));
            migLogUser = LogManager.getLogManager().getLogger(MigrationManager.class.getName());
            migLogUser.setLevel(Level.INFO);
            FileHandler fh = new FileHandler(generalWorkspace + "/migInfo.log", false);
            MigrationLogFormatter formatter = new MigrationLogFormatter();
            fh.setFormatter(formatter);
            migLogUser.addHandler(fh);
            
            // write some initial information to the log file
            migLogUser.info(StringUtils.repeat('*',  MAX_LOGENTRY_WIDTH));
            String tmpStr = "Migration info log file for the conversion of old Fakturama data.";
            migLogUser.info("*" + StringUtils.center(tmpStr, MAX_LOGENTRY_WIDTH-2) + "*");
            migLogUser.info("*" + StringUtils.repeat(' ',  MAX_LOGENTRY_WIDTH-2) + "*");
            tmpStr = String.format("* %s %s", StringUtils.rightPad("Start:", 20), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            migLogUser.info(StringUtils.rightPad(tmpStr, MAX_LOGENTRY_WIDTH-1) + "*");
            tmpStr = String.format("* %s %s", StringUtils.rightPad("Workspace path:", 20), generalWorkspace);
            migLogUser.info(StringUtils.rightPad(tmpStr, MAX_LOGENTRY_WIDTH-1) + "*");
            tmpStr = String.format("* %s %s", StringUtils.rightPad("Old workspace path:", 20), oldWorkDir);
            migLogUser.info(StringUtils.rightPad(tmpStr, MAX_LOGENTRY_WIDTH-1) + "*");
            // TODO maybe we could write down some other useful information? version of old application?
            migLogUser.info("*" + StringUtils.repeat(' ',  MAX_LOGENTRY_WIDTH-2) + "*");
            migLogUser.info(StringUtils.repeat('*',  MAX_LOGENTRY_WIDTH));
            migLogUser.info(" ");
        }
        catch (SecurityException | IOException e) {
            log.error(e, "error creating migration user info file. Message: " + e.getMessage());
        }
    }

    /**
	 * This method switches to the migration methods.
	 * 
	 * @param progressMonitor {@link SubMonitor}
	 * @param tableinfo tableinfo which table has to be migrated
	 * @throws InterruptedException
	 */
	private void runMigration(IProgressMonitor progressMonitor, OldTableinfo tableinfo) throws InterruptedException {
	    migLogUser.info(String.format("Start converting %s (%s)", msg.getMessageFromKey(tableinfo.getMessageKey()), tableinfo.name()));
        SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 
        		String.format("%s %s", msg.startMigrationConvert, msg.getMessageFromKey(tableinfo.getMessageKey())), 100);
		try {
			switch (tableinfo) {
			case Properties:
				runMigratePropertiesSubTask(subMonitor);
				break;
			case Shippings:
				runMigrateShippingsSubTask(subMonitor);
				break;
			case Vats:
				runMigrateVatsSubTask(subMonitor);
				break;
			case Lists:
				// Country Codes are no user definable data; they will be read
				// from java Locale. Therefore we've to convert only the itemAccountTypes
		        itemAccountTypes = buildItemAccountTypeMap();
				break;
			case Texts:
				runMigrateTextsSubTask(subMonitor);
				break;
			case Contacts:
				runMigrateContactsSubTask(subMonitor);
				break;
			case Documents:
				runMigrateDocumentsSubTask(subMonitor);
				break;
			case Payments:
				runMigratePaymentsSubTask(subMonitor);
				break;
			case Products:
				runMigrateProductsSubTask(subMonitor);
				break;
			case Expenditures:
				runMigrateExpendituresSubTask(subMonitor);
				break;
			case Receiptvouchers:
				runMigrateReceiptvouchersSubTask(subMonitor);
				break;
			default:
				break;
			}
	        migLogUser.info(String.format("End converting %s%n", msg.getMessageFromKey(tableinfo.getMessageKey())));
		}
		catch (RuntimeException rex) {
			log.error(rex, "error while migrating "+tableinfo.name()+"; Message: " + rex.getMessage());
		} finally {
			SubMonitor.done(progressMonitor);
		}
	}


	private void runMigrateProductsSubTask(SubMonitor subMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllProducts();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));

        subMonitor.setWorkRemaining(countOfEntitiesInTable.intValue());
        subMonitor.subTask(String.format("%s %d %s", msg.startMigrationWorking, countOfEntitiesInTable, msg.startMigration));
		CategoryBuilder<ProductCategory> catBuilder = new CategoryBuilder<>(log); 
		Map<String, ProductCategory> productCategories = catBuilder.buildCategoryMap(oldDao.findAllProductCategories(), ProductCategory.class);
		for (OldProducts oldProduct : oldDao.findAllProducts()) {
			try {
				Product product = modelFactory.createProduct();
				addPriceBlock(product, oldProduct.getBlock1(), roundValue(oldProduct.getPrice1()));
				addPriceBlock(product, oldProduct.getBlock2(), roundValue(oldProduct.getPrice2()));
				addPriceBlock(product, oldProduct.getBlock3(), roundValue(oldProduct.getPrice3()));
				addPriceBlock(product, oldProduct.getBlock4(), roundValue(oldProduct.getPrice4()));
				addPriceBlock(product, oldProduct.getBlock5(), roundValue(oldProduct.getPrice5()));
				product.setBlock1(oldProduct.getBlock1());
				product.setBlock2(oldProduct.getBlock2());
				product.setBlock3(oldProduct.getBlock3());
				product.setBlock4(oldProduct.getBlock4());
				product.setBlock5(oldProduct.getBlock5());
				product.setDateAdded(getSaveParsedDate(oldProduct.getDateAdded()));
				product.setDeleted(oldProduct.isDeleted());
				product.setDescription(oldProduct.getDescription());
				if(StringUtils.isNotBlank(oldProduct.getCategory()) && productCategories.containsKey(oldProduct.getCategory())) {
					// add it to the new entity
					//product.addToCategories(productCategoriesDAO.getCategory(oldProduct.getCategory(), true));
					product.setCategories(productCategoriesDAO.getCategory(oldProduct.getCategory(), true));
				}
				product.setItemNumber(oldProduct.getItemnr());
				product.setName(oldProduct.getName());
				// copy the old product picture into the new workspace
				copyProductPicture(product, oldProduct);
				product.setPrice1(roundValue(oldProduct.getPrice1()));
				product.setPrice2(roundValue(oldProduct.getPrice2()));
				product.setPrice3(roundValue(oldProduct.getPrice3()));
				product.setPrice4(roundValue(oldProduct.getPrice4()));
				product.setPrice5(roundValue(oldProduct.getPrice5()));
				product.setQuantity(oldProduct.getQuantity());
				product.setQuantityUnit(oldProduct.getQunit());
				product.setSellingUnit(oldProduct.getUnit());
//				product.setProductCode(oldProduct.gProductCode());
				// find the VAT entry
				Long vatId = newVats.get(oldProduct.getVatid());
				if(vatId != null) {
				    VAT newVat = vatsDAO.findById(vatId);
				    product.setVat(newVat);
				}
				product.setWebshopId(new Long(oldProduct.getWebshopid()));
				product.setWeight(oldProduct.getWeight());
				product.setValidFrom(new Date());
				
				product = productsDAO.save(product, true);
				newProducts.put(oldProduct.getId(), product.getId());
				subMonitor.worked(1);
			}
			catch (FakturamaStoringException e) {
				log.error("error while migrating Product. (old) ID=" + oldProduct.getId() + "; Message: " + e.getMessage());
			}
		}
		subMonitor.done();
	}


	/**
	 * Add a new price block to the new product. Copy from old product if not <code>null</code>.
	 * 
	 * @param product the new Product
	 * @param block1 the block which should be created.
	 */
	private void addPriceBlock(Product product, int block, double price) {
		if(block != 0) {
			ProductBlockPrice blockPrice = modelFactory.createProductBlockPrice();
			blockPrice.setBlock(block);
			blockPrice.setPrice(price);
			blockPrice.setValidFrom(new Date());
			blockPrice.setDateAdded(new Date());
			product.getBlockPrices().add(blockPrice);
		}
	}

	/**
	 * Handles the copying of the product pictures
	 * @param product
	 * @param oldProduct
	 */
	private void copyProductPicture(Product product, OldProducts oldProduct) {
		if (StringUtils.isNoneEmpty(oldProduct.getPicturename())) {

			// First of all check, if the output file already exists.
			Path outputFile = Paths.get(generalWorkspace,
					Constants.PRODUCT_PICTURE_FOLDER,
					oldProduct.getPicturename());
			if (Files.exists(outputFile)) {
				return;
			}
			product.setPicture(createImageFromFile(oldProduct.getPicturename()));
		}
	}

	private void runMigrateDocumentsSubTask(SubMonitor subMonitor) {
		Document document;
		Long countOfEntitiesInTable = oldDao.countAllDocuments();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));

        subMonitor.setWorkRemaining(countOfEntitiesInTable.intValue());
        subMonitor.subTask(String.format("%s %d %s", msg.startMigrationWorking, countOfEntitiesInTable, msg.startMigration));
		Map<Integer, Document> invoiceRelevantDocuments = new HashMap<>();
		Map<Integer, Invoice> invoiceDocuments = new HashMap<>();
		for (OldDocuments oldDocument : oldDao.findAllDocuments()) {
			try {
				BillingType billingType = BillingType.get(oldDocument.getCategory());
                switch (billingType) {
                case INVOICE:
                    document = modelFactory.createInvoice();
                    break;
                case LETTER:
                    document = modelFactory.createLetter();
                    break;
                case ORDER:
                    document = modelFactory.createOrder();
                    break;
                case OFFER:
                    document = modelFactory.createOffer();
                    break;
                case CONFIRMATION:
                    document = modelFactory.createConfirmation();
                    break;
                case CREDIT:
                    document = modelFactory.createCredit();
                    break;
                case DUNNING:
                    document = modelFactory.createDunning();
                    ((Dunning)document).setDunningLevel(oldDocument.getDunninglevel());
                    break;
                case DELIVERY:
                    document = modelFactory.createDelivery();
                    break;
                case PROFORMA:
                    document = modelFactory.createProforma();
                    break;
                default:
                    document = modelFactory.createOrder();
                    break;
                }
				if(oldDocument.getAddressid() < 0) {
					/* manually edited address => store in the data container!
					 * perhaps we have to check additionally if the address stored in document
					 * is equal to the address stored in the database :-(
					 */
					// at first we try to interpret the address data
					Contact contact = modelFactory.createDebitor();
					// there is NO Customer No. since we extracted it from a plain String.
					Address address = contactUtil.createAddressFromString(oldDocument.getAddress());
					contact.setAddress(address);
					// try to get the name
					String name = contactUtil.getDataFromAddressField(oldDocument.getAddress(), "lastname");
					if(name.isEmpty()) {
						name = contactUtil.getDataFromAddressField(oldDocument.getAddress(), "name");
					}
					contact.setName(name);
					contact.setFirstName(contactUtil.getDataFromAddressField(oldDocument.getAddress(), "firstname"));
//					document.getBillingContact().getAddress().setManualAddress(oldDocument.getAddress());
					document.setBillingContact(contact);
					
					Contact deliveryContact = modelFactory.createDebitor();
					Address deliveryAddress = contactUtil.createAddressFromString(oldDocument.getDeliveryaddress());
					deliveryContact.setAddress(deliveryAddress);
					String deliveryName = contactUtil.getDataFromAddressField(oldDocument.getDeliveryaddress(), "lastname");
					if(deliveryName.isEmpty()) {
						deliveryName = contactUtil.getDataFromAddressField(oldDocument.getDeliveryaddress(), "name");
					}
					deliveryContact.setName(deliveryName);
//					deliveryContact.setName(contactUtil.getDataFromAddressField(oldDocument.getDeliveryaddress(), "lastname"));
					deliveryContact.setFirstName(contactUtil.getDataFromAddressField(oldDocument.getDeliveryaddress(), "firstname"));
					if(!deliveryContact.isSameAs(contact)) {
						document.setDeliveryContact(deliveryContact);
					}
//					document.getDeliveryContact().getAddress().setManualAddress(oldDocument.getDeliveryaddress());
				} else {
					// use the previous filled Contact hashmap
					Long newContactIdDerivedFromOld = newContacts.get(oldDocument.getAddressid());
//					if (newContactIdDerivedFromOld == null) {
//						migLogUser.warning(String.format("found a document (No. '%s') which has a contact (id='%d' [DB-ID!]) that is marked as deleted", oldDocument.getName(), oldDocument.getAddressid()));
//						OldContacts oldContact = oldDao.findContactById(oldDocument.getAddressid());
//						
//					} else {
						Contact contact = contactDAO.findById(newContactIdDerivedFromOld);
						if(contact != null) {
						    // delivery documents are slightly different...
						    if(document.getBillingType() == BillingType.DELIVERY) {
		                        document.setBillingContact(contact.getAlternateContacts());
		                        document.setDeliveryContact(contact);
						    } else {
		                        document.setBillingContact(contact);
		                        document.setDeliveryContact(contact.getAlternateContacts());
						    }
						}
//					}
				}
				document.setAddressFirstLine(oldDocument.getAddressfirstline());
				document.setBillingType(billingType);
				document.setCustomerRef(oldDocument.getCustomerref());
				document.setValidFrom(getSaveParsedDate(oldDocument.getDate()));
				document.setDeleted(oldDocument.isDeleted());
				// delivery address? got from contact? Assume that it's equal to contact address 
				// as long there's no delivery address stored 
				document.setDueDays(oldDocument.getDuedays());
				// each Document has its own items
				if(StringUtils.isNotBlank(oldDocument.getItems())) {
					String[] itemRefs = oldDocument.getItems().split(",");
					createItems(document, itemRefs);
				}
				document.setItemsRebate(oldDocument.getItemsdiscount());
				document.setMessage(oldDocument.getMessage());
				document.setMessage2(oldDocument.getMessage2());
				document.setMessage3(oldDocument.getMessage3());
				// The document number is the document name
				document.setName(oldDocument.getName());
				document.setNetGross(oldDocument.getNetgross());
				if(oldDocument.isNovat()) {
					// find the VAT entry
					VAT noVatRef = vatsDAO.findByName(oldDocument.getNovatname());
					if(noVatRef == null) {
						log.error("no entry for " + oldDocument.getNovatname() + " found. (old) document ID=" + oldDocument.getId());
					} else {
						document.setNoVatReference(noVatRef);
					}
					
					// since we now have a reference to a valid VAT we don't need the fields "novatdescription" and "novatname"
				}
				
				// if either the ODT or the PDF field is filled we can assume that the document was printed.
				// Therefore we needn't the "printed" flag.
				document.setOdtPath(oldDocument.getOdtpath());
				document.setPdfPath(oldDocument.getPdfpath());
				document.setPrintTemplate(oldDocument.getPrintedtemplate());
				
				document.setConsultant(oldDocument.getConsultant());
				document.setPrinted(oldDocument.isPrinted());
				document.setDeposit(oldDocument.isIsdeposit());
				document.setDocumentDate(getSaveParsedDate(oldDocument.getDate()));
				document.setOrderDate(getSaveParsedDate(oldDocument.getOrderdate()));
				document.setServiceDate(getSaveParsedDate(oldDocument.getServicedate()));
				// if "paydate" is set and *NOT* 2000-01-01 then the document is paid
				// the "Mark as paid" command could change this state, too. Therefore we need an extra attribute.
				document.setPaid(oldDocument.isPaid());
				document.setPaidValue(oldDocument.getPayvalue());
				Date payDate = getSaveParsedDate(oldDocument.getPaydate());
				if(payDate.compareTo(zeroDate.getTime()) != 0) {
					document.setPayDate(payDate);
				}
				
				// get payment reference
				Long paymentId = newPayments.get(oldDocument.getPaymentid());
				if(paymentId != null) {
                    Payment newPayment = paymentsDAO.findById(paymentId);
    				document.setPayment(newPayment);
				}
				document.setTotalValue(roundValue(oldDocument.getTotal()));
				document.setTransactionId(new Integer(oldDocument.getTransaction()));
				document.setWebshopDate(getSaveParsedDate(oldDocument.getWebshopdate()));
				document.setWebshopId(oldDocument.getWebshopid());
				document.setProgress(oldDocument.getProgress());
				
				// find the Shipping entry
				Shipping newShipping = shippingsDAO.findById(newShippings.get(oldDocument.getShippingid()));
				document.setShipping(newShipping);
                document.setShippingAutoVat(ShippingVatType.get(oldDocument.getShippingautovat()));
                document.setShippingValue(oldDocument.getShipping());

                document = documentDAO.save(document, true);
                // store the pair for later processing
                // if the old Document has an InvoiceId and that ID is the same as the Document's ID
                // then we have to store it for further processing.
                // if the Invoice Id is the same as the document's id then it's the invoice itself
                if(oldDocument.getInvoiceid() >= 0) {
                    if (oldDocument.getId() != oldDocument.getInvoiceid()) {
                        invoiceRelevantDocuments.put(oldDocument.getId(), document);
                    } else {
                        if(!(document instanceof Invoice)) {
                            migLogUser.warning("!!! the document no. " + document.getName() + " is of type " + document.getBillingType() +
                                    " and has itself as invoice reference. This doesn't fit!");
                        } else {
                            invoiceDocuments.put(oldDocument.getId(), (Invoice) document);
                        }
                    }
                }
				subMonitor.worked(1);
			}
			catch (FakturamaStoringException | NumberFormatException e) {
				log.error("error while migrating Document. (old) ID=" + oldDocument.getId()+"; Message: " + e.getMessage());
			}
		}
		// second pass...
		
		// the reference to the source document cannot be set before all documents are stored
		for (OldDocuments oldDocument : oldDao.findAllInvoiceRelatedDocuments()) {
			try {
				// invoiceRelevantDocuments now contains all Documents that needs to have an Invoice reference
				document = invoiceRelevantDocuments.get(oldDocument.getId());
				// now find the corresponding NEW document
				Invoice relatedDocument = invoiceDocuments.get(oldDocument.getInvoiceid());
				if (relatedDocument != null) {
					document.setInvoiceReference(relatedDocument);
					documentDAO.save(document, true);
				}
			}
			catch (FakturamaStoringException e) {
				log.error("error while migrating Document. (old) ID=" + oldDocument.getId()+"; Message: " + e.getMessage());
			}
		}
	}


	/**
	 * @param document
	 * @param itemRefs
	 */
	private void createItems(Document document, String[] itemRefs) {
	    for (int i = 0; i < itemRefs.length; i++) {
            String itemRef = itemRefs[i];
			OldItems oldItem = oldDao.findDocumentItem(Integer.valueOf(itemRef));
			DocumentItem item = modelFactory.createDocumentItem();
			// the position was formerly determined through the order how they stayed in documents entry
			item.setPosNr(Integer.valueOf(i+1));
			item.setValidFrom(document.getDocumentDate());
			item.setDescription(oldItem.getDescription());
			item.setDeleted(oldItem.isDeleted());
			item.setItemType(ItemType.POSITION);
			item.setItemRebate(oldItem.getDiscount());
			item.setItemNumber(oldItem.getItemnr());
			item.setName(oldItem.getName());
			// find the VAT entry
			Long vatId = newVats.get(oldItem.getVatid());
            VAT vatRef = vatId != null ?  vatsDAO.findById(vatId) : null;
			if(vatRef == null) {
				migLogUser.info("!!! no VAT " + oldItem.getVatname() + " entry for an item found or entry deleted. (old) Item ID=" + oldItem.getId());
			} else {
				item.setItemVat(vatRef);
			}
			// since we now have a reference to a valid VAT we don't need the fields "vatdescription" and "vatname"
			item.setNoVat(oldItem.isNovat());
			item.setOptional(oldItem.isOptional());
			// owner field (oldItem) contains a reference to the containing document - we don't need it
			// the "shared" field we also don't need
			item.setPicture(createImageFromFile(oldItem.getPicturename()));
			item.setPrice(roundValue(oldItem.getPrice()));
			if(oldItem.getProductid() >= 0) {
				Product prod = productsDAO.findById(newProducts.get(oldItem.getProductid()));
				item.setProduct(prod);
			}
			item.setQuantity(oldItem.getQuantity());
			item.setQuantityUnit(oldItem.getQunit());

			// only PRO version
//			item.setTara(oldItem.getTara());
//			item.setWeight(oldItem.getWeight());
			
			// the owning document is _always_ the document which was given as parameter herein
			document.addToItems(item);
		}
	}

	private byte[] createImageFromFile(String picturename) {
		if(picturename.isEmpty()) return null;
		
		byte[] buffer = null;
		String oldWorkspace = eclipsePrefs.get(
				ConfigurationManager.MIGRATE_OLD_DATA, null);
		Path oldFile = Paths.get(oldWorkspace,
				Constants.PRODUCT_PICTURE_FOLDER,
				picturename);
//		Image image = new Image(Display.getCurrent(), oldFile.toFile().getAbsolutePath());
//		return image.getImageData().data;
		if(Files.exists(oldFile)) {
			try(InputStream inputStream = Files.newInputStream(oldFile);) {
				buffer = new byte[Long.valueOf(Files.size(oldFile)).intValue()];
				IOUtils.read(inputStream, buffer);
			} catch (IOException e) {
				migLogUser.severe(String.format("can't read image for product from file '%s'!", oldFile));
			}
		}
		return buffer;
	}

	private void runMigrateContactsSubTask(SubMonitor subMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllContacts();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));

        subMonitor.setWorkRemaining(countOfEntitiesInTable.intValue());
        subMonitor.subTask(String.format("%s %d %s", msg.startMigrationWorking, countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		CategoryBuilder<ContactCategory> catBuilder = new CategoryBuilder<>(log); 
		Map<String, ContactCategory> contactCategories = catBuilder.buildCategoryMap(oldDao.findAllContactCategories(), ContactCategory.class);
		for (OldContacts oldContact : oldDao.findAllContacts()) {
			try {
				Contact contact = createBaseContactFromOldContact(false, oldContact);
                if (StringUtils.isNotEmpty(oldContact.getBankCode()) && StringUtils.isNumericSpace(oldContact.getBankCode())) {
                    BankAccount bankAccount = modelFactory.createBankAccount();
                    bankAccount.setName(oldContact.getAccount());
                    bankAccount.setAccountHolder(oldContact.getAccountHolder());

                    bankAccount.setBankCode(Integer.parseInt(oldContact.getBankCode().replaceAll(" ", ""))); // could run into trouble if bankCode contains spaces
                    bankAccount.setBankName(oldContact.getBankName());
                    bankAccount.setIban(oldContact.getIban());
                    bankAccount.setBic(oldContact.getBic());
                    bankAccount.setValidFrom(new Date());
                    contact.setBankAccount(bankAccount);
                }
				if(StringUtils.isNotBlank(oldContact.getCategory()) && contactCategories.containsKey(oldContact.getCategory())) {
					// add it to the new entity
//					contact.addToCategories(contactCategories.get(oldContact.getCategory()));
                    contact.setCategories(contactCategoriesDAO.getCategory(oldContact.getCategory(), true));
				}
				contact.setCustomerNumber(oldContact.getNr());
				contact.setDateAdded(getSaveParsedDate(oldContact.getDateAdded()));
				if(!isAddressEqualToDeliveryAdress(oldContact)) {
    				Contact deliveryContact = createBaseContactFromOldContact(true, oldContact);
    //				contact.getAlternateContacts().add(deliveryContact);
                    contact.setAlternateContacts(deliveryContact);
				}
				/*
				 * This is crucial, since there could be (undeleted) documents which have references to deleted contacts!
				 */
				contact.setDeleted(oldContact.isDeleted());
				contact.setDiscount(oldContact.getDiscount());
				contact.setEmail(oldContact.getEmail());
				contact.setFax(oldContact.getFax());
				contact.setMobile(oldContact.getMobile());
				contact.setNote(oldContact.getNote());
				if(oldContact.getPayment() > -1) {
				    contact.setPayment(paymentsDAO.findById(newPayments.get(oldContact.getPayment())));
				}
				contact.setPhone(oldContact.getPhone());
				contact.setReliability(ReliabilityType.get(oldContact.getReliability()));
				contact.setSupplierNumber(oldContact.getSuppliernumber());
				contact.setUseNetGross(Integer.valueOf(oldContact.getUseNetGross()).shortValue());
				contact.setVatNumber(oldContact.getVatnr());
				contact.setVatNumberValid(BooleanUtils.toBooleanObject(oldContact.getVatnrvalid()));
				contact.setWebsite(oldContact.getWebsite());
				contact.setMandateReference(oldContact.getMandatRef());
				contact = contactDAO.save(contact, true);
				
				// store it for further using (only ID for memory saving)
				newContacts.put(oldContact.getId(), contact.getId());
				subMonitor.worked(1);
			}
			catch (FakturamaStoringException | RuntimeException e) {
				migLogUser.warning("!!! error while migrating Contact. (old) ID=" + oldContact.getId()+"; Message: " + e.getMessage());
			}
		}
	}

    /**
     * Returns, if the address is equal to the delivery address
     * 
     * @return True, if both are equal
     */
    private boolean isAddressEqualToDeliveryAdress(OldContacts oldContact) {
        if (oldContact.getGender() != oldContact.getDeliveryGender()) { return false; }
        if (!oldContact.getDeliveryTitle().equals(oldContact.getTitle())) { return false; }
        if (!oldContact.getDeliveryFirstname().equals(oldContact.getFirstname())) { return false; }
        if (!oldContact.getDeliveryName().equals(oldContact.getName())) { return false; }
        if (!oldContact.getDeliveryCompany().equals(oldContact.getCompany())) { return false; }
        if (!oldContact.getDeliveryStreet().equals(oldContact.getStreet())) { return false; }
        if (!oldContact.getDeliveryZip().equals(oldContact.getZip())) { return false; }
        if (!oldContact.getDeliveryCity().equals(oldContact.getCity())) { return false; }
        if (!oldContact.getDeliveryCountry().equals(oldContact.getCountry())) { return false; }

        return true;
    }

	private Contact createBaseContactFromOldContact(boolean isDeliveryAddress, OldContacts oldContact) {
		Contact contact = null;
//		if(!StringUtils.isEmpty(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryName(), oldContact.getName())) 
//		        || !StringUtils.isEmpty(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryFirstname(), oldContact.getFirstname()))) {
			contact = modelFactory.createDebitor();
			contact.setContactType(isDeliveryAddress ? ContactType.DELIVERY : ContactType.BILLING);
			contact.setCompany(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryCompany(), oldContact.getCompany()));
			contact.setFirstName(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryFirstname(), oldContact.getFirstname()));
			contact.setGender(isDeliveryAddress ? oldContact.getDeliveryGender() : oldContact.getGender());
			contact.setName(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryName(), oldContact.getName()));
			contact.setTitle(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryTitle(), oldContact.getTitle()));
			contact.setValidFrom(new Date());
			contact.setBirthday(isDeliveryAddress ? getSaveParsedDate(oldContact.getBirthday()) :  getSaveParsedDate(oldContact.getDeliveryBirthday()));
//			contact.setBirthday(LocalDate.parse(oldContact.getBirthday()));
			
			// create address
			Address address = modelFactory.createAddress();
			address.setStreet(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryStreet(), oldContact.getStreet()));
			address.setCity(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryCity(), oldContact.getCity()));
			address.setZip(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryZip(), oldContact.getZip()));
			address.setValidFrom(new Date());
			// we don't have a CountryCode table :-(, therefore we have to look up in Locale classes
			String country = getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryCountry(), oldContact.getCountry());
			Optional<Locale> locale = contactUtil.determineCountryCode(country);
			if(locale.isPresent() && StringUtils.isNotBlank(locale.get().getCountry())) {
			    address.setCountryCode(locale.get().getCountry());
			} else {
			    migLogUser.info(String.format("!!! unable to determine the country for contact number [%s]", oldContact.getNr()));
			}
			contact.setAddress(address);
//		}
// else there's no delivery contact!
		return contact;
	}

	private String getDeliveryConsideredValue(boolean isDelivery, String deliveryValue, String normalValue) {
		return isDelivery ? deliveryValue : normalValue;
	}


	private Date getSaveParsedDate(String dateValue) {
		Date retval = null;
		if(StringUtils.isNotEmpty(dateValue)) {
		    if(dateValue.length() < 11) {
    			Optional<LocalDate> parsedDate = Optional.ofNullable(LocalDate.parse(dateValue));
                retval = parsedDate.isPresent() ? Date.from(parsedDate.get().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) : zeroDate.getTime();
		    } else {
		        // there're really entries such as '2014-12-27 21:16: 3' in the database... strange...
		        String correctDateTime = dateValue.replaceAll(" ", "~").replaceFirst("~", "T").replaceAll("~", "0");
		        Optional<LocalDateTime> parsedDateTime = Optional.ofNullable(LocalDateTime.parse(correctDateTime));
                retval = parsedDateTime.isPresent() ? Date.from(parsedDateTime.get().atZone(ZoneId.systemDefault()).toInstant()) : zeroDate.getTime();
		    }
		}
		return retval;
	}

	private void runMigrateReceiptvouchersSubTask(SubMonitor subMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllReceiptvouchers();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));

        subMonitor.setWorkRemaining(countOfEntitiesInTable.intValue());
        subMonitor.subTask(String.format("%s %d %s", msg.startMigrationWorking, countOfEntitiesInTable, msg.startMigration));

        CategoryBuilder<VoucherCategory> catBuilder = new CategoryBuilder<>(log); 
        Map<String, VoucherCategory> receiptVoucherAccounts = catBuilder.buildCategoryMap(oldDao.findAllReceiptvoucherCategories(), VoucherCategory.class);

		for (OldReceiptvouchers oldReceiptvoucher : oldDao.findAllReceiptvouchers()) {
			try {
				Voucher receiptVoucher = modelFactory.createVoucher();
				receiptVoucher.setVoucherType(VoucherType.RECEIPTVOUCHER);
				if(StringUtils.isNotBlank(oldReceiptvoucher.getCategory()) && receiptVoucherAccounts.containsKey(oldReceiptvoucher.getCategory())) {
					//receiptVoucher.setAccount(receiptVoucherAccounts.get(oldReceiptvoucher.getCategory()));
					receiptVoucher.setAccount(voucherCategoriesDAO.getOrCreateCategory(oldReceiptvoucher.getCategory(), true));
				}
				receiptVoucher.setDiscounted(oldReceiptvoucher.isDiscounted());
				receiptVoucher.setDeleted(oldReceiptvoucher.isDeleted());
				receiptVoucher.setDocumentNumber(oldReceiptvoucher.getDocumentnr());
				receiptVoucher.setDoNotBook(oldReceiptvoucher.isDonotbook());
				if(StringUtils.isNotEmpty(oldReceiptvoucher.getDate())) {
				    receiptVoucher.setVoucherDate(dateFormat.parse(oldReceiptvoucher.getDate()));
				}
				receiptVoucher.setVoucherNumber(oldReceiptvoucher.getNr());
				// each Receiptvoucher has its own items
				if(StringUtils.isNotBlank(oldReceiptvoucher.getItems())) {
					String[] itemRefs = oldReceiptvoucher.getItems().split(",");
					for (String itemRef : itemRefs) {
						OldReceiptvoucheritems oldReceiptvoucherItem = oldDao.findReceiptvoucherItem(itemRef);
						VoucherItem item = modelFactory.createVoucherItem();
						item.setItemVoucherType(VoucherType.RECEIPTVOUCHER);
						item.setAccountType(itemAccountTypes.get(oldReceiptvoucherItem.getCategory()));
						item.setName(oldReceiptvoucherItem.getName());
						item.setPrice(oldReceiptvoucherItem.getPrice());
						VAT newVat = vatsDAO.findById(newVats.get(oldReceiptvoucherItem.getVatid()));
						item.setVat(newVat);
						item.setValidFrom(new Date());
						receiptVoucher.addToItems(item);
					}
				}
				receiptVoucher.setName(oldReceiptvoucher.getName());
				receiptVoucher.setPaidValue(oldReceiptvoucher.getPaid());
				receiptVoucher.setTotalValue(oldReceiptvoucher.getTotal());
				receiptVoucher.setValidFrom(new Date());
				receiptVouchersDAO.save(receiptVoucher, true);
				subMonitor.worked(1);
			}
			catch (FakturamaStoringException | ParseException e) {
				migLogUser.warning("!!! error while migrating Receiptvoucher. (old) ID=" + oldReceiptvoucher.getId() + "; Message: " + e.getMessage());
			}
		}
		subMonitor.done();
	}

	private void runMigrateExpendituresSubTask(SubMonitor subMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllExpenditures();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));

        subMonitor.setWorkRemaining(countOfEntitiesInTable.intValue());
        subMonitor.subTask(String.format("%s %d %s", msg.startMigrationWorking, countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache

        @SuppressWarnings("unchecked")
		CategoryBuilder<VoucherCategory> catBuilder = ContextInjectionFactory.make(CategoryBuilder.class, context);
        Map<String, VoucherCategory> expenditureAccounts = catBuilder.buildCategoryMap(oldDao.findAllExpenditureVoucherCategories(), VoucherCategory.class);
        
		for (OldExpenditures oldExpenditure : oldDao.findAllExpenditures()) {
			try {
				Voucher Voucher = modelFactory.createVoucher();
				Voucher.setVoucherType(VoucherType.EXPENDITURE);
				if(StringUtils.isNotBlank(oldExpenditure.getCategory()) && expenditureAccounts.containsKey(oldExpenditure.getCategory())) {
					Voucher.setAccount(voucherCategoriesDAO.getOrCreateCategory(oldExpenditure.getCategory(), true));
				}
				Voucher.setDeleted(oldExpenditure.isDeleted());
				Voucher.setDiscounted(oldExpenditure.isDiscounted());
				Voucher.setDocumentNumber(oldExpenditure.getDocumentnr());
				Voucher.setDoNotBook(oldExpenditure.isDonotbook());
				Voucher.setValidFrom(new Date());
				if(StringUtils.isNotEmpty(oldExpenditure.getDate())) {
				    Date expenditureDate = dateFormat.parse(oldExpenditure.getDate());
					Voucher.setVoucherDate(expenditureDate);
				}
				Voucher.setVoucherNumber(oldExpenditure.getNr());
				// each Voucher has its own items
				if(StringUtils.isNotBlank(oldExpenditure.getItems())) {
					String[] itemRefs = oldExpenditure.getItems().split(",");
					int pos = 1;
					for (String itemRef : itemRefs) {
						OldExpenditureitems oldExpenditureItem = oldDao.findExpenditureItem(itemRef);
						VoucherItem item = modelFactory.createVoucherItem();
						item.setItemVoucherType(VoucherType.EXPENDITURE);
						item.setAccountType(itemAccountTypes.get(oldExpenditureItem.getCategory()));
						item.setDeleted(oldExpenditureItem.isDeleted());
						item.setName(oldExpenditureItem.getName());
						item.setPrice(oldExpenditureItem.getPrice());
						item.setPosNr(pos++);
						VAT newVat = vatsDAO.findById(newVats.get(oldExpenditureItem.getVatid()));
						item.setVat(newVat);
						Voucher.addToItems(item);
					}
				}
				Voucher.setName(oldExpenditure.getName());
				Voucher.setPaidValue(oldExpenditure.getPaid());
				Voucher.setTotalValue(oldExpenditure.getTotal());
				expendituresDAO.save(Voucher, true);
				subMonitor.worked(1);
			}
			catch (FakturamaStoringException | ParseException e) {
				migLogUser.warning("!!! error while migrating Voucher. (old) ID=" + oldExpenditure.getId() + "; Message: " + e.getMessage());
            }
		}
	}

	/**
	 * Builds the category map for {@link ItemListTypeCategory}. These categories 
	 * come from the old "DataSetListNames", which contained the "country codes"
	 * and the "billing account" names. Here we only migrate the "billing account"
	 * names. There's no parent-child-relationship between categories. Therefore
	 * we don't need the {@link CategoryBuilder}.
	 * 
	 * @return
	 */
	private Map<String, ItemAccountType> buildItemAccountTypeMap() {
	    if(itemAccountTypes == null) {
	        itemAccountTypes = new HashMap<String, ItemAccountType>();
	    }
		List<OldList> resultSet = oldDao.findAllVoucherItemCategories();
		// there's only one fixed category which has to be migrated ("billing_accounts")
		ItemListTypeCategory cat = modelFactory.createItemListTypeCategory();
		// the message key has to be provided here (we us it as combo value later on)
		cat.setName("data.list.accountnumbers");  // old: "billing_accounts"
		for (OldList oldVoucherItemCategory : resultSet) {
			try {
		    ItemAccountType itemAccountType = modelFactory.createItemAccountType();
		    itemAccountType.setName(oldVoucherItemCategory.getName());
		    itemAccountType.setValue(oldVoucherItemCategory.getValue());
		    itemAccountType.setDeleted(oldVoucherItemCategory.isDeleted());
		    itemAccountType.setValidFrom(new Date());
		    itemAccountType.setCategory(cat);
				itemAccountType = itemAccountTypeDAO.findOrCreate(itemAccountType);
		    // Only the name is usable for an identification because the category 
		    // name is always the same.
		    itemAccountTypes.put(oldVoucherItemCategory.getName(), itemAccountType);
		    // "refresh" cat (if it is new and didn't have any id it could else
		    // be saved again and again and again... (with every new itemAccountType)
		    if(cat.getId() == 0) cat = itemAccountType.getCategory();
		    
            } catch (FakturamaStoringException e) {
				migLogUser.info("!!! error while migrating ItemAccountTypes. (old) ID=" + oldVoucherItemCategory.getId() + "; Message: " + e.getMessage());
            }
        }
		return itemAccountTypes;
	}


    /**
     * Migration of the Payments table (DataSetPayment). The created objects will be stored in a {@link Map} for later use (only the IDs).
     * 
     * @param subMonitor the {@link SubMonitor}
     */
	private void runMigratePaymentsSubTask(SubMonitor subMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllPayments();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));

        subMonitor.setWorkRemaining(countOfEntitiesInTable.intValue());
        subMonitor.subTask(String.format("%s %d %s", msg.startMigrationWorking, countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		// Hint: Payments have the same(!) categories as Vouchers
		CategoryBuilder<VoucherCategory> catBuilder = new CategoryBuilder<>(log); 
		Map<String, VoucherCategory> paymentCategories = catBuilder.buildCategoryMap(oldDao.findAllPaymentCategories(), VoucherCategory.class);
		for (OldPayments oldPayment : oldDao.findAllPayments()) {
			try {
				Payment payment = modelFactory.createPayment();
				payment.setName(oldPayment.getName());
				payment.setPaidText(oldPayment.getPaidtext());
				payment.setUnpaidText(oldPayment.getUnpaidtext());
				payment.setDepositText(oldPayment.getDeposittext());
				// unused field since the default payment is stored in preferences
//				payment.setDefaultPaid(oldPayment.isDefaultpaid());
				payment.setDeleted(oldPayment.isDeleted()); // sometimes we need deleted entries (for some items)
				payment.setDiscountDays(oldPayment.getDiscountdays());
				payment.setDiscountValue(oldPayment.getDiscountvalue());
				payment.setDescription(oldPayment.getDescription());
				payment.setNetDays(oldPayment.getNetdays());
				payment.setValidFrom(new Date());
				if(StringUtils.isNotBlank(oldPayment.getCategory()) && paymentCategories.containsKey(oldPayment.getCategory())) {
					// add it to the new entity
					payment.setCategory(voucherCategoriesDAO.getOrCreateCategory(oldPayment.getCategory(), true));
				}
				payment = paymentsDAO.save(payment, true);
				newPayments.put(oldPayment.getId(), payment.getId());
				subMonitor.worked(1);
			}
			catch (FakturamaStoringException e) {
				migLogUser.info("!!! error while migrating Payment. (old) ID=" + oldPayment.getId() + "; Message: " + e.getMessage());
			}
		}
	}


	/**
	 * Migration of the Texts table (DataSetText)
	 * 
	 * @param subMonitor the {@link SubMonitor}
	 */
	private void runMigrateTextsSubTask(SubMonitor subMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllTexts();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));

        subMonitor.setWorkRemaining(countOfEntitiesInTable.intValue());
        subMonitor.subTask(String.format("%s %d %s", msg.startMigrationWorking, countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		CategoryBuilder<TextCategory> catBuilder = new CategoryBuilder<>(log); 
		Map<String, TextCategory> textCategoriesMap = catBuilder.buildCategoryMap(oldDao.findAllTextCategories(), TextCategory.class);
		for (OldTexts oldTexts : oldDao.findAllTexts()) {
			try {
				TextModule text = modelFactory.createTextModule();
				text.setName(oldTexts.getName());
				text.setText(oldTexts.getText());
				text.setDeleted(Boolean.FALSE);
				text.setValidFrom(new Date());
				if(StringUtils.isNotBlank(oldTexts.getCategory()) && textCategoriesMap.containsKey(oldTexts.getCategory())) {
					// add it to the new entity
//					text.addToCategories(textCategoriesMap.get(oldTexts.getCategory()));
					text.setCategories(textCategoriesMap.get(oldTexts.getCategory()));
				}
				textDAO.save(text, true);
				subMonitor.worked(1);
			}
			catch (FakturamaStoringException e) {
				migLogUser.info("!!! error while migrating Text. (old) ID=" + oldTexts.getId() + "; Message: " + e.getMessage());
			}
		}
	}

	/**
	 * Migration of the VATs table (DataSetVAT). The created objects will be stored in a {@link Map} for later use (only the IDs).
	 * 
	 * @param subMonitor the {@link SubMonitor}
	 */
	private void runMigrateVatsSubTask(SubMonitor subMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllVats();
		migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));

        subMonitor.setWorkRemaining(countOfEntitiesInTable.intValue());
        subMonitor.subTask(String.format("%s %d %s", msg.startMigrationWorking, countOfEntitiesInTable, msg.startMigration));

		CategoryBuilder<VATCategory> catBuilder = new CategoryBuilder<>(log); 
		Map<String, VATCategory> vatCategoriesMap = catBuilder.buildCategoryMap(oldDao.findAllVatCategories(), VATCategory.class);
		for (OldVats oldVat : oldDao.findAllVats()) {
			try {
				VAT vat = modelFactory.createVAT();
				vat.setName(oldVat.getName());
				vat.setTaxValue(roundValue(oldVat.getValue()));
				vat.setDeleted(oldVat.isDeleted()); // sometimes we need deleted entries (for some items)
				vat.setDescription(oldVat.getDescription());
				vat.setValidFrom(new Date());
				if(StringUtils.isNotBlank(oldVat.getCategory()) && vatCategoriesMap.containsKey(oldVat.getCategory())) {
					// add it to the new entity
				    // get VatCategory from DAO since it may not be stored
					vat.setCategory(vatCategoriesDAO.getOrCreateCategory(oldVat.getCategory(), true));
				}
				vat = vatsDAO.save(vat, true);
				// use a HashMap as a simple cache
				newVats.put(oldVat.getId(), vat.getId());
				subMonitor.worked(1);
			}
			catch (FakturamaStoringException e) {
			    migLogUser.info("!!! error while migrating VAT. (old) ID=" + oldVat.getId() + "; Message: " + e.getMessage());
			}
		}
	}

	/**
	 * Migration of the Shipping table (DataSetShipping). The created objects will be stored in a {@link Map} for later use (only the IDs).
	 * 
	 * @param subMonitor the {@link SubMonitor}
	 */
	private void runMigrateShippingsSubTask(SubMonitor subMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllShippings();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));

        subMonitor.setWorkRemaining(countOfEntitiesInTable.intValue());
        subMonitor.subTask(String.format("%s %d %s", msg.startMigrationWorking, countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		CategoryBuilder<ShippingCategory> catBuilder = new CategoryBuilder<>(log); 
		Map<String, ShippingCategory> shippingCategoriesMap = catBuilder.buildCategoryMap(oldDao.findAllShippingCategories(), ShippingCategory.class);
		for (OldShippings oldShipping : oldDao.findAllShippings()) {
			try {
				Shipping shipping = modelFactory.createShipping();
				shipping.setName(oldShipping.getName());
				shipping.setShippingValue(roundValue(oldShipping.getValue()));
				shipping.setDeleted(oldShipping.isDeleted());  // sometimes we need deleted entries (for some items)
				shipping.setAutoVat(ShippingVatType.get(oldShipping.getAutovat()));
				shipping.setDescription(oldShipping.getDescription());
				shipping.setValidFrom(new Date());
				if(StringUtils.isNotBlank(oldShipping.getCategory()) && shippingCategoriesMap.containsKey(oldShipping.getCategory())) {
					// add it to the new entity
//					shipping.addToCategories(shippingCategoriesMap.get(oldShipping.getCategory()));
                    shipping.setCategories(shippingCategoriesDAO.getCategory(oldShipping.getCategory(), true));
				}
				
				VAT newVat = vatsDAO.findById(newVats.get(oldShipping.getVatid()));
				shipping.setShippingVat(newVat);
				shipping = shippingsDAO.save(shipping, true);
				newShippings.put(oldShipping.getId(), shipping.getId());
				subMonitor.worked(1);
			}
			catch (FakturamaStoringException e) {
				migLogUser.info("!!! error while migrating Shipping. (old) ID=" + oldShipping.getId() + "; Message: " + e.getMessage());
			}
		}
	}

	/**
	 * Migration of the Properties table. Sets the properties in EclipsePreferences.
	 * 
	 * @param subMonitor
	 * @param countOfEntitiesInTable
	 * @throws InterruptedException
	 * @throws
	 */
	private void runMigratePropertiesSubTask(SubMonitor subMonitor) throws InterruptedException {
		Long countOfEntitiesInTable = oldDao.countAllProperties();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));

        subMonitor.setWorkRemaining(countOfEntitiesInTable.intValue());
        subMonitor.subTask(String.format("%s %d %s", msg.startMigrationWorking, countOfEntitiesInTable, msg.startMigration));

	    createColumnWidthPreferences();
	    String currentUser = System.getProperty("user.name", "(unknown)");
        for (OldProperties oldProperty : oldDao.findAllPropertiesWithoutColumnWidthProperties()) {
			try {
				UserProperty prop = modelFactory.createUserProperty();
				prop.setName(oldProperty.getName());
				prop.setUser(currentUser);
				String propValue = oldProperty.getValue();

				// there are only three different default entries
				// some other values have to be corrected
				switch (oldProperty.getName()) {
                case Constants.DEFAULT_VAT:
				    // if we get a default entry property we have to synchronize this with the correct entry
                    // switch propValue to the correct id
                    VAT newVat = vatsDAO.findById(newVats.get(Integer.parseInt(propValue)));
                    propValue = Long.toString(newVat.getId());
                    break;
                case Constants.DEFAULT_SHIPPING:
                    Shipping newShipping = shippingsDAO.findById(newShippings.get(Integer.parseInt(propValue)));
                    propValue = Long.toString(newShipping.getId());
                    break;
                case Constants.DEFAULT_PAYMENT:
                    Payment newPayment = paymentsDAO.findById(newPayments.get(Integer.parseInt(propValue)));
                    propValue = Long.toString(newPayment.getId());
                    break;
                case Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS:
                	propValue = Integer.toString(Integer.parseInt(propValue)+1);
                    break;
                case Constants.PREFERENCE_GENERAL_CURRENCY:
                    double exampleNumber = -1234.56864;
                    String retval = "";
                	// if the currency is stored as symbol we have to convert it to an ISO code.
           			// Money doesn't work with symbols, therefore we have to convert this.
                    // Try to determine a Locale from symbol
                    Locale currencyLocale = Locale.getDefault();
           			// the key has to be changed, too!
           			prop.setName(Constants.PREFERENCE_CURRENCY_LOCALE);
           			UserProperty propUseSymbol = modelFactory.createUserProperty();
           			propUseSymbol.setUser(currentUser);
           			propUseSymbol.setName(Constants.PREFERENCES_CURRENCY_USE_SYMBOL);
       			    propUseSymbol.setValue(Boolean.FALSE.toString());  // default
       			    
       			    // try to interpret the currency symbol
           			if(StringUtils.length(propValue) == 1) {
           			    propUseSymbol.setValue(CurrencySettingEnum.SYMBOL.name());
                        Currency jdkCurrency = Currency.getInstance(currencyLocale);
           		        String currencySymbol = jdkCurrency.getSymbol(currencyLocale);
           		        if(currencySymbol.contentEquals(propValue)) {
           		            propValue = currencyLocale.getLanguage() + "/" + currencyLocale.getCountry();
           		            migLogUser.info("!!! The currency locale was set to '" + currencyLocale.toLanguageTag()+"'. "
           		                    + "Please check this in the general settings.");
           		        } else {
           		            // Since most of the Fakturama users are from Germany we assume "de/DE" as default locale.
           		            currencyLocale = Locale.GERMANY;
           		            propValue = "de/DE";
           		            migLogUser.info("!!! Can't determine the currency locale. Please choose the right locale "
           		                    + "in the general settings dialog. Locale is temporarily set to '" +propValue + "'.");
           		        }
           			}
                    retval = DataUtils.getInstance().formatCurrency(exampleNumber, currencyLocale, 
                    		propUseSymbol.getValue().contentEquals(CurrencySettingEnum.SYMBOL.name()) 
                    		? CurrencySettingEnum.SYMBOL
                    		: CurrencySettingEnum.CODE, false, false);
                    propertiesDAO.save(propUseSymbol, true);
                    eclipsePrefs.put(propUseSymbol.getName(), propUseSymbol.getValue());
                    break;
                case Constants.PREFERENCES_BROWSER_TYPE:
                	propValue = "0";  // because setting the browser type makes a lot of trouble!
                	break;
                default:
                    break;
                }
                prop.setValue(propValue);
                propertiesDAO.save(prop, true);
                
                // save the preference in preference store
				eclipsePrefs.put(prop.getName(), prop.getValue());
				subMonitor.worked(1);
			}
			catch (FakturamaStoringException sqlex) {
				migLogUser.info("!!! error while migrating UserProperty. (old) ID=" + oldProperty.getId() + "; Message: " + sqlex.getMessage());
			}
		}
	}


    /**
     * Assume that all preferences in CAPITAL LETTERS are user preferences.
     * For ease of use we create a properties file for the column settings of the views
     * (therefore we don't have to store tons of COLUMN_* settings).
     */
    private void createColumnWidthPreferences() {
		// at first create a properties file
		String requestedWorkspace = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null);
		Path propertiesFile = Paths.get(requestedWorkspace, Constants.VIEWTABLE_PREFERENCES_FILE);
		Properties columnWidthProperties = new Properties();
		
		log.info("propertiesFile: "+propertiesFile);
        migLogUser.info("properties file for column widths: "+propertiesFile);
		if(log.isDebugEnabled()) {
			log.debug("findAllColumnWidthProperties():"+oldDao.findAllColumnWidthProperties().size());
		}
			
		// truncate and overwrite an existing file, or create the file if
		// it doesn't initially exist
		try(BufferedWriter propsWriter = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(Files.newOutputStream(propertiesFile))));) {
		    // at first we collect all COLUMNWIDTH_ properties
	        Pattern pattern = Pattern.compile("COLUMNWIDTH_([A-Z]+)_(\\w+)(\\.*)");
	        // since we have to write it on ONE line we collect all the properties for a table
	        Map<String, List<Integer>> columnWidthsMap = new HashMap<>();
            for (OldProperties oldProperty : oldDao.findAllColumnWidthProperties()) {
		        Matcher matcher = pattern.matcher(oldProperty.getName());
		        if(matcher.matches()) {
		            String table = matcher.group(1);
		            // prefix for the column width properties
		            String tableId = null;
		            switch (table) {
                    case "VATS":
                        tableId = VATListTable.ID;
                        break;
                    case "CONTACTS":
                        tableId = ContactListTable.ID;
                        break;
                    case "DOCUMENTS":
                        tableId = DocumentsListTable.ID;
                        break;
                    case "ITEMS":
                        tableId = DocumentItemListTable.ID;
                        break;
//                    case "LIST":
//                        break;
                    case "PAYMENTS":
                        tableId = PaymentListTable.ID;
                        break;
//                    case "PRODUCTS":
//                        break;
                    case "SHIPPINGS":
                        tableId = ShippingListTable.ID;
                        break;
//                    case "TEXTS":
//                        break;
//                    case "VOUCHERITEMS":
//                        break;
//                    case "VOUCHERS":
//                        break;
                    case "DIALOG":
                        // these settings can be silently ignored since we use the list table parts instead
                        // e.g., COLUMNWIDTH_DIALOG_CONTACTS_CITY
                        continue;
                    default:
                        tableId = table;
                        break;
                    }
                    List<Integer> valueList = columnWidthsMap.get(tableId);
                    if(valueList == null) {
                        valueList = new ArrayList<>();
                    }
                    valueList.add(Integer.parseInt(oldProperty.getValue()));
                    columnWidthsMap.put(tableId, valueList);
		        }
            }
            
            List<Integer> valueList;
            List<Integer> convertedValueList;
            int totalSize;
            int largestFieldIdx;
            int currentHighest;
        	Integer convertedValue;
            int convertedMaxSize;
            // now write all the collected column widths to property file
            for (String tableId : columnWidthsMap.keySet()) {
                // format: tableId.BODY.columnWidth.sizes=0\:49,1\:215,2\:90,
            	
				valueList = columnWidthsMap.get(tableId);
                StringBuilder stringBuilder = new StringBuilder();
                
                //convert old values to percentage values
				totalSize = 0;
				largestFieldIdx = 0;
                
                //calculate the total size and get the largest field
				currentHighest = 0;
                int valueListSize = valueList.size();
				for(int i = 0; i < valueListSize; i++) {
                	totalSize += valueList.get(i);
                	
                	if(valueListSize > currentHighest){
                		currentHighest = valueListSize;
                		largestFieldIdx = i;
                	}
                }
                
                //convert the values
				convertedValueList = new ArrayList<>();
				convertedMaxSize = 0;
                for(int i = 0; i < valueListSize; i++) {
					int colWidth = valueList.get(i) == 0 ? 1 : valueList.get(i);
					convertedValue = 100/(totalSize/colWidth);
                	convertedMaxSize += convertedValue;
                	convertedValueList.add(convertedValue);
                }
                
                //add rest space to the largest field
                convertedValueList.set(largestFieldIdx, convertedValueList.get(largestFieldIdx) + 100 - convertedMaxSize);
                
                for(int i = 0; i < convertedValueList.size(); i++) {
                    if(stringBuilder.length() > 0) {
                        stringBuilder.append(',');
                    }
                    stringBuilder.append(i+":"+convertedValueList.get(i));
                }
                columnWidthProperties.setProperty(tableId+".BODY.columnWidth.sizes", stringBuilder.toString());
				log.info(columnWidthProperties.toString());
            }
            columnWidthProperties.store(propsWriter, "Column widths for tables (initially migrated from old values).");
            log.info("--- old data end ----");

        }
        catch (IOException e1) {
            log.error(e1, "Error while writing ColumnWidthPreferences to " + propertiesFile.getFileName() + "; Message: " + e1.getMessage());
        }
    }
	
	/**
	 * Checks for cancellation of the long running migration job
	 * 
	 * @param monitor
	 * @throws InterruptedException
	 */
	private void checkCancel(IProgressMonitor monitor) throws InterruptedException {
		if (monitor.isCanceled()) { throw new InterruptedException(); }
	}

    /**
     * Rounds a value up to 5 digits after decimal point. This is used mostly
     * for currency values or some percentage values.
     * 
     * @param value
     *            the value to round
     * @return rounded value
     */
    private double roundValue(double value) {
        int scale = 5;
        BigDecimal b = new BigDecimal(value);
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
