package org.fakturama.imp.wizard.csv.contacts;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
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
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BankAccount;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.util.ContactUtil;

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
    private PaymentsDAO paymentsDAO;
    
    @Inject
    private IDateFormatterService dateFormatterService;

    @Inject
    private IDocumentAddressManager addressManager;
    
    @Inject
    private IEclipseContext ctx;

    private ContactUtil contactUtil;
    
    @Inject
    private ILocaleService localeUtil;

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

        if (fileName == null) {
            log.error("No filename for import file given.");
            return;
        }

        // Result string
        // T: Importing + .. FILENAME
        result = String.format("%s %s", importMessages.wizardImportProgressinfo, fileName);

        // Count the imported contacts
        int importedContacts = 0;
        int updatedContacts = 0;
        int skippedContacts = 0;

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
            // with a fixed size. Only the latest lines are kept.

            // Read line by line
            while (productCsvIterator.hasNext()) {
//                lineNr++;

                ContactBeanCSV contactBean = productCsvIterator.next();
                
                Address address = modelFactory.createAddress();
                address.setZip(contactBean.getZip());
                address.getContactTypes().add(ContactType.BILLING);

                Contact contact = modelFactory.createDebitor();
                contact.setCustomerNumber(StringUtils.trim(contactBean.getCustomerNumber()));
                contact.setName(StringUtils.trim(contactBean.getName()));
//   ???           contact.getAddresses().add(address);
     
                /*
                 * Customer number, first name, name and ZIP are compared
                 */
                Contact testContact = contactsDao.findOrCreate(contact, true);
                
                // if found and no update is required skip to the next record
                if(testContact != null && !updateExisting) {
                    skippedContacts++;
                    continue;
                }
                
                if(testContact == null) {
                    // work further with testcontact
                    testContact = contact;
                }
                
                if (contactBean.getCategory() != null) {
                    ContactCategory category = contactCategoriesDao.getCategory(StringUtils.trim(contactBean.getCategory()), false);
                    if (category != null || importEmptyValues) {
                        testContact.setCategories(category);
                    }
                }
                
                // update/copy values from CSV bean
                testContact = copyValues(testContact, address, contactBean);
                
                // Add the contact to the data base
                // Update the modified contact data
                long currentId = testContact.getId();
                contactsDao.update(testContact);
                if (currentId == 0) {
                    importedContacts++;
                } else if (updateExisting) {
                    // Update data
                    updatedContacts++;
                }
            }            

            // The result string
            //T: Message: xx Contacts have been imported 
            result += NL + importedContacts + " " + importMessages.wizardImportInfoContactsimported;
            if (updatedContacts > 0)
                result += NL + updatedContacts + " " + importMessages.wizardImportInfoContactsupdated;
            if (skippedContacts > 0)
                result += NL + skippedContacts + " " + importMessages.wizardImportInfoContactsskipped;
            
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
    private Contact copyValues(Contact contact, Address address, ContactBeanCSV contactBean) {
        Date today = Calendar.getInstance().getTime();

        // check if contact is an existing entry, set date_modified accordingly
        if (contactBean.getDateAdded() != null) {
            contact.setModified(today);
        } else {
            contact.setDateAdded(today);
        }
        
        // customerNumber and name were already set in caller method to check if contact exists
        contact.setAlias(contactBean.getAlias());
        contact.setCompany(contactBean.getCompany());
        contact.setTitle(contactBean.getTitle());
        contact.setFirstName(contactBean.getFirstName());
        contact.setGender(getContactUtil().getGenderIdFromString(contactBean.getGender()));
        if(contactBean.getReliability() != null) {
            contact.setReliability(getContactUtil().getReliabilityID(contactBean.getReliability().toUpperCase()));
        }
        
        // if previous address is given use it
        Address tmpAddress = addressManager.getAddressFromContact(contact, ContactType.BILLING).orElse(null);
        if(tmpAddress != null) {
            address = createOrUpdateAddressFromContactBean(contactBean, tmpAddress, ContactType.BILLING);
        } else { // if it's a completely new address
            address = createOrUpdateAddressFromContactBean(contactBean, address, ContactType.BILLING);
            contact.getAddresses().add(address);
            address.setContact(contact);
        }
        
        if(contactBean.hasDeliveryAddress()) {
            Address deliveryAddress = addressManager.getAddressFromContact(contact, ContactType.DELIVERY).orElse(null);
            if(deliveryAddress.getId() == address.getId()) {
                // recreation of delivery address, if any
                deliveryAddress = modelFactory.createAddress();
            }
            deliveryAddress = createOrUpdateAddressFromContactBean(contactBean, deliveryAddress, ContactType.DELIVERY);
            deliveryAddress.getContactTypes().add(ContactType.DELIVERY);
            
            // only if it's a new address
            if(deliveryAddress.getId() == 0) {
                contact.getAddresses().add(deliveryAddress);
                deliveryAddress.setContact(contact);
            }
        }
        
        if(StringUtils.isNotBlank(contactBean.getIban())) {
            BankAccount account = contact.getBankAccount() != null ? contact.getBankAccount() : modelFactory.createBankAccount();
            account.setValidFrom(Calendar.getInstance().getTime());
            account.setAccountHolder(contactBean.getAccount_holder());
            account.setName(contactBean.getName());
            account.setBankName(contactBean.getBankName());
            account.setIban(contactBean.getIban());
            account.setBic(contactBean.getBic());
            contact.setBankAccount(account);
        }
        
        contact.setNote(contactBean.getNote());
        contact.setDiscount(contactBean.getDiscount());
        
        Payment payment = paymentsDAO.findByName(contactBean.getPaymentType());
        if(payment != null) {
            contact.setPayment(payment);
        }

        contact.setGln(contactBean.getGln());
        contact.setWebsite(contactBean.getWebsite());
        contact.setSupplierNumber(contactBean.getSupplierNumber());
        contact.setWebshopName(contactBean.getWebshopName());
        contact.setVatNumber(contactBean.getVatNumber());
        contact.setVatNumberValid(contactBean.getVatNumberValid());
        contact.setMandateReference(contactBean.getMandateReference());
        contact.setRegisterNumber(contactBean.getRegisterNumber());
        
        String birthday = contactBean.getBirthday();
        if(StringUtils.isNotBlank(birthday)) {
            GregorianCalendar dateFromString = dateFormatterService.getCalendarFromDateString(birthday);
            contact.setBirthday(dateFromString.getTime());
        }

        return contact;
    }

    /**
     * Creates an {@link Address} from CSV properties.
     * 
     * @param prop
     * @param address
     * @param prefix necessary e.g. for delivery addresses
     * @return 
     */
    private Address createOrUpdateAddressFromContactBean(ContactBeanCSV contactBean, Address address, ContactType type) {
        address.setValidFrom(Calendar.getInstance().getTime());
        address.setStreet(contactBean.getStreet(type));
        address.setCityAddon(contactBean.getCityAddon(type));
        address.setZip(contactBean.getZip(type));
        address.setCity(contactBean.getCity(type));
        if (contactBean.getCountryCode(type) != null) {
            String countryCode = localeUtil.findCodeByDisplayCountry(contactBean.getCountryCode(type),
                    localeUtil.getDefaultLocale().getLanguage());
            address.setCountryCode(countryCode);
        }
        address.setLocalConsultant(contactBean.getLocalConsultant(type));
        address.setEmail(contactBean.getEmail(type));
        address.setMobile(contactBean.getMobile(type));
        address.setPhone(contactBean.getPhone(type));
        address.setFax(contactBean.getFax(type));
        address.setAdditionalPhone(contactBean.getAdditionalPhone(type));
        return address;
    }

    public String getResult() {
        return result;
    }

    private ContactUtil getContactUtil() {
        if(contactUtil == null) {
            contactUtil = ContextInjectionFactory.make(ContactUtil.class, ctx);
        }
        return contactUtil;
    }

}
