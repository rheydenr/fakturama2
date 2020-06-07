package org.fakturama.export.zugferd;

import java.util.Optional;

import com.sebulli.fakturama.model.Document;

public interface IEinvoiceCreator {

    boolean createEInvoice(Optional<Document> invoice, ConformanceLevel zugferdProfile);

}
