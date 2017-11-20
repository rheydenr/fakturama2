/**
 * 
 */
package org.fakturama.imp.wizard.csv.orders;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.imp.ImportMessages;

import com.opencsv.CSVReader;
import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.calculate.NumberGenerator;
import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CreateOODocumentHandler;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Order;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.util.DocumentItemUtil;

/**
 *
 */
public class OrdersCsvImporter {
	
	private static final String CSV_PROP_COMPANY = "firma";

	private static final String CSV_PROP_LASTNAME = "nachname";

	private static final String CSV_PROP_FIRSTNAME = "vorname";

	private static final int MAX_COUNT_OF_ITEMS_PER_ROW = 7;

	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	@Translation
	protected ImportOrdersMessages importOrdersMessages;

	@Inject
	@Translation
	protected Messages msg;    

	@Inject
	private ILogger log;
	
	@Inject
	private DocumentsDAO documentsDAO;
	
	@Inject
	private DebitorsDAO debitorsDAO;
	
	@Inject
	private ProductsDAO productsDAO;
	
    @Inject
    private IEclipseContext context;

	@Inject
	private ECommandService cmdService;
	
	@Inject
	private EHandlerService handlerService;

	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;

	// Defines all columns that are used and imported
	private String[] requiredHeaders = { CSV_PROP_LASTNAME,CSV_PROP_FIRSTNAME,CSV_PROP_COMPANY,
			"anzahl1","artikelnr1","anzahl2","artikelnr2",
			"anzahl3","artikelnr3","anzahl4","artikelnr4",
			"anzahl5","artikelnr5","anzahl6","artikelnr6",
			"anzahl7","artikelnr7" };

	// The result string
	String result = "";

	// NewLine
	String NL = System.lineSeparator();

	/**
	 * Returns if a column is in the list of required columns
	 * 
	 * @param columnName
	 *            The name of the column to test
	 * @return <code>true</code>, if this column is in the list of required columns
	 */
	private boolean isRequiredColumn(String columnName) {
		// Test all columns
		return Arrays.stream(requiredHeaders).anyMatch(col -> columnName.equalsIgnoreCase(col));
	}


	/**
	 * The import procedure.
	 * 
	 * @param fileName
	 *            Name of the file to import
	 * @param test
	 *            if <code>true</code>, the datasets are not imported (currently not used)
	 * @param shipping
	 *            which {@link Shipping} should be used
	 * @param orderTemplate 
	 * @param deliveryTemplate 
	 */
	public List<Document> importCSV(final String fileName, final boolean test, final Shipping shipping, Optional<Path> deliveryTemplate, Optional<Path> orderTemplate) {
		DocumentItemUtil documentItemUtil = ContextInjectionFactory.make(DocumentItemUtil.class, context);
		ContactUtil contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);
		NumberGenerator numberGenerator = ContextInjectionFactory.make(NumberGenerator.class, context);
		List<Document> resultList = new ArrayList<>();
		
		// Result string
		//T: Importing + .. FILENAME
		result = String.format("%s %s", importMessages.wizardImportProgressinfo, fileName);

		// Count the imported orders
		Set<Long> documentIds = new HashSet<>();

		// Count the line of the import file
		int lineNr = 0;
		final java.util.Date today = Calendar.getInstance().getTime();
		String[] columns;
		boolean doRewind = false;

		// save current counters for debtor number and order number 
		// (if the process should be rolled back)
		int currentDebtorNumber = numberGenerator.getCurrentNumber("Debtor");
		int currentOrderNumber = numberGenerator.getCurrentNumber(DocumentType.ORDER.getTypeAsString());
		
