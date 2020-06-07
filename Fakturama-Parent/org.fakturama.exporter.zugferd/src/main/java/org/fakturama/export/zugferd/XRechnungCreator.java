package org.fakturama.export.zugferd;

import java.util.Optional;

import org.fakturama.export.facturx.modelgen.CrossIndustryInvoice;
import org.fakturama.export.facturx.modelgen.ObjectFactory;

import com.sebulli.fakturama.model.Document;

public class XRechnungCreator extends AbstractEInvoiceCreator {

    private ObjectFactory factory;

    @Override
    public boolean createEInvoice(Optional<Document> invoice, ConformanceLevel zugferdProfile) {
        factory = new ObjectFactory();
        
        // 2. create XML file
        CrossIndustryInvoice root = createInvoiceFromDataset(invoice.get(), zugferdProfile);

//      testOutput(root);
        
        // 3. merge XML & PDF/A-1 to PDF/A-3
        return createPdf(invoice.get(), () -> root, zugferdProfile);
    }

    private CrossIndustryInvoice createInvoiceFromDataset(Document document, ConformanceLevel zugferdProfile) {
        // TODO Auto-generated method stub
        return null;
    }

}
