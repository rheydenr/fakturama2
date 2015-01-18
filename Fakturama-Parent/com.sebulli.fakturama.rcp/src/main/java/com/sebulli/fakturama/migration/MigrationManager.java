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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dao.PropertiesDAO;
import com.sebulli.fakturama.dao.ReceiptVouchersDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.TextsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dbconnector.OldTableinfo;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.migration.olddao.OldEntitiesDAO;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Account;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Confirmation;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.Credit;
import com.sebulli.fakturama.model.Delivery;
//import com.sebulli.fakturama.model.CustomDocument;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.Dunning;
import com.sebulli.fakturama.model.Expenditure;
import com.sebulli.fakturama.model.ExpenditureItem;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.Letter;
import com.sebulli.fakturama.model.Offer;
import com.sebulli.fakturama.model.Order;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.PaymentCategory;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.Proforma;
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
import com.sebulli.fakturama.oldmodel.OldContacts;
import com.sebulli.fakturama.oldmodel.OldDocuments;
import com.sebulli.fakturama.oldmodel.OldExpenditureitems;
import com.sebulli.fakturama.oldmodel.OldExpenditures;
import com.sebulli.fakturama.oldmodel.OldItems;
import com.sebulli.fakturama.oldmodel.OldPayments;
import com.sebulli.fakturama.oldmodel.OldProducts;
import com.sebulli.fakturama.oldmodel.OldProperties;
import com.sebulli.fakturama.oldmodel.OldReceiptvoucheritems;
import com.sebulli.fakturama.oldmodel.OldReceiptvouchers;
import com.sebulli.fakturama.oldmodel.OldShippings;
import com.sebulli.fakturama.oldmodel.OldTexts;
import com.sebulli.fakturama.oldmodel.OldVats;
import com.sebulli.fakturama.startup.ConfigurationManager;
import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;
import com.sebulli.fakturama.views.datatable.shippings.ShippingListTable;
import com.sebulli.fakturama.views.datatable.vats.VATListTable;

/**
 * Migration Tool for converting old data to new one. This affects only database
 * data, not templates or other plain files.
 * 
 * @author R. Heydenreich
 * 
 */
public class MigrationManager {
	private static final int VOUCHERTYPE_RECEIPT = 1;
	private static final int VOUCHERTYPE_EXPENDITURE = 2;

	@Inject
	private IEclipseContext context;

	@Inject
	private Logger log;

	private IEclipsePreferences eclipsePrefs;
	
	@Inject
	@Translation
	protected Messages msg;

	/*
	 * all available DAO classes
	 */
	private ContactsDAO contactDAO;
	private DocumentsDAO documentDAO;
	private ExpendituresDAO expendituresDAO;
	private PaymentsDAO paymentsDAO;
	private ProductsDAO productsDAO;
	private PropertiesDAO propertiesDAO;
	private ReceiptVouchersDAO receiptVouchersDAO;
	private ShippingsDAO shippingsDAO;
	private TextsDAO textDAO;
	private VatsDAO vatsDAO;
	
	// These Maps are for assigning the old entities to the new one (needed for lookup)
	private Map<Integer, Long> newContacts = new HashMap<Integer, Long>();
	private Map<Integer, Long> newVats     = new HashMap<Integer, Long>();
	private Map<Integer, Long> newShippings =new HashMap<Integer, Long>();
	private Map<Integer, Long> newPayments = new HashMap<Integer, Long>();
	private Map<Integer, Long> newProducts = new HashMap<Integer, Long>();
	
