package org.fakturama.export.einvoice;

import java.util.Optional;

import com.sebulli.fakturama.model.Document;

public interface IEinvoiceCreator {

    boolean createEInvoice(Optional<Document> invoice, ConformanceLevel zugferdProfile);

}
