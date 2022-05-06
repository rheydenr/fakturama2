package org.fakturama.qrcode;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.component.annotations.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.qrcode.QRCodeService;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.core.scheme.VCard;
import net.glxn.qrgen.javase.QRCode;

@Component(name = "qrcodeservice")
public class QRCodeServiceImpl implements QRCodeService {
    
    @Inject
    private IEclipseContext context;

    @Inject
    private ILogger log;

    @Inject
    private IPreferenceStore preferences;

    @Override
    public byte[] createSwissCodeQR(Invoice document) {
        QRSwissCodeGenerator qrSwissCodeGenerator = ContextInjectionFactory.make(QRSwissCodeGenerator.class, context);
        return qrSwissCodeGenerator.createSwissCodeQR(document);
    }

    @Override
    public byte[] createGiroCode(Invoice document) {
        GiroCodeGenerator giroCodeGenerator = ContextInjectionFactory.make(GiroCodeGenerator.class, context);
        return giroCodeGenerator.createGiroCode(document);
    }
    
    @Override
    public byte[] createVCardQRCode(Invoice document) {
        VCard vcard = new VCard(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_OWNER));
        vcard.setAddress(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_STREET));
        vcard.setCompany(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME));
        vcard.setEmail(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_EMAIL));
        vcard.setName(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME));
        vcard.setPhoneNumber(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TEL));
        vcard.setWebsite(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_WEBSITE));
        ByteArrayOutputStream qrCodeFile = QRCode.from(vcard).to(ImageType.PNG).stream();
        return qrCodeFile.toByteArray();
    }
    
    @Override
    public byte[] createEANCode(String productNumber) {
            EAN13Writer barcodeWriter = new EAN13Writer();
            BitMatrix bitMatrix = barcodeWriter.encode(productNumber, BarcodeFormat.EAN_13, 300, 50);

            byte[] imageBytes = null;
            try {
                BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                imageBytes = baos.toByteArray();    
                
            } catch (IOException e) {
                log.error(e);
            }
            return imageBytes;
    }
}
