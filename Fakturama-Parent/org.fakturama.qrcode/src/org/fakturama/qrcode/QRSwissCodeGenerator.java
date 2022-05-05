package org.fakturama.qrcode;

import javax.inject.Inject;

import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.IDocumentAddressManager;

import net.codecrete.qrbill.generator.Bill;
import net.codecrete.qrbill.generator.BillFormat;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.Language;
import net.codecrete.qrbill.generator.OutputSize;
import net.codecrete.qrbill.generator.Payments;
import net.codecrete.qrbill.generator.QRBill;

public class QRSwissCodeGenerator {

    @Inject
    private ILocaleService localeUtil;

    @Inject
    private IPreferenceStore preferences;
    
    @Inject
    private IDocumentAddressManager addressManager;
    
    @Inject
    private ContactsDAO contactsDAO;

    public byte[] createSwissCodeQR(Document document) {
        DocumentReceiver documentReceiver = addressManager.getBillingAdress(document);
        Contact debitor = contactsDAO.findById(documentReceiver.getOriginContactId());
        if (debitor != null && debitor.getBankAccount() != null) {
            // Setup bill
            Bill bill = new Bill();
            bill.setAccount(debitor.getBankAccount().getIban());
            bill.setAmountFromDouble(document.getTotalValue());
            bill.setCurrency(DataUtils.getInstance().getDefaultCurrencyUnit().getCurrencyCode());

            // Set creditor
            net.codecrete.qrbill.generator.Address creditor = new net.codecrete.qrbill.generator.Address();
            creditor.setName(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_OWNER));
            creditor.setAddressLine1(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_STREET));
            
            String zipCity = String.join(" ", preferences.getString(Constants.PREFERENCES_YOURCOMPANY_ZIP),
                    preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CITY));
            creditor.setAddressLine2(zipCity);
            
            String countryCode = localeUtil.findCodeByDisplayCountry(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_COUNTRY),
                    localeUtil.getDefaultLocale().getLanguage());
            creditor.setCountryCode(countryCode);
            
            bill.setCreditor(creditor);

            // only add a reference if the IBAN is a valid QR-IBAN
            if (Payments.isQRIBAN(debitor.getBankAccount().getIban())) {
                bill.setReference(document.getCustomerRef());
            }
            bill.setUnstructuredMessage(document.getMessage());

            // Set debtor
            net.codecrete.qrbill.generator.Address debtor = new net.codecrete.qrbill.generator.Address();
            debtor.setName(document.getAddressFirstLine());
            debtor.setAddressLine1(documentReceiver.getStreet());
            debtor.setAddressLine2(documentReceiver.getCity());
            debtor.setCountryCode(documentReceiver.getCountryCode());
            bill.setDebtor(debtor);

            // Set output format
            BillFormat format = bill.getFormat();
            format.setGraphicsFormat(GraphicsFormat.PNG);
            format.setOutputSize(OutputSize.QR_BILL_ONLY);
            
            net.codecrete.qrbill.generator.Language lang;
            switch (localeUtil.getDefaultLocale().getLanguage()) {
            case "de":
                lang = Language.DE;
                break;
            case "en":
                lang = Language.EN;
                break;
            case "fr":
                lang = Language.FR;
                break;
            case "ro":
                lang = Language.RM;
                break;
            default:
                lang = Language.DE;
                break;
            }
            
            format.setLanguage(lang);

            // Generate QR bill
            return QRBill.generate(bill);
        } else 
            return new byte[] {};
    }

}