		// Open the existing file
		try (BufferedReader in = Files.newBufferedReader(Paths.get(fileName), Charset.forName("UTF-8"));
			 CSVReader csvr = new CSVReader(in, ',');) {
			
			// Read next CSV line
			columns = csvr.readNext();
			
			if (columns.length < 5) {
				//T: Error message
				result += NL + importMessages.wizardImportErrorFirstline;
				return resultList;
			}
			
		// Read the existing file and store it in a buffer
		// with a fix size. Only the newest lines are kept.
			// Read line by line
			String[] cells;
			while ((cells = csvr.readNext()) != null) {
				lineNr++;
				Properties prop = new Properties();

				// Dispatch all the cells into a property
				for (int col = 0; col < cells.length; col++) {
					if (col < columns.length && isRequiredColumn(columns[col])) {
						prop.setProperty(columns[col].toLowerCase(), cells[col]);
					}
				}

				// Test if all columns are used
				if (prop.size() > 0 && prop.size() != requiredHeaders.length) {
					for (int i = 0; i < requiredHeaders.length; i++) {
						if (!prop.containsKey(requiredHeaders[i]))
							//T: Format: LINE: xx: NO DATA IN COLUMN yy FOUND.
							result += NL 
								+ MessageFormat.format(importMessages.wizardImportErrorNodatafound, Integer.toString(lineNr),
							    "\"" + requiredHeaders[i] + "\""); 
					}
				}
				else {
					// build a new order
					Order order = modelFactory.createOrder();
					
					// lookup debtor (extract from compound identifier, e.g. "10001Z / 001 / 000017"
					String[] debtorIdentifier = prop.getProperty(CSV_PROP_COMPANY).split("/");
					Debitor contact;
					if(debtorIdentifier.length > 1) {
						contact = debitorsDAO.findByDebitorNumber(StringUtils.trim(debtorIdentifier[2]));
						if(contact == null) {
							result += NL + "no debtor found or multiple results found with number " + debtorIdentifier[2] + " in line " + csvr.getLinesRead();
							continue; // don't create an order since it's useless without a debtor.
						}
					} else {
						// create a new debtor if no identifier is given
						contact = modelFactory.createDebitor();
						contact.setFirstName(prop.getProperty(CSV_PROP_FIRSTNAME));
						contact.setName(prop.getProperty(CSV_PROP_LASTNAME));
						contact.setCompany(prop.getProperty(CSV_PROP_COMPANY));
						contact.setContactType(ContactType.BILLING);
						String nextDebtorNumber = numberGenerator.getNextNr("Debtor");
						contact.setCustomerNumber(nextDebtorNumber);
					}
					
					// set some facts
					order.setBillingType(BillingType.ORDER);
					order.setShipping(shipping);
					order.setBillingContact(contact);
					order.setAddressFirstLine(contactUtil.getNameWithCompany(contact));
					order.setName(numberGenerator.getNextNr(DocumentType.ORDER.getTypeAsString()));
					order.setOrderDate(today);
					order.setDocumentDate(today);
					order.setServiceDate(today);
					order.setPaid(Boolean.FALSE);
					order.setPrinted(Boolean.FALSE);
					order.setShippingAutoVat(shipping.getAutoVat());
					order.setShippingValue(shipping.getShippingValue());					
					
					List<DocumentItem> itemsList = new ArrayList<>();
					for (int i = 1; i <= MAX_COUNT_OF_ITEMS_PER_ROW; i++) {
						
						DocumentItem item = modelFactory.createDocumentItem();
						if(prop.getProperty("artikelnr"+i) != null) {
							Product product = productsDAO.findByItemNumber(StringUtils.trim(prop.getProperty("artikelnr"+i)));
							if(product == null) {
								result += NL + "no product found with Number " + prop.getProperty("artikelnr"+i) + " in line " + csvr.getLinesRead();
								continue; // no item? doesn't work! 
							}
							item = documentItemUtil.from(product, DocumentType.ORDER);
							item.setPosNr(i);
						}
						String quantityProp = prop.getProperty("anzahl"+i);
						double quantity = Double.valueOf(0.0);
						if(StringUtils.isNumeric(quantityProp)) {
							quantity = Double.parseDouble(quantityProp);
						}
						item.setQuantity(quantity);
						itemsList.add(item);
					}
					
					order.setItems(itemsList);
					DocumentSummaryCalculator documentSummaryCalculator = new DocumentSummaryCalculator(order);
					DocumentSummary documentSummary = documentSummaryCalculator.calculate(order);
					order.setTotalValue(documentSummary.getTotalGross().getNumber().doubleValue());
					
					// cache the results
					resultList.add(order);
					numberGenerator.setNextFreeNumberInPrefStore(order.getName(), DocumentType.ORDER.getTypeAsString());
				}
			}
			if(!resultList.isEmpty()) {
				// Add the order to the data base and store the ID for later processing
				// (the order of these IDs is not relevant)
				documentIds = documentsDAO.saveBatch(resultList);
				Set<Long> deliveries = new HashSet<>();
				
				// now try to create delivery documents from orders
				context.set(CallEditor.PARAM_CATEGORY, BillingType.DELIVERY.name());
				context.set(CallEditor.PARAM_DUPLICATE, Boolean.toString(true));
				context.set(DocumentEditor.PARAM_SILENT_MODE, Boolean.TRUE);
				documentIds.forEach(id -> {
					context.set(CallEditor.PARAM_OBJ_ID, Long.toString(id));
	                DocumentEditor tmpEditor = ContextInjectionFactory.make(DocumentEditor.class, context);
	                deliveries.add(tmpEditor.getDocument() != null ? tmpEditor.getDocument().getId() : Long.valueOf(0L));
				});
				
				createDocuments(documentIds, orderTemplate);
				
				createDocuments(deliveries, deliveryTemplate);
			}
			
			// The result string
			//T: Message: xx Orders have been imported 
			result += NL + MessageFormat.format(importOrdersMessages.wizardImportInfoOrdersimported, documentIds.size());
		} catch (FakturamaStoringException e) {/*, address first line is: '" + doc.getAddressFirstLine() + "'*/
			log.error(e, "can't save imported order");
			doRewind = true;
		}
		catch (UnsupportedEncodingException e) {
			log.error(e, "Unsupported UTF-8 encoding");
			result += NL + "Unsupported UTF-8 encoding";
			doRewind = true;
		}
		catch (FileNotFoundException e) {
			result += NL + importMessages.wizardImportErrorFilenotfound;
			doRewind = true;
		}
		catch (IOException e) {
			result += NL + importMessages.wizardImportErrorOpenfile;
			doRewind = true;
		} finally {
			// rewind numbers
			if(doRewind) {
				numberGenerator.setNextFreeNumberInPrefStore(Integer.toString(currentOrderNumber), DocumentType.ORDER.getTypeAsString());
				numberGenerator.setNextFreeNumberInPrefStore(Integer.toString(currentDebtorNumber), "Debtor");
			}
		}
		return resultList;
	}

	private void createDocuments(Set<Long> documentIds, Optional<Path> orderTemplate) {
        Map<String, Object> params = new HashMap<>();
        Iterator<Long> docIdIterator = documentIds.iterator();
		while(docIdIterator.hasNext()) {
			Long nextDocId = docIdIterator.next();
			// create order documents
			params.put(CreateOODocumentHandler.PARAM_TEMPLATEPATH, orderTemplate.orElse(Paths.get("")).toString());
			params.put(CreateOODocumentHandler.PARAM_SILENTMODE, Boolean.toString(true));
			params.put(CreateOODocumentHandler.PARAM_DOCUMENT, nextDocId.toString());
			ParameterizedCommand pCmd = cmdService.createCommand("org.fakturama.print.oofile", params);
			if (handlerService.canExecute(pCmd)) {
				handlerService.executeHandler(pCmd);
			}
		}
	}


	public String getResult() {
		return result;
	}
}
