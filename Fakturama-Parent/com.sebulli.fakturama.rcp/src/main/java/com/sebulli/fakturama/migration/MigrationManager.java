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
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
//import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
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
import com.sebulli.fakturama.dbconnector.OldTableinfo;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.migration.olddao.OldEntitiesDAO;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BankAccount;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
//import com.sebulli.fakturama.model.CustomDocument;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.Dunning;
import com.sebulli.fakturama.model.Expenditure;
import com.sebulli.fakturama.model.ExpenditureItem;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.ReceiptVoucher;
import com.sebulli.fakturama.model.ReceiptVoucherItem;
import com.sebulli.fakturama.model.ReliabilityType;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingCategory;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.TextCategory;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.model.UserProperty;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.model.VoucherCategory;
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
	private ContactsDAO contactDAO;
	private ContactCategoriesDAO contactCategoriesDAO;
	private DocumentsDAO documentDAO;
	private ExpendituresDAO expendituresDAO;
	private PaymentsDAO paymentsDAO;
	private ProductsDAO productsDAO;
	private ProductCategoriesDAO productCategoriesDAO;
	private PropertiesDAO propertiesDAO;
	private ReceiptVouchersDAO receiptVouchersDAO;
	private ShippingsDAO shippingsDAO;
	private ShippingCategoriesDAO shippingCategoriesDAO;
	private TextsDAO textDAO;
	private VatsDAO vatsDAO;
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
	private OldEntitiesDAO oldDao;

	private String generalWorkspace = null;
	private IApplicationContext appContext;
    private final GregorianCalendar zeroDate;

	/**
     * @param context
     * @param log
     * @param preferences
     * @param msg
     */
    public MigrationManager(IEclipseContext context, Messages msg, IEclipsePreferences eclipsePrefs) {
        this.context = context;
        appContext = context.get(IApplicationContext.class);
        this.log = context.get(org.eclipse.e4.core.services.log.Logger.class);
        this.eclipsePrefs = eclipsePrefs;
        this.generalWorkspace  = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, "");
        this.msg = msg;
        this.modelFactory = FakturamaModelPackage.MODELFACTORY;
        zeroDate = new GregorianCalendar(2000, 0, 1);
    }

    public MigrationManager() {
        zeroDate = new GregorianCalendar(2000, 0, 1);
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
		
		initMigLog();

		// initialize DAOs via EclipseContext
		// new Entities have their own DAO ;-)
		contactDAO = ContextInjectionFactory.make(ContactsDAO.class, context);
		contactCategoriesDAO = ContextInjectionFactory.make(ContactCategoriesDAO.class, context);
		documentDAO = ContextInjectionFactory.make(DocumentsDAO.class, context);
		propertiesDAO = ContextInjectionFactory.make(PropertiesDAO.class, context);
		expendituresDAO = ContextInjectionFactory.make(ExpendituresDAO.class, context);
		paymentsDAO = ContextInjectionFactory.make(PaymentsDAO.class, context);
		productsDAO = ContextInjectionFactory.make(ProductsDAO.class, context);
		productCategoriesDAO = ContextInjectionFactory.make(ProductCategoriesDAO.class, context);
		receiptVouchersDAO = ContextInjectionFactory.make(ReceiptVouchersDAO.class, context);
		shippingsDAO = ContextInjectionFactory.make(ShippingsDAO.class, context);
		shippingCategoriesDAO = ContextInjectionFactory.make(ShippingCategoriesDAO.class, context);
		vatsDAO = ContextInjectionFactory.make(VatsDAO.class, context);
        vatCategoriesDAO = ContextInjectionFactory.make(VatCategoriesDAO.class, context);
		textDAO = ContextInjectionFactory.make(TextsDAO.class, context);

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
//			OldTableinfo.Lists,
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
						monitor.setTaskName(String.format("%s %s", msg.startMigrationConvert, msg.getMessageFromKey(tableinfo.getMessageKey())));
						checkCancel(monitor);
						runMigration(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), tableinfo);
