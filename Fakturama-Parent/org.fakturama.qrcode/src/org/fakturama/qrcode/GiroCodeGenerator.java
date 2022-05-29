package org.fakturama.qrcode;

import java.io.ByteArrayOutputStream;
import java.security.InvalidParameterException;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.model.BankAccount;
import com.sebulli.fakturama.model.Invoice;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.core.scheme.Girocode;
import net.glxn.qrgen.javase.QRCode;

public class GiroCodeGenerator {

    @Inject
    private INumberFormatterService numberFormatterService;
    
    @Inject
    private IDateFormatterService dateFormatter;

    public byte[] createGiroCode(Invoice document, BankAccount companyBankaccount) {
        if (companyBankaccount == null || companyBankaccount.getBic() == null || companyBankaccount.getIban() == null) {
            throw new InvalidParameterException(StringUtils.join("No bank account given for your company", '\n'));
        }
        Girocode girocode = new Girocode();

        String amount = numberFormatterService.doubleToFormattedPrice(document.getTotalValue());
        girocode.setAmount(amount);

        girocode.setBic(companyBankaccount.getBic());
        girocode.setIban(companyBankaccount.getIban());
        girocode.setName(document.getAddressFirstLine());
        girocode.setReference(document.getName());
        girocode.setText("Rechnung vom " + dateFormatter.getFormattedLocalizedDate(document.getDocumentDate()));

        ByteArrayOutputStream qrCodeFile = QRCode.from(girocode).to(ImageType.PNG).stream();
        return qrCodeFile.toByteArray();
    }
}
