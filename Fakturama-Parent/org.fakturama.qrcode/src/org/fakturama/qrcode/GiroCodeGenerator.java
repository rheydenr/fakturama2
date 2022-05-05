package org.fakturama.qrcode;

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.model.Invoice;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.core.scheme.Girocode;
import net.glxn.qrgen.javase.QRCode;

public class GiroCodeGenerator {

    @Inject
    private INumberFormatterService numberFormatterService;
    
    @Inject
    private IDateFormatterService dateFormatter;

    @Inject
    private ContactsDAO contactsDAO;

    @Inject
    private IDocumentAddressManager addressManager;

    public byte[] createGiroCode(Invoice document) {
        DocumentReceiver documentReceiver = addressManager.getBillingAdress(document);
        Contact debitor = contactsDAO.findById(documentReceiver.getOriginContactId());
        if (debitor != null && debitor.getBankAccount() != null) {
            Girocode girocode = new Girocode();

            String amount = numberFormatterService.doubleToFormattedPrice(document.getTotalValue());
            girocode.setAmount(amount);

            girocode.setBic(debitor.getBankAccount().getBic());
            girocode.setIban(debitor.getBankAccount().getIban());
            girocode.setName(document.getAddressFirstLine());
            girocode.setReference(document.getName());
            girocode.setText("Rechnung vom " + dateFormatter.getFormattedLocalizedDate(document.getDocumentDate()));

            ByteArrayOutputStream qrCodeFile = QRCode.from(girocode).to(ImageType.PNG).stream();
            return qrCodeFile.toByteArray();
        } else return null;
    }
}