//						monitor.worked(1);
					}
					monitor.done();
				}

				/**
				 * entry point for migration of old data (db only)
				 * 
				 * @param parent the current {@link Shell}
				 * @throws BackingStoreException
				 */
				@Execute
				public void migrateOldData(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) throws BackingStoreException {
					eclipsePrefs.put("OLD_JDBC_URL", hsqlConnectionString);
					eclipsePrefs.flush();
			
					// now start a ProgressMonitorDialog for tracing the progress of migration
					ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
					try {
						IRunnableWithProgress op = new IRunnableWithProgress() {
			
							@Override
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								monitor.beginTask(msg.startMigrationBegin, orderedTasks.length);
								for (OldTableinfo tableinfo : orderedTasks) {
									monitor.setTaskName(msg.startMigrationConvert + tableinfo.name());
									checkCancel(monitor);
									runMigration(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), tableinfo);
			//						monitor.worked(1);
								}
								monitor.done();
							}
						};
						;
						progressMonitorDialog.run(true, true, op);
					}
					catch (InvocationTargetException e) {
						log.error("Fehler: ", e.getMessage());
					}
					catch (InterruptedException e) {
						// handle cancellation
						throw new OperationCanceledException();
					}
					finally {
						log.info(msg.startMigrationEnd);
					}
				}
			};
			;
			progressMonitorDialog.run(true, true, op);
		}
		catch (InvocationTargetException e) {
			log.error("Fehler: ", e.getMessage());
		}
		catch (InterruptedException e) {
			// handle cancellation
			throw new OperationCanceledException();
		}
		finally {
		    eclipsePrefs.flush();
			log.info(msg.startMigrationEnd);
		}
        String tmpStr = String.format("* %s %s", StringUtils.rightPad("Start:", 20), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        migLogUser.info(StringUtils.rightPad(tmpStr, MAX_LOGENTRY_WIDTH-1) + "*");
	}
	

	/**
	 * Initialize the information file for the user. This is done by simply using the
	 * java.util.Logging class.  
	 */
    private void initMigLog() {
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
            // TODO maybe we could write down some other useful information? version of old application?
            migLogUser.info("*" + StringUtils.repeat(' ',  MAX_LOGENTRY_WIDTH-2) + "*");
            migLogUser.info(StringUtils.repeat('*',  MAX_LOGENTRY_WIDTH));
            migLogUser.info(" ");
        }
        catch (SecurityException | IOException e) {
            log.error(e, "error creating migration user info file.");
        }
    }

    /**
	 * This method switches to the migration methods.
	 * 
	 * @param subProgressMonitor {@link SubProgressMonitor}
	 * @param tableinfo tableinfo which table has to be migrated
	 * @throws InterruptedException
	 */
	private void runMigration(SubProgressMonitor subProgressMonitor, OldTableinfo tableinfo) throws InterruptedException {
	    migLogUser.info(String.format("Start converting %s ", msg.getMessageFromKey(tableinfo.getMessageKey())));
		try {
			switch (tableinfo) {
			case Properties:
				runMigratePropertiesSubTask(subProgressMonitor);
				break;
			case Shippings:
				runMigrateShippingsSubTask(subProgressMonitor);
				break;
			case Vats:
				runMigrateVatsSubTask(subProgressMonitor);
				break;
			case Lists:
				// Country Codes are no user definable data; they will be read
				// from java Locale.
				// The Account entries are converted while migrating the Vouchers.
				break;
			case Texts:
				runMigrateTextsSubTask(subProgressMonitor);
				break;
			case Expenditures:
				runMigrateExpendituresSubTask(subProgressMonitor);
				break;
			case Contacts:
				runMigrateContactsSubTask(subProgressMonitor);
				break;
			case Documents:
				runMigrateDocumentsSubTask(subProgressMonitor);
				break;
			case Payments:
				runMigratePaymentsSubTask(subProgressMonitor);
				break;
			case Products:
				runMigrateProductsSubTask(subProgressMonitor);
				break;
			case Receiptvouchers:
				runMigrateReceiptvouchersSubTask(subProgressMonitor);
				break;
			default:
				break;
			}
	        migLogUser.info(String.format("End converting %s%n", msg.getMessageFromKey(tableinfo.getMessageKey())));
		}
		catch (RuntimeException rex) {
			log.error(rex, "error while migrating "+tableinfo.name()+"; Reason: " + rex.getMessage());
		}
	}


	private void runMigrateProductsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllProducts();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
        subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		CategoryBuilder<ProductCategory> catBuilder = new CategoryBuilder<>(log); 
		Map<String, ProductCategory> productCategories = catBuilder.buildCategoryMap(oldDao.findAllProductCategories(), ProductCategory.class);
		for (OldProducts oldProduct : oldDao.findAllProducts()) {
			try {
				Product product = modelFactory.createProduct();
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
					product.addToCategories(productCategoriesDAO.getCategory(oldProduct.getCategory(), true));
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
				
				product = productsDAO.save(product);
				newProducts.put(oldProduct.getId(), product.getId());
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				log.error("error while migrating Product. (old) ID=" + oldProduct.getId() + "; Message: " + e.getMessage());
			}
		}
		subProgressMonitor.done();
	}


	/**
	 * Handles the copying of the product pictures
	 * @param product
	 * @param oldProduct
	 */
	private void copyProductPicture(Product product, OldProducts oldProduct) {
		product.setPictureName(oldProduct.getPicturename());
		if (StringUtils.isNoneEmpty(oldProduct.getPicturename())) {

			// First of all check, if the output file already exists.
			Path outputFile = Paths.get(generalWorkspace,
					Constants.PRODUCT_PICTURE_FOLDER,
					oldProduct.getPicturename());
			if (Files.exists(outputFile)) {
				return;
			}

			try {
				String oldWorkspace = eclipsePrefs.get(
						ConfigurationManager.MIGRATE_OLD_DATA, null);
				Path oldFile = Paths.get(oldWorkspace,
						Constants.PRODUCT_PICTURE_FOLDER,
						oldProduct.getPicturename());

				// Create the destination folder to store the file
				if (!Files.isDirectory(Paths.get(generalWorkspace,
						Constants.PRODUCT_PICTURE_FOLDER)))
					Files.createDirectories(outputFile);
				Files.copy(oldFile, outputFile);
			} catch (IOException e) {
				log.error("error while copying product picture for product [" + oldProduct.getId() + "] from old workspace. Reason: "+e.getMessage());
				migLogUser.info("!!! error while copying product picture for product [" + oldProduct.getId() + "] from old workspace. Reason:\n"+e.getMessage());
			}
		}
	}

	private void runMigrateDocumentsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllDocuments();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
        subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		Map<Integer, Document> invoiceRelevantDocuments = new HashMap<>();
		Map<Integer, Document> invoiceDocuments = new HashMap<>();
		for (OldDocuments oldDocument : oldDao.findAllDocuments()) {
			try {
				Document document;
				switch (BillingType.get(oldDocument.getCategory())) {
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
					document.setManualAddress(oldDocument.getAddress());
				} else {
					// use the previous filled Contact hashmap
					Contact contact = contactDAO.findById(newContacts.get(oldDocument.getAddressid()));
					if(contact != null) {
						document.setContact(contact);
					}
				}
				document.setAddressFirstLine(oldDocument.getAddressfirstline());
				document.setBillingType(BillingType.get(oldDocument.getCategory()));
				document.setCustomerRef(oldDocument.getCustomerref());
				document.setDeleted(oldDocument.isDeleted());
				// delivery address? got from contact? Assume that it's equal to contact address 
				// as long there's no delivery address stored 
				document.setDueDays(oldDocument.getDuedays());
				// store the pair for later processing
				// if the old Document has an InvoiceId and that ID is the same as the Document's ID
				// then we have to store it for further processing.
				// if the Invoice Id is the same as the document's id then it's the invoice itself
				if(oldDocument.getInvoiceid() >= 0) {
					if (oldDocument.getId() != oldDocument.getInvoiceid()) {
						invoiceRelevantDocuments.put(oldDocument.getId(), document);
					} else {
						invoiceDocuments.put(oldDocument.getId(), document);
					}
				}
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
				document.setPaidValue(oldDocument.getPayvalue());
				document.setTotalValue(oldDocument.getTotal());
				document.setTransactionId(new Long(oldDocument.getTransaction()));
				document.setWebshopDate(getSaveParsedDate(oldDocument.getWebshopdate()));
				document.setWebshopId(oldDocument.getWebshopid());
				document.setProgress(oldDocument.getProgress());
				
				// find the Shipping entry
				Shipping newShipping = shippingsDAO.findById(newShippings.get(oldDocument.getShippingid()));
				document.setShipping(newShipping);
                document.setShippingAutoVat(ShippingVatType.get(oldDocument.getShippingautovat()));
                document.setShippingValue(oldDocument.getShipping());

				documentDAO.save(document);
				subProgressMonitor.worked(1);
			}
			catch (SQLException | NumberFormatException e) {
				log.error("error while migrating Document. (old) ID=" + oldDocument.getId());
			}
		}
		// second pass...
		
		// the reference to the source document cannot be set before all documents are stored
		for (OldDocuments oldDocument : oldDao.findAllInvoiceRelatedDocuments()) {
			try {
				// invoiceRelevantDocuments now contains all Documents that needs to have an Invoice reference
				Document document = invoiceRelevantDocuments.get(oldDocument.getId());
				// now find the corresponding NEW document
				Document relatedDocument = invoiceDocuments.get(oldDocument.getInvoiceid());
				if (relatedDocument != null) {
					document.setSourceDocument((Document) relatedDocument);
					documentDAO.save(document);
				}
			}
			catch (SQLException e) {
				log.error("error while migrating Document. (old) ID=" + oldDocument.getId());
			}

		}
		subProgressMonitor.done();
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
			// the position was formerly determined through the order how they stay in documents entry
			item.setPosNr(Integer.valueOf(i));
			item.setDescription(oldItem.getDescription());
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
			item.setPictureName(oldItem.getPicturename());
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

	private void runMigrateContactsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllContacts();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		CategoryBuilder<ContactCategory> catBuilder = new CategoryBuilder<>(log); 
		Map<String, ContactCategory> contactCategories = catBuilder.buildCategoryMap(oldDao.findAllContactCategories(), ContactCategory.class);
		for (OldContacts oldContact : oldDao.findAllContacts()) {
			try {
				Contact contact = createBaseContactFromOldContact(false, oldContact);
                if (StringUtils.isNotEmpty(oldContact.getBankCode()) && StringUtils.isNumericSpace(oldContact.getBankCode())) {
                    BankAccount bankAccount = modelFactory.createBankAccount();
                    bankAccount.setAccount(oldContact.getAccount());
                    bankAccount.setAccountHolder(oldContact.getAccountHolder());

                    bankAccount.setBankCode(Integer.parseInt(oldContact.getBankCode().replaceAll(" ", ""))); // could run into trouble if bankCode contains spaces
                    bankAccount.setBankName(oldContact.getBankName());
                    bankAccount.setIban(oldContact.getIban());
                    bankAccount.setBic(oldContact.getBic());
                    contact.setBankAccount(bankAccount);
                }
				contact.setBirthday(getSaveParsedDate(oldContact.getBirthday()));
//				contact.setBirthday(LocalDate.parse(oldContact.getBirthday()));
				if(StringUtils.isNotBlank(oldContact.getCategory()) && contactCategories.containsKey(oldContact.getCategory())) {
					// add it to the new entity
//					contact.addToCategories(contactCategories.get(oldContact.getCategory()));
                    contact.setCategories(contactCategoriesDAO.getCategory(oldContact.getCategory(), true));
				}
				contact.setCustomerNumber(oldContact.getNr());
				contact.setDateAdded(getSaveParsedDate(oldContact.getDateAdded()));
				if(!isAddressEqualToDeliveryAdress(oldContact)) {
    				Contact deliveryContact = createBaseContactFromOldContact(true, oldContact);
    //				contact.getDeliveryContacts().add(deliveryContact);
                    contact.setDeliveryContacts(deliveryContact);
				}
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
				contact = contactDAO.save(contact);
				
				// store it for further using (only ID for memory saving)
				newContacts.put(oldContact.getId(), contact.getId());
				subProgressMonitor.worked(1);
			}
			catch (SQLException | RuntimeException e) {
				migLogUser.info("!!! error while migrating Contact. (old) ID=" + oldContact.getId()+"; Reason: " + e.getMessage());
			}
		}
		subProgressMonitor.done();
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
		if(!StringUtils.isEmpty(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryName(), oldContact.getName())) || !StringUtils.isEmpty(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryFirstname(), oldContact.getFirstname()))) {
			contact = modelFactory.createDebitor();
			contact.setCompany(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryCompany(), oldContact.getCompany()));
			contact.setFirstName(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryFirstname(), oldContact.getFirstname()));
			contact.setGender(isDeliveryAddress ? oldContact.getDeliveryGender() : oldContact.getGender());
			contact.setName(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryName(), oldContact.getName()));
			contact.setTitle(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryTitle(), oldContact.getTitle()));
			
			// create address
			Address address = modelFactory.createAddress();
			address.setStreet(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryStreet(), oldContact.getStreet()));
			address.setCity(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryCity(), oldContact.getCity()));
			address.setZip(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryZip(), oldContact.getZip()));
			// at this stage there're no entries in CountryCode table :-(
	//		CountryCode country = countryCodesDAO.findByLongName(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryCountry(), oldContact.getCountry()));
	//		address.setCountry(country);
			contact.setAddress(address);
		}
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

	private void runMigrateReceiptvouchersSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllReceiptvouchers();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));

        CategoryBuilder<VoucherCategory> catBuilder = new CategoryBuilder<>(log); 
        Map<String, VoucherCategory> receiptVoucherAccounts = catBuilder.buildCategoryMap(oldDao.findAllReceiptvoucherCategories(), VoucherCategory.class);

		Map<String, ItemAccountType> receiptVoucherItemAccountTypes = buildItemAccountTypeMap();
		for (OldReceiptvouchers oldReceiptvoucher : oldDao.findAllReceiptvouchers()) {
			try {
				ReceiptVoucher receiptVoucher = modelFactory.createReceiptVoucher();
				if(StringUtils.isNotBlank(oldReceiptvoucher.getCategory()) && receiptVoucherAccounts.containsKey(oldReceiptvoucher.getCategory())) {
					receiptVoucher.setAccount(receiptVoucherAccounts.get(oldReceiptvoucher.getCategory()));
				}
				receiptVoucher.setDiscounted(oldReceiptvoucher.isDiscounted());
				receiptVoucher.setDeleted(oldReceiptvoucher.isDeleted());
				receiptVoucher.setDocumentNumber(oldReceiptvoucher.getDocumentnr());
				receiptVoucher.setDoNotBook(oldReceiptvoucher.isDonotbook());
				if(StringUtils.isNotEmpty(oldReceiptvoucher.getDate())) {
					LocalDate receiptVoucherDate = LocalDate.parse(oldReceiptvoucher.getDate());
					receiptVoucher.setReceiptVoucherDate(Date.from(receiptVoucherDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
				}
				receiptVoucher.setReceiptVoucherNumber(oldReceiptvoucher.getNr());
				// each Expenditure has its own items
				if(StringUtils.isNotBlank(oldReceiptvoucher.getItems())) {
					String[] itemRefs = oldReceiptvoucher.getItems().split(",");
					for (String itemRef : itemRefs) {
						OldReceiptvoucheritems oldReceiptvoucherItem = oldDao.findReceiptvoucherItem(itemRef);
						ReceiptVoucherItem item = modelFactory.createReceiptVoucherItem();
						item.setAccount(receiptVoucherItemAccountTypes.get(oldReceiptvoucherItem.getCategory()));
						item.setName(oldReceiptvoucherItem.getName());
						item.setPrice(oldReceiptvoucherItem.getPrice());
						VAT newVat = vatsDAO.findById(newVats.get(oldReceiptvoucherItem.getVatid()));
						item.setReceiptVoucherItemVat(newVat);
//						receiptVoucher.addToItems(item);
					}
				}
				receiptVoucher.setName(oldReceiptvoucher.getName());
				receiptVoucher.setPaidValue(oldReceiptvoucher.getPaid());
				receiptVoucher.setTotalValue(oldReceiptvoucher.getTotal());
				receiptVouchersDAO.save(receiptVoucher);
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				migLogUser.info("!!! error while migrating Receiptvoucher. (old) ID=" + oldReceiptvoucher.getId() + "; Message: " + e.getMessage());
			}
		}
		subProgressMonitor.done();
	}

	private void runMigrateExpendituresSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllPayments();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache

        CategoryBuilder<VoucherCategory> catBuilder = new CategoryBuilder<>(log); 
        Map<String, VoucherCategory> expenditureAccounts = catBuilder.buildCategoryMap(oldDao.findAllReceiptvoucherCategories(), VoucherCategory.class);

        Map<String, ItemAccountType> expenditureItemAccountTypes = buildItemAccountTypeMap();
		for (OldExpenditures oldExpenditure : oldDao.findAllExpenditures()) {
			try {
				Expenditure expenditure = modelFactory.createExpenditure();
				if(StringUtils.isNotBlank(oldExpenditure.getCategory()) && expenditureAccounts.containsKey(oldExpenditure.getCategory())) {
					expenditure.setAccount(expenditureAccounts.get(oldExpenditure.getCategory()));
				}
				expenditure.setDeleted(oldExpenditure.isDeleted());
				expenditure.setDiscounted(oldExpenditure.isDiscounted());
				expenditure.setDocumentNumber(oldExpenditure.getDocumentnr());
				expenditure.setDoNotBook(oldExpenditure.isDonotbook());
				if(StringUtils.isNotEmpty(oldExpenditure.getDate())) {
				    LocalDate parsedDate = LocalDate.parse(oldExpenditure.getDate());
					Date expenditureDate = Date.from(parsedDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
					expenditure.setExpenditureDate(expenditureDate);
				}
				expenditure.setExpenditureNumber(oldExpenditure.getNr());
				// each Expenditure has its own items
				if(StringUtils.isNotBlank(oldExpenditure.getItems())) {
					String[] itemRefs = oldExpenditure.getItems().split(",");
					for (String itemRef : itemRefs) {
						OldExpenditureitems oldExpenditureItem = oldDao.findExpenditureItem(itemRef);
						ExpenditureItem item = modelFactory.createExpenditureItem();
						item.setAccount(expenditureItemAccountTypes.get(oldExpenditureItem.getCategory()));
						item.setName(oldExpenditureItem.getName());
						item.setPrice(oldExpenditureItem.getPrice());
						VAT newVat = vatsDAO.findById(newVats.get(oldExpenditureItem.getVatid()));
						item.setVat(newVat);
						expenditure.addToItems(item);
					}
				}
				expenditure.setName(oldExpenditure.getName());
				expenditure.setPaidValue(oldExpenditure.getPaid());
				expenditure.setTotalValue(oldExpenditure.getTotal());
				expendituresDAO.save(expenditure);
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				migLogUser.info("!!! error while migrating Expenditure. (old) ID=" + oldExpenditure.getId());
			}
		}
		subProgressMonitor.done();
	}


	private Map<String, ItemAccountType> buildItemAccountTypeMap() {
		Map<String, ItemAccountType> itemAccountTypes = new HashMap<String, ItemAccountType>();
		List<OldList> resultSet = oldDao.findAllVoucherItemCategories();
		for (OldList oldVoucherItemCategory : resultSet) {
		    ItemAccountType itemAccountType = modelFactory.createItemAccountType();
		    itemAccountType.setName(oldVoucherItemCategory.getName());
		    itemAccountType.setValue(oldVoucherItemCategory.getValue());
		    itemAccountTypes.put(oldVoucherItemCategory.getName(), itemAccountType);
        }
		return itemAccountTypes;
	}


    /**
     * Migration of the Payments table (DataSetPayment). The created objects will be stored in a {@link Map} for later use (only the IDs).
     * 
     * @param subProgressMonitor the {@link SubProgressMonitor}
     */
	private void runMigratePaymentsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllPayments();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
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
				if(StringUtils.isNotBlank(oldPayment.getCategory()) && paymentCategories.containsKey(oldPayment.getCategory())) {
					// add it to the new entity
					payment.setCategory(paymentCategories.get(oldPayment.getCategory()));
				}
				payment = paymentsDAO.save(payment);
				newPayments.put(oldPayment.getId(), payment.getId());
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				migLogUser.info("!!! error while migrating Payment. (old) ID=" + oldPayment.getId());
			}
		}
		subProgressMonitor.done();
	}


	/**
	 * Migration of the Texts table (DataSetText)
	 * 
	 * @param subProgressMonitor the {@link SubProgressMonitor}
	 */
	private void runMigrateTextsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllTexts();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		CategoryBuilder<TextCategory> catBuilder = new CategoryBuilder<>(log); 
		Map<String, TextCategory> textCategoriesMap = catBuilder.buildCategoryMap(oldDao.findAllTextCategories(), TextCategory.class);
		for (OldTexts oldTexts : oldDao.findAllTexts()) {
			try {
				TextModule text = modelFactory.createTextModule();
				text.setName(oldTexts.getName());
				text.setText(oldTexts.getText());
				text.setDeleted(Boolean.FALSE);
				if(StringUtils.isNotBlank(oldTexts.getCategory()) && textCategoriesMap.containsKey(oldTexts.getCategory())) {
					// add it to the new entity
					text.addToCategories(textCategoriesMap.get(oldTexts.getCategory()));
				}
				textDAO.save(text);
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				migLogUser.info("!!! error while migrating Text. (old) ID=" + oldTexts.getId());
			}
		}
		subProgressMonitor.done();
	}

	/**
	 * Migration of the VATs table (DataSetVAT). The created objects will be stored in a {@link Map} for later use (only the IDs).
	 * 
	 * @param subProgressMonitor the {@link SubProgressMonitor}
	 */
	private void runMigrateVatsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllVats();
		migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		CategoryBuilder<VATCategory> catBuilder = new CategoryBuilder<>(log); 
		Map<String, VATCategory> vatCategoriesMap = catBuilder.buildCategoryMap(oldDao.findAllVatCategories(), VATCategory.class);
		for (OldVats oldVat : oldDao.findAllVats()) {
			try {
				VAT vat = modelFactory.createVAT();
				vat.setName(oldVat.getName());
				vat.setTaxValue(roundValue(oldVat.getValue()));
				vat.setDeleted(oldVat.isDeleted()); // sometimes we need deleted entries (for some items)
				vat.setDescription(oldVat.getDescription());
				if(StringUtils.isNotBlank(oldVat.getCategory()) && vatCategoriesMap.containsKey(oldVat.getCategory())) {
					// add it to the new entity
				    // get VatCategory from DAO since it may not be stored
					vat.setCategory(vatCategoriesDAO.getOrCreateCategory(oldVat.getCategory(), true));
				}
				vat = vatsDAO.save(vat);
				// use a HashMap as a simple cache
				newVats.put(oldVat.getId(), vat.getId());
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
			    migLogUser.info("!!! error while migrating VAT. (old) ID=" + oldVat.getId()+"; Reason: " + e.getMessage());
			}
		}
		subProgressMonitor.done();
	}

	/**
	 * Migration of the Shipping table (DataSetShipping). The created objects will be stored in a {@link Map} for later use (only the IDs).
	 * 
	 * @param subProgressMonitor the {@link SubProgressMonitor}
	 */
	private void runMigrateShippingsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllShippings();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
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
				if(StringUtils.isNotBlank(oldShipping.getCategory()) && shippingCategoriesMap.containsKey(oldShipping.getCategory())) {
					// add it to the new entity
//					shipping.addToCategories(shippingCategoriesMap.get(oldShipping.getCategory()));
                    shipping.setCategories(shippingCategoriesDAO.getCategory(oldShipping.getCategory(), true));
				}
				
				VAT newVat = vatsDAO.findById(newVats.get(oldShipping.getVatid()));
				shipping.setShippingVat(newVat);
				shipping = shippingsDAO.save(shipping);
				newShippings.put(oldShipping.getId(), shipping.getId());
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				migLogUser.info("!!! error while migrating Shipping. (old) ID=" + oldShipping.getId());
			}
		}
		subProgressMonitor.done();
	}

	/**
	 * Migration of the Properties table. Sets the properties in EclipsePreferences.
	 * 
	 * @param subProgressMonitor
	 * @param countOfEntitiesInTable
	 * @throws InterruptedException
	 * @throws
	 */
	private void runMigratePropertiesSubTask(SubProgressMonitor subProgressMonitor) throws InterruptedException {
		Long countOfEntitiesInTable = oldDao.countAllProperties();
        migLogUser.info(String.format("Number of entities: %d", countOfEntitiesInTable));
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));

	    createColumnWidthPreferences();
	    
        for (OldProperties oldProperty : oldDao.findAllPropertiesWithoutColumnWidthProperties()) {
			try {
				UserProperty prop = modelFactory.createUserProperty();
				prop.setName(oldProperty.getName());
				String propValue = oldProperty.getValue();

				// there are only three different default entries
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
           			propUseSymbol.setName(Constants.PREFERENCES_CURRENCY_USE_SYMBOL);
       			    propUseSymbol.setValue(Boolean.FALSE.toString());  // default
       			    
       			    // try to interpret the currency symbol
           			if(StringUtils.length(propValue) == 1) {
           			    propUseSymbol.setValue(Boolean.TRUE.toString());
                        Currency jdkCurrency = Currency.getInstance(currencyLocale);
           		        String currencySymbol = jdkCurrency.getSymbol(currencyLocale);
           		        if(currencySymbol.contentEquals(propValue)) {
           		            propValue = currencyLocale.getLanguage() + "/" + currencyLocale.getCountry();
           		            migLogUser.info("!!! The currency locale was set to '" + currencyLocale.toLanguageTag()+"'. "
           		                    + "Please check this in the general settings.");
           		        } else {
           		            // Since most of the Fakturama users are from Germany we choose "de/DE" as default locale.
           		            currencyLocale = Locale.GERMANY;
           		            propValue = "de/DE";
           		            migLogUser.info("!!! Can't determine the currency locale. Please choose the right locale "
           		                    + "in the general settings dialog. Locale is temporarily set to '" +propValue + "'.");
           		        }
           			}
                    retval = DataUtils.getInstance().formatCurrency(exampleNumber, currencyLocale, Boolean.parseBoolean(propUseSymbol.getValue()));
                    UserProperty propFormatExample = modelFactory.createUserProperty();
                    propFormatExample.setName(Constants.PREFERENCE_CURRENCY_FORMAT_EXAMPLE);
                    propFormatExample.setValue(retval);
                    propertiesDAO.save(propUseSymbol);
                    eclipsePrefs.put(propUseSymbol.getName(), propUseSymbol.getValue());
                    propertiesDAO.save(propFormatExample);
                    eclipsePrefs.put(propFormatExample.getName(), propFormatExample.getValue());
               	break;
                default:
                    break;
                }
                prop.setValue(propValue);
                propertiesDAO.save(prop);
                
                // save the preference in preference store
				eclipsePrefs.put(prop.getName(), prop.getValue());
				subProgressMonitor.worked(1);
			}
			catch (SQLException sqlex) {
				migLogUser.info("!!! error while migrating UserProperty. (old) ID=" + oldProperty.getId());
			}
		}
		subProgressMonitor.done();
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
        log.info("properties file for column widths: "+propertiesFile);
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
                    case "LIST":
                        break;
                    case "PAYMENTS":
                        tableId = PaymentListTable.ID;
                        break;
                    case "PRODUCTS":
                        break;
                    case "SHIPPINGS":
                        tableId = ShippingListTable.ID;
                        break;
                    case "TEXTS":
                        break;
                    case "VOUCHERITEMS":
                        break;
                    case "VOUCHERS":
                        break;
                    case "DIALOG":
                        // these settings can be silently ignored since we use the list table parts instead
                        // e.g., COLUMNWIDTH_DIALOG_CONTACTS_CITY
                        continue;
                    default:
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
				convertedValueList = new ArrayList<Integer>();
				convertedMaxSize = 0;
                for(int i = 0; i < valueListSize; i++) {
					convertedValue = 100/(totalSize/valueList.get(i));
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
            log.error(e1, "Error while writing ColumnWidthPreferences to " + propertiesFile.getFileName());
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
