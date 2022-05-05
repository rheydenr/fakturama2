package org.fakturama.qrcode;

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.service.component.annotations.Component;

import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.qrcode.QRCodeService;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.core.scheme.VCard;
import net.glxn.qrgen.javase.QRCode;

@Component(name = "qrcodeservice")
public class QRCodeServiceImpl implements QRCodeService {
    
    @Inject
    private IEclipseContext context;

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
        VCard vcard = new VCard("your name");
        vcard.setAddress(document.getAddressFirstLine());
        vcard.setCompany("Fa. Dumpermann & Co.");
        vcard.setEmail("email@erde.de");
        vcard.setName("Max Mustermann");
        vcard.setPhoneNumber("0180 6565061651");
        vcard.setTitle("Mr.");
        vcard.setWebsite("www.fakturama.info");
        ByteArrayOutputStream qrCodeFile = QRCode.from(vcard).to(ImageType.PNG).stream();
        return qrCodeFile.toByteArray();
    }
}
