package org.fakturama.imp.wizard.csv.contacts;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportOptions;
import org.fakturama.imp.wizard.csv.common.CustomHeaderColumnNameTranslateMappingStrategy;
import org.fakturama.imp.wizard.csv.products.ImportMapping;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.bean.CsvToBean;
import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;

public class GenericContactsCsvImporter {

    @Inject
    @Translation
    protected ImportMessages importMessages;

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private ContactsDAO contactsDao;

    @Inject
    private ContactCategoriesDAO contactCategoriesDao;

    @Inject
    protected ILogger log;

    @Inject
    protected IPreferenceStore defaultValuePrefs;

    /**
     * the model factory
     */
    private FakturamaModelFactory modelFactory;

    // The result string
    private String result = " ";

    // NewLine
    private String NL = System.lineSeparator();

    /**
     * The import procedure
     * 
     * @param fileName
     *            Name of the file to import
     * @param test
     *            if true, the dataset are not imported (currently not used)
     * @param importOptions
     *            Options for Import
     * 
     */
    public void importCSV(ImportOptions importOptions, boolean test) {

        // if true, also existing entries will be updated
        boolean updateExisting = importOptions.getUpdateExisting();

        // if true, also empty values will be updated
        boolean importEmptyValues = importOptions.getUpdateWithEmptyValues();
        String fileName = importOptions.getCsvFile();
        char separator = StringUtils.defaultIfBlank(importOptions.getSeparator(), ";").charAt(0);
        char quoteChar = StringUtils.isNotBlank(importOptions.getQuoteChar()) ? importOptions.getQuoteChar().charAt(0) : '"';
        modelFactory = FakturamaModelPackage.MODELFACTORY;
        Date today = Calendar.getInstance().getTime();

        if (fileName == null) {
            log.error("No filename for import file given.");
            return;
        }

        // Result string
        // T: Importing + .. FILENAME
        result = String.format("%s %s", importMessages.wizardImportProgressinfo, fileName);

        // Count the imported contacts
        int importedProducts = 0;
        int updatedProducts = 0;

        // Count the line of the import file
//        int lineNr = 0;

        Path inputFile = Paths.get(fileName);

        // Open the existing file
        try (BufferedReader in = Files.newBufferedReader(inputFile)) {
            ICSVParser csvParser = new CSVParserBuilder().withSeparator(separator).withQuoteChar(quoteChar).build();
            CSVReader csvr = new CSVReaderBuilder(in).withCSVParser(csvParser).build();

            CustomHeaderColumnNameTranslateMappingStrategy<ContactBeanCSV> strat = new CustomHeaderColumnNameTranslateMappingStrategy<>();
            strat.setType(ContactBeanCSV.class);
            Map<String, String> columnMapping = new HashMap<String, String>(); 
            
            for (ImportMapping pm : importOptions.getMappings()) {
                // null values aren't allowed in streams, therefore we use the classical way
                columnMapping.put(pm.getLeftItem(), pm.getRightItem() != null ? pm.getRightItem().getKey() : null);
            }
            strat.setColumnMapping(columnMapping); // the fields to bind to in your bean

            CsvToBean<ContactBeanCSV> csv = new CsvToBean<>();
            csv.setCsvReader(csvr);
            csv.setMappingStrategy(strat);

            Iterator<ContactBeanCSV> productCsvIterator = csv.iterator();            
            
            // Read the existing file and store it in a buffer
            // with a fixed size. Only the newest lines are kept.

            // Read line by line
            while (productCsvIterator.hasNext()) {
//                lineNr++;

                //  contact contact = modelFactory.createProduct();
                ContactBeanCSV contactBean = productCsvIterator.next();
                 
                Contact contact = modelFactory.createDebitor();
                contact.setCustomerNumber(StringUtils.trim(contactBean.getCustomerNumber()));
                contact.setName(StringUtils.trim(contactBean.getName()));
                
                if (updateExisting) {
                    contact = contactsDao.findOrCreate(contact);
                }
                
                // update/copy values from CSV bean
                contact = copyValues(contact, contactBean);
                
                if (contactBean.getCategory() != null) {
                    ContactCategory category = contactCategoriesDao.getCategory(StringUtils.trim(contactBean.getCategory()), false);
                    if (category != null || importEmptyValues) {
                        contact.setCategories(category);
                    }
                }
                
                // Add the contact to the data base
                if (DateUtils.isSameDay(contact.getDateAdded(), today)) {
                    importedProducts++;
                } else if (updateExisting || DateUtils.isSameDay(contact.getModified(), today)) {
                    // Update data
                    updatedProducts++;
                }
                // Update the modified contact data
                contactsDao.update(contact);
            }            

            // The result string
            // T: Message: xx contacts HAVE BEEN IMPORTED
            result += NL + Integer.toString(importedProducts) + " " + importMessages.wizardImportInfoContactsimported;
            if (updatedProducts > 0)
                result += NL + Integer.toString(updatedProducts) + " " + importMessages.wizardImportInfoContactsupdated;
            
        } catch (IOException e) {
            // T: Error message
            result += NL + importMessages.wizardImportErrorOpenfile;
        } catch (FakturamaStoringException e) {
            log.error(e, "cant't store import data.");
        }

    }

    /**
     * Update values from CSV bean into contact bean.
     * 
     * @param contact target
     * @param contactBean source
     */
    private Contact copyValues(Contact contact, ContactBeanCSV contactBean) {
        Date today = Calendar.getInstance().getTime();

        // check if contact is an existing entry, set date_modified accordingly
        if (contact.getDateAdded() != null) {
            contact.setModified(today);
        } else {
            contact.setDateAdded(today);
        }
        
        // itemNumber and name were already set in caller method to check if contact exists
        
        contact.setSupplierNumber(contactBean.getSupplierNumber());
        contact.setNote(contactBean.getNote());
        
        return contact;
    }

    public String getResult() {
        return result;
    }

}
