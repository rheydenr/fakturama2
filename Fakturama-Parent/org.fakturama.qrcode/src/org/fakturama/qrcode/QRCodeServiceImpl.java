package org.fakturama.qrcode;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.service.component.annotations.Component;

import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.qrcode.QRCodeService;

@Component(name = "qrcodeservice")
public class QRCodeServiceImpl implements QRCodeService {
    
    @Inject
    private IEclipseContext context;

    @Override
    public byte[] createSwissCodeQR(Document document) {
        QRSwissCodeGenerator qrSwissCodeGenerator = ContextInjectionFactory.make(QRSwissCodeGenerator.class, context);
        return qrSwissCodeGenerator.createSwissCodeQR(document);
    }

}