	/*
	 * there's only one DAO for old data
	 */
	private OldEntitiesDAO oldDao;

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private String generalWorkspace = null;
	private IApplicationContext appContext;

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
        this.eclipsePrefs = eclipsePrefs; //context.get(IEclipsePreferences.class);
        this.generalWorkspace  = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, "");
        this.msg = msg;
    }

    public MigrationManager() {
	}

	/**
	 * entry point for migration of old data (db only)
	 * 
	 * @param parent the current {@link Shell}
	 * @throws BackingStoreException
	 */
	@Execute
	public void migrateOldData(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) throws BackingStoreException {
		// build jdbc connection string; can't be null at this point since we've checked it above (within the caller)
		final String oldWorkDir = eclipsePrefs.get(ConfigurationManager.MIGRATE_OLD_DATA, null);
		// hard coded old database name
//		final String hsqlConnectionString = "jdbc:hsqldb:file:" + oldWorkDir + "/Database/Database";
		
		// ****** FIXME TEST ONLY
		final String hsqlConnectionString = "jdbc:hsqldb:hsql://localhost/Fakturama";   // for connection with a HSQLDB Server
		eclipsePrefs.put("OLD_JDBC_URL", hsqlConnectionString);
		eclipsePrefs.flush();

		// initialize DAOs via EclipseContext
		// new Entities have their own dao ;-)
		contactDAO = ContextInjectionFactory.make(ContactsDAO.class, context);
		documentDAO = ContextInjectionFactory.make(DocumentsDAO.class, context);
		propertiesDAO = ContextInjectionFactory.make(PropertiesDAO.class, context);
		expendituresDAO = ContextInjectionFactory.make(ExpendituresDAO.class, context);
		paymentsDAO = ContextInjectionFactory.make(PaymentsDAO.class, context);
		productsDAO = ContextInjectionFactory.make(ProductsDAO.class, context);
		receiptVouchersDAO = ContextInjectionFactory.make(ReceiptVouchersDAO.class, context);
		shippingsDAO = ContextInjectionFactory.make(ShippingsDAO.class, context);
		vatsDAO = ContextInjectionFactory.make(VatsDAO.class, context);
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
						monitor.setTaskName(msg.startMigrationConvert +" " + msg.getMessageFromKey(tableinfo.getMessageKey()));
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
			
					// initialize DAOs via EclipseContext
					// new Entities have their own dao ;-)
					
					// TODO checken, ob das wirklich sein muß (zweimal initialisieren???)
					contactDAO = ContextInjectionFactory.make(ContactsDAO.class, context);
					documentDAO = ContextInjectionFactory.make(DocumentsDAO.class, context);
					propertiesDAO = ContextInjectionFactory.make(PropertiesDAO.class, context);
					expendituresDAO = ContextInjectionFactory.make(ExpendituresDAO.class, context);
					paymentsDAO = ContextInjectionFactory.make(PaymentsDAO.class, context);
					productsDAO = ContextInjectionFactory.make(ProductsDAO.class, context);
					receiptVouchersDAO = ContextInjectionFactory.make(ReceiptVouchersDAO.class, context);
					shippingsDAO = ContextInjectionFactory.make(ShippingsDAO.class, context);
					vatsDAO = ContextInjectionFactory.make(VatsDAO.class, context);
					textDAO = ContextInjectionFactory.make(TextsDAO.class, context);
			
					// old entities only have one DAO for all entities
					oldDao = ContextInjectionFactory.make(OldEntitiesDAO.class, context);
			
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
	}
	

	/**
	 * This method switches to the migration methods.
	 * 
	 * @param subProgressMonitor {@link SubProgressMonitor}
	 * @param tableinfo tableinfo which table has to be migrated
	 * @throws InterruptedException
	 */
	private void runMigration(SubProgressMonitor subProgressMonitor, OldTableinfo tableinfo) throws InterruptedException {
		
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
		}
		catch (RuntimeException rex) {
			log.error(rex, "error while migrating "+tableinfo.name()+"; Reason: " + rex.getMessage());
		}
	}


	private void runMigrateProductsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllProducts();
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(" " + countOfEntitiesInTable + " " + msg.startMigration);
		CategoryBuilder<ProductCategory> catBuilder = new CategoryBuilder<ProductCategory>(); 
		Map<String, ProductCategory> productCategories = catBuilder.buildCategoryMap(oldDao.findAllProductCategories(), ProductCategory.class);
		for (OldProducts oldProduct : oldDao.findAllProducts()) {
			try {
				Product product = new Product();
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
					product.addToCategories(productCategories.get(oldProduct.getCategory()));
				}
				product.setItemNumber(oldProduct.getItemnr());
				product.setName(oldProduct.getName());
				// copy the old product picture into the new workspace
				copyProductPicture(product, oldProduct);
				product.setPrice1(oldProduct.getPrice1());
				product.setPrice2(oldProduct.getPrice2());
				product.setPrice3(oldProduct.getPrice3());
				product.setPrice4(oldProduct.getPrice4());
				product.setPrice5(oldProduct.getPrice5());
				product.setQuantity(oldProduct.getQuantity());
				product.setQuantityUnit(oldProduct.getQunit());
				product.setSellingUnit(oldProduct.getUnit());
				// find the VAT entry
				VAT newVat = vatsDAO.findById(newVats.get(oldProduct.getVatid()));
				product.setVat(newVat);
				product.setWebshopId(new Long(oldProduct.getWebshopid()));
				product.setWeight(oldProduct.getWeight());
				
				productsDAO.save(product);
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
				log.error(e, "error while copying product picture for product [" + oldProduct.getId() + "] from old workspace. Reason:");
			}

		}
	}

	private void runMigrateDocumentsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllDocuments();
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(" " + countOfEntitiesInTable + " " + msg.startMigration);
		Map<Integer, Document> invoiceRelevantDocuments = new HashMap<>();
		Map<Integer, Document> invoiceDocuments = new HashMap<>();
		GregorianCalendar zeroDate = new GregorianCalendar(2000, 0, 1);
