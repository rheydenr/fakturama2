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
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.fakturama.imp.ImportMessages;

import com.opencsv.CSVReader;
import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Order;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.util.DocumentItemUtil;

/**
 *
 */
public class OrdersCsvImporter {
	
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

	/**
	 * the model factory
	 */
	private FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;

	// Defines all columns that are used and imported
	private String[] requiredHeaders = { "nachname","vorname","firma",
			"anzahl1","artikel1","anzahl2","artikel2",
			"anzahl3","artikel3","anzahl4","artikel4",
			"anzahl5","artikel5","anzahl6","artikel6",
			"anzahl7","artikel7" };

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
	 */
	public void importCSV(final String fileName, final boolean test, final Shipping shipping) {
		DocumentItemUtil documentItemUtil = ContextInjectionFactory.make(DocumentItemUtil.class, context);
		List<Order> resultList = new ArrayList<>();
		
		// Result string
		//T: Importing + .. FILENAME
		result = String.format("%s %s", importMessages.wizardImportProgressinfo, fileName);

		// Count the imported orders
		int importedOrders = 0;

		// Count the line of the import file
		int lineNr = 0;

		String[] columns;
		
		// Open the existing file
		try (BufferedReader in = Files.newBufferedReader(Paths.get(fileName), Charset.forName("UTF-8"));
			 CSVReader csvr = new CSVReader(in, ';');) {
			
			// Read next CSV line
			columns = csvr.readNext();
			
			if (columns.length < 5) {
				//T: Error message
				result += NL + importMessages.wizardImportErrorFirstline;
				return;
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
					String[] debtorIdentifier = prop.getProperty("firma").split("/");
					Debitor contact;
					if(debtorIdentifier.length > 1) {
						contact = debitorsDAO.findByName(debtorIdentifier[2]);
						if(contact == null) {
							result += NL + "no debtor found with ID " + debtorIdentifier[2] + " in line " + csvr.getLinesRead();
						}
						continue; // don't create an order since it's useless without a debtor.
						
					} else {
						// create a new debtor if no identifier is given
						contact = modelFactory.createDebitor();
						contact.setFirstName(prop.getProperty("vorname"));
						contact.setName(prop.getProperty("nachname"));
						contact.setCompany(prop.getProperty("firma"));
						contact.setContactType(ContactType.BILLING);
						contact.setCustomerNumber("0815");  // TODO use the Editor method "setNextNr"!!!
					}
					
					
					// set some facts
					order.setShipping(shipping);
					order.setBillingContact(contact);
					List<DocumentItem> itemsList = new ArrayList<>();
					for (int i = 1; i <= MAX_COUNT_OF_ITEMS_PER_ROW; i++) {
						
						DocumentItem item = modelFactory.createDocumentItem();
						if(prop.getProperty("Artikel"+i) != null) {
							Product product = productsDAO.findByName(prop.getProperty("Artikel"+i));
							item = documentItemUtil.from(product, DocumentType.ORDER);
						}
						String quantityProp = prop.getProperty("Anzahl"+i);
						double quantity = Double.valueOf(0.0);
						if(StringUtils.isNumeric(quantityProp)) {
							quantity = Double.parseDouble(quantityProp);
						}
						item.setQuantity(quantity);
						itemsList.add(item);
					}
					
					order.setItems(itemsList);
					
					// cache the results
					resultList.add(order);
				}
			}
			if(!resultList.isEmpty()) {
				// Add the order to the data base
				resultList.forEach(doc -> {
					try {
						documentsDAO.save(doc);
					} catch (FakturamaStoringException e) {
						log.error(e, "can't save or update imported order");
					}
				});
			}
			// The result string
			//T: Message: xx Orders have been imported 
			result += NL + MessageFormat.format(importOrdersMessages.wizardImportInfoOrdersimported, importedOrders);
		}
		
		catch (UnsupportedEncodingException e) {
			log.error(e, "Unsupported UTF-8 encoding");
			result += NL + "Unsupported UTF-8 encoding";
			return;
		}
		catch (FileNotFoundException e) {
			//T: Error message
			result += NL + importMessages.wizardImportErrorFilenotfound;
			return;
		}
		catch (IOException e) {
			//T: Error message
			result += NL + importMessages.wizardImportErrorOpenfile;
		}
	}

	public String getResult() {
		return result;
	}
}