//		CategoryBuilder<DocumentItemCategory> catBuilder = new CategoryBuilder<DocumentItemCategory>(); 
//		Map<String, DocumentItemCategory> documentItemCategories = catBuilder.buildCategoryMap(oldDao.findAllDocumentItemCategories(), DocumentItemCategory.class);
		for (OldDocuments oldDocument : oldDao.findAllDocuments()) {
			try {
				Document document;
				switch (BillingType.get(oldDocument.getCategory())) {
                case INVOICE:
                    document = new Invoice();
                    break;
                case LETTER:
                    document = new Letter();
                    break;
                case ORDER:
                    document = new Order();
                    break;
                case OFFER:
                    document = new Offer();
                    break;
                case CONFIRMATION:
                    document = new Confirmation();
                    break;
                case CREDIT:
                    document = new Credit();
                    break;
                case DUNNING:
                    document = new Dunning();
                    break;
                case DELIVERY:
                    document = new Delivery();
                    break;
                case PROFORMA:
                    document = new Proforma();
                    break;
                default:
                    document = new Invoice();
                    break;
                }
//				// save it for further use
//				document = documentDAO.save(document);
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
//				document.setCreationDate(getSaveParsedDate(oldDocument.getDate()));
				document.setDeleted(oldDocument.isDeleted());
				// delivery address? got from contact? Assume that it's equal to contact address 
				// as long there's no delivery address stored 
				document.setDueDays(oldDocument.getDuedays());
				document.setDunningLevel(oldDocument.getDunninglevel());
				// store the pair for later processing
				// if the old Document has an InvoiceId and that ID is the same as the Document's ID
				// then we have to store it for further processing.
				// if the Invoiceid is the same as the document's id then it's the invoice itself
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
				
				document.setOrderDate(getSaveParsedDate(oldDocument.getOrderdate()));
				document.setServiceDate(getSaveParsedDate(oldDocument.getServicedate()));
				// if "paydate" is set and *NOT* 2000-01-01 then the document is paid - we don't need a "paid" flag
				Date payDate = getSaveParsedDate(oldDocument.getPaydate());
				if(payDate.compareTo(zeroDate.getTime()) != 0) {
					document.setPayDate(payDate);
				}
				
				// get payment reference
				Payment newPayment = paymentsDAO.findById(newPayments.get(oldDocument.getPaymentid()));
				document.setPayment(newPayment);
				document.setPayedValue(oldDocument.getPayvalue());
				document.setTotalValue(oldDocument.getTotal());
				document.setTransactionId(new Long(oldDocument.getTransaction()));
				document.setWebshopDate(getSaveParsedDate(oldDocument.getWebshopdate()));
				document.setWebshopId(oldDocument.getWebshopid());
				document.setProgress(oldDocument.getProgress());
				
				// find the Shipping entry
				Shipping newShipping = shippingsDAO.findById(newShippings.get(oldDocument.getShippingid()));
				document.setShipping(newShipping);

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
				Document document = (Document) invoiceRelevantDocuments.get(oldDocument.getId());
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
		for (String itemRef : itemRefs) {
			OldItems oldItem = oldDao.findDocumentItem(Integer.valueOf(itemRef));
			DocumentItem item = new DocumentItem();
//						if(StringUtils.isNotBlank(oldItem.getCategory()) && documentItemCategories.containsKey(oldItem.getCategory())) {
//							// add it to the new entity
//							item.addToCategories(documentItemCategories.get(oldItem.getCategory()));
//						}
			item.setDescription(oldItem.getDescription());
			item.setItemRebate(oldItem.getDiscount());
			item.setItemNumber(oldItem.getItemnr());
			item.setName(oldItem.getName());
			// find the VAT entry
			VAT vatRef = vatsDAO.findById(newVats.get(oldItem.getVatid()));
			if(vatRef == null) {
				log.error("no entry for " + oldItem.getVatname() + " found. (old) Item ID=" + oldItem.getId());
			} else {
				item.setItemVat(vatRef);
			}
			// since we now have a reference to a valid VAT we don't need the fields "novatdescription" and "novatname"
			item.setOptional(oldItem.isOptional());
			// owner field (oldItem) contains a reference to the containing document - we don't need it
			item.setPictureName(oldItem.getPicturename());
			item.setPrice(oldItem.getPrice());
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
//   			item.setOwningDocument(document);
			document.addToItems(item);
		}
	}

	private void runMigrateContactsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllContacts();
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		CategoryBuilder<ContactCategory> catBuilder = new CategoryBuilder<ContactCategory>(); 
		Map<String, ContactCategory> contactCategories = catBuilder.buildCategoryMap(oldDao.findAllContactCategories(), ContactCategory.class);
		for (OldContacts oldContact : oldDao.findAllContacts()) {
			try {
				Contact contact = createBaseContactFromOldContact(false, oldContact);
				contact.setAccount(oldContact.getAccount());
				contact.setAccountHolder(oldContact.getAccountHolder());
				
				if(StringUtils.isNotEmpty(oldContact.getBankCode()) && StringUtils.isNumericSpace(oldContact.getBankCode())) {
					contact.setBankCode(Integer.parseInt(oldContact.getBankCode().replaceAll(" ", "")));  // could run into trouble if bankCode contains spaces
				}
				contact.setBankName(oldContact.getBankName());
				contact.setBic(oldContact.getBic());
				contact.setBirthday(getSaveParsedDate(oldContact.getBirthday()));
				if(StringUtils.isNotBlank(oldContact.getCategory()) && contactCategories.containsKey(oldContact.getCategory())) {
					// add it to the new entity
//					contact.addToCategories(contactCategories.get(oldContact.getCategory()));
                    contact.setCategories(contactCategories.get(oldContact.getCategory()));
				}
				contact.setCustomerNumber(oldContact.getNr());  // TODO check if it's correct
				contact.setDateAdded(getSaveParsedDate(oldContact.getDateAdded()));
				Contact deliveryContact = createBaseContactFromOldContact(true, oldContact);
//				contact.getDeliveryContacts().add(deliveryContact);
                contact.setDeliveryContacts(deliveryContact);
				contact.setDeleted(oldContact.isDeleted());
				contact.setDiscount(oldContact.getDiscount());
				contact.setEmail(oldContact.getEmail());
				contact.setFax(oldContact.getFax());
				contact.setIban(oldContact.getIban());
				contact.setMobile(oldContact.getMobile());
				contact.setNote(oldContact.getNote());
				contact.setPayment(paymentsDAO.findById(newPayments.get(oldContact.getPayment())));
				contact.setPhone(oldContact.getPhone());
				contact.setReliability(ReliabilityType.get(oldContact.getReliability()));
				contact.setSupplierNumber(oldContact.getSuppliernumber());
				contact.setUseNetGross(Integer.valueOf(oldContact.getUseNetGross()).shortValue());
				contact.setVatNumber(oldContact.getVatnr());
				contact.setVatNumberValid(BooleanUtils.toBooleanObject(oldContact.getVatnrvalid()));
				contact.setWebsite(oldContact.getWebsite());
				contact.setMandateReference(oldContact.getMandatRef());
				contactDAO.save(contact);
				
				// store it for further using (only ID for memory saving)
				newContacts.put(oldContact.getId(), contact.getId());
				subProgressMonitor.worked(1);
			}
			catch (SQLException | RuntimeException e) {
				log.error("error while migrating Contact. (old) ID=" + oldContact.getId()+"; Reason: " + e.getMessage());
			}
		}
		subProgressMonitor.done();
	}
	
	private Contact createBaseContactFromOldContact(boolean isDeliveryAddress, OldContacts oldContact) {
		Contact contact = null;
		if(!StringUtils.isEmpty(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryName(), oldContact.getName())) || !StringUtils.isEmpty(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryFirstname(), oldContact.getFirstname()))) {
			contact = new Contact();
			contact.setCompany(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryCompany(), oldContact.getCompany()));
			contact.setFirstName(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryFirstname(), oldContact.getFirstname()));
			contact.setGender(isDeliveryAddress ? oldContact.getDeliveryGender() : oldContact.getGender());
			contact.setName(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryName(), oldContact.getName()));
			contact.setTitle(getDeliveryConsideredValue(isDeliveryAddress, oldContact.getDeliveryTitle(), oldContact.getTitle()));
			
			// create address
			Address address = new Address();
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
			try {
				retval = sdf.parse(dateValue);
			}
			catch (ParseException e) {
				log.error(String.format("error while parsing date value [%s]", dateValue));
			}
		}
		return retval;
	}

	private void runMigrateReceiptvouchersSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllReceiptvouchers();
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		Map<String, Account> receiptVoucherAccounts = buildAccountMap(VOUCHERTYPE_RECEIPT);
		Map<String, ItemAccountType> receiptVoucherItemAccountTypes = buildItemAccountTypeMap(VOUCHERTYPE_RECEIPT);
		for (OldReceiptvouchers oldReceiptvoucher : oldDao.findAllReceiptvouchers()) {
			try {
				ReceiptVoucher receiptVoucher = new ReceiptVoucher();
				if(StringUtils.isNotBlank(oldReceiptvoucher.getCategory()) && receiptVoucherAccounts.containsKey(oldReceiptvoucher.getCategory())) {
					receiptVoucher.setAccount(receiptVoucherAccounts.get(oldReceiptvoucher.getCategory()));
				}
				receiptVoucher.setDiscounted(oldReceiptvoucher.isDiscounted());
				receiptVoucher.setDeleted(oldReceiptvoucher.isDeleted());
				receiptVoucher.setDocumentNumber(oldReceiptvoucher.getDocumentnr());
				receiptVoucher.setDoNotBook(oldReceiptvoucher.isDonotbook());
				if(StringUtils.isNotEmpty(oldReceiptvoucher.getDate())) {
					Date receiptVoucherDate = sdf.parse(oldReceiptvoucher.getDate());
					receiptVoucher.setReceiptVoucherDate(receiptVoucherDate);
				}
				receiptVoucher.setReceiptVoucherNumber(oldReceiptvoucher.getNr());
				// each Expenditure has its own items
				if(StringUtils.isNotBlank(oldReceiptvoucher.getItems())) {
					String[] itemRefs = oldReceiptvoucher.getItems().split(",");
					for (String itemRef : itemRefs) {
						OldReceiptvoucheritems oldReceiptvoucherItem = oldDao.findReceiptvoucherItem(itemRef);
						ReceiptVoucherItem item = new ReceiptVoucherItem();
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
			catch (SQLException | ParseException e) {
				log.error("error while migrating Receiptvoucher. (old) ID=" + oldReceiptvoucher.getId() + "; Message: " + e.getMessage());
			}
		}
		subProgressMonitor.done();
	}

	private void runMigrateExpendituresSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllPayments();
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		Map<String, Account> expenditureAccounts = buildAccountMap(VOUCHERTYPE_EXPENDITURE);
		Map<String, ItemAccountType> expenditureItemAccountTypes = buildItemAccountTypeMap(VOUCHERTYPE_EXPENDITURE);
		for (OldExpenditures oldExpenditure : oldDao.findAllExpenditures()) {
			try {
				Expenditure expenditure = new Expenditure();
				if(StringUtils.isNotBlank(oldExpenditure.getCategory()) && expenditureAccounts.containsKey(oldExpenditure.getCategory())) {
					expenditure.setAccount(expenditureAccounts.get(oldExpenditure.getCategory()));
				}
				expenditure.setDeleted(oldExpenditure.isDeleted());
				expenditure.setDiscounted(oldExpenditure.isDiscounted());
				expenditure.setDocumentNumber(oldExpenditure.getDocumentnr());
				expenditure.setDoNotBook(oldExpenditure.isDonotbook());
				if(StringUtils.isNotEmpty(oldExpenditure.getDate())) {
					Date expenditureDate = sdf.parse(oldExpenditure.getDate());
					expenditure.setExpenditureDate(expenditureDate);
				}
				expenditure.setExpenditureNumber(oldExpenditure.getNr());
				// each Expenditure has its own items
				if(StringUtils.isNotBlank(oldExpenditure.getItems())) {
					String[] itemRefs = oldExpenditure.getItems().split(",");
					for (String itemRef : itemRefs) {
						OldExpenditureitems oldExpenditureItem = oldDao.findExpenditureItem(itemRef);
						ExpenditureItem item = new ExpenditureItem();
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
			catch (SQLException | ParseException e) {
				log.error("error while migrating Expenditure. (old) ID=" + oldExpenditure.getId());
			}
		}
		subProgressMonitor.done();
	}


	private Map<String, ItemAccountType> buildItemAccountTypeMap(int type) {
		Map<String, ItemAccountType> itemAccountTypes = new HashMap<String, ItemAccountType>();
		List<String> resultSet;
		if(type == VOUCHERTYPE_EXPENDITURE) {
			resultSet = oldDao.findAllExpenditureItemCategories();
		} else {
			resultSet = oldDao.findAllReceiptVoucherItemCategories();
		}
		for (String oldVoucherItemCategory : resultSet) {
			ItemAccountType itemAccountType = new ItemAccountType();
			itemAccountType.setName(oldVoucherItemCategory);
			itemAccountTypes.put(oldVoucherItemCategory, itemAccountType);
		}
		return itemAccountTypes;
	}


	private Map<String, Account> buildAccountMap(int type) {
		Map<String, Account> expenditureAccounts = new HashMap<String, Account>();
		List<String> resultSet;
		if(type == VOUCHERTYPE_EXPENDITURE) {
			resultSet = oldDao.findAllExpenditureCategories();
		} else {
			resultSet = oldDao.findAllReceiptvoucherCategories();
		}
		for (String oldVoucherCategory : resultSet) {
			Account account = new Account();
			account.setName(oldVoucherCategory);
			expenditureAccounts.put(oldVoucherCategory, account);
		}
		return expenditureAccounts;
	}


	private void runMigratePaymentsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllPayments();
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		CategoryBuilder<PaymentCategory> catBuilder = new CategoryBuilder<PaymentCategory>(); 
		Map<String, PaymentCategory> paymentCategories = catBuilder.buildCategoryMap(oldDao.findAllPaymentCategories(), PaymentCategory.class);
		for (OldPayments oldPayment : oldDao.findAllPayments()) {
			try {
				Payment payment = new Payment();
				payment.setName(oldPayment.getName());
				payment.setPaidText(oldPayment.getPaidtext());
				payment.setUnpaidText(oldPayment.getUnpaidtext());
				payment.setDefaultPaid(oldPayment.isDefaultpaid());
				payment.setDeleted(oldPayment.isDeleted());
				payment.setDiscountDays(oldPayment.getDiscountdays());
				payment.setDiscountValue(oldPayment.getDiscountvalue());
				payment.setDescription(oldPayment.getDescription());
				payment.setNetDays(oldPayment.getNetdays());
				if(StringUtils.isNotBlank(oldPayment.getCategory()) && paymentCategories.containsKey(oldPayment.getCategory())) {
					// add it to the new entity
					payment.setCategory(paymentCategories.get(oldPayment.getCategory()));
				}
				paymentsDAO.save(payment);
				newPayments.put(oldPayment.getId(), payment.getId());
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				log.error("error while migrating Payment. (old) ID=" + oldPayment.getId());
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
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		CategoryBuilder<TextCategory> catBuilder = new CategoryBuilder<TextCategory>(); 
		Map<String, TextCategory> textCategoriesMap = catBuilder.buildCategoryMap(oldDao.findAllTextCategories(), TextCategory.class);
		for (OldTexts oldTexts : oldDao.findAllTexts()) {
			try {
				TextModule text = new TextModule();
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
				log.error("error while migrating Text. (old) ID=" + oldTexts.getId());
			}
		}
		subProgressMonitor.done();
	}


	/**
	 * Migration of the Vats table (DataSetVAT)
	 * 
	 * @param subProgressMonitor the {@link SubProgressMonitor}
	 */
	private void runMigrateVatsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllVats();
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		CategoryBuilder<VATCategory> catBuilder = new CategoryBuilder<VATCategory>(); 
		Map<String, VATCategory> vatCategoriesMap = catBuilder.buildCategoryMap(oldDao.findAllVatCategories(), VATCategory.class);
		for (OldVats oldVat : oldDao.findAllVats()) {
			try {
				VAT vat = new VAT();
				vat.setName(oldVat.getName());
				vat.setTaxValue(oldVat.getValue());
				vat.setDeleted(oldVat.isDeleted());
				vat.setDescription(oldVat.getDescription());
				if(StringUtils.isNotBlank(oldVat.getCategory()) && vatCategoriesMap.containsKey(oldVat.getCategory())) {
					// add it to the new entity
					vat.setCategory(vatCategoriesMap.get(oldVat.getCategory()));
				}
				vatsDAO.save(vat);
				newVats.put(oldVat.getId(), vat.getId());
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				log.error(e, "error while migrating VAT. (old) ID=" + oldVat.getId()+"; Reason: " + e.getMessage());
			}
		}
		subProgressMonitor.done();
	}

	/**
	 * Migration of the Shipping table (DataSetShipping)
	 * 
	 * @param subProgressMonitor the {@link SubProgressMonitor}
	 */
	private void runMigrateShippingsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllShippings();
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));
		// use a HashMap as a simple cache
		CategoryBuilder<ShippingCategory> catBuilder = new CategoryBuilder<ShippingCategory>(); 
		Map<String, ShippingCategory> shippingCategoriesMap = catBuilder.buildCategoryMap(oldDao.findAllShippingCategories(), ShippingCategory.class);
		for (OldShippings oldShipping : oldDao.findAllShippings()) {
			try {
				Shipping shipping = new Shipping();
				shipping.setName(oldShipping.getName());
				shipping.setShippingValue(oldShipping.getValue());
				shipping.setDeleted(oldShipping.isDeleted());
				shipping.setAutoVat(ShippingVatType.get(oldShipping.getAutovat()));
				shipping.setDescription(oldShipping.getDescription());
				if(StringUtils.isNotBlank(oldShipping.getCategory()) && shippingCategoriesMap.containsKey(oldShipping.getCategory())) {
					// add it to the new entity
//					shipping.addToCategories(shippingCategoriesMap.get(oldShipping.getCategory()));
                    shipping.setCategories(shippingCategoriesMap.get(oldShipping.getCategory()));
				}
				
				VAT newVat = vatsDAO.findById(newVats.get(oldShipping.getVatid()));
				shipping.setShippingVat(newVat);
				shippingsDAO.save(shipping);
				newShippings.put(oldShipping.getId(), shipping.getId());
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				log.error("error while migrating Shipping. (old) ID=" + oldShipping.getId());
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
		subProgressMonitor.beginTask(msg.startMigrationWorking, countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(String.format(" %d %s", countOfEntitiesInTable, msg.startMigration));

	    createColumnWidthPreferences();
	    
        for (OldProperties oldProperty : oldDao.findAllPropertiesWithoutColumnWidthProperties()) {
			try {
				UserProperty prop = new UserProperty();
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
                	// if the currency is stored as symbol we have to convert it to an ISO code.
           			// Money doesn't work with symbols, therefore we have to convert this.
                    // Try to determine a Locale from symbol
                    Locale currencyLocale = Locale.getDefault();
           			if(StringUtils.length(propValue) == 1) {
                        Currency jdkCurrency = Currency.getInstance(currencyLocale);
           		        String currencySymbol = jdkCurrency.getSymbol(currencyLocale);
           		        if(currencySymbol.contentEquals(propValue)) {
           		            propValue = currencyLocale.getLanguage() + "/" + currencyLocale.getCountry();
           		            log.info("The currency locale was set to " + currencyLocale.toLanguageTag()+". "
           		                    + "Please check this in the general settings.");
           		        } else {
           		            // Since most of the Fakturama users are from Germany we choose "de/DE" as default locale.
           		            log.error("Can't determine the currency locale. Please choose the right locale "
           		                    + "in the general settings dialog. Locale is temporarily set to de/DE.");
           		            propValue = "de/DE";
           		        }
           			}
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
				log.error("error while migrating UserProperty. (old) ID=" + oldProperty.getId());
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
//                        tableId = DocumentsListTable.ID;
                        break;
                    case "ITEMS":
                        break;
                    case "LIST":
                        break;
                    case "PAYMENTS":
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
                        // e.g., COLUMNWIDTH_DIALOG_CONTACTS_CITY
                        String[] splittedString = matcher.group(2).split("_");
                        // TODO implement!
                        switch (splittedString[0]) {
                        case "CONTACTS":
                            tableId="DIALOG";
                            break;
                        case "PRODUCTS":
                            break;

                        case "TEXTS":
                            break;
                        default:
                            break;
                        }
                        break;
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
//    		String columnWidthPropertyString;
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

}
