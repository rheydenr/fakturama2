/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2020 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Ralf Heydenreich - initial API and implementation
 */
package org.fakturama.export.facturx;

import java.io.Serializable;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.fakturama.export.einvoice.AbstractEInvoiceCreator;
import org.fakturama.export.einvoice.ConformanceLevel;
import org.fakturama.export.einvoice.IEinvoice;
import org.fakturama.export.einvoice.IPdfHelper;
import org.fakturama.export.zugferd.ZUGFeRD;
import org.fakturama.export.zugferd.ZugferdHelper;

import com.sebulli.fakturama.model.Invoice;

/**
 * Generator class for XRechnung /Factur-x files.
 * 
 * <p>
 * HINTS (from German documentation):<br/>
 * <ul>
 * <li>Bei Dezimalzahlen müssen die Nachkommastellen durch einen Dezimalpunkt
 * getrennt sein.
 * <li>Die Codelisten werden analog zu CEN/TS 16931-3-3 definiert
 * <li>Bis zum Profil EN 16931 (COMFORT) gelten die Design-Prinzipien der Norm,
 * dass sich eine Rechnung immer nur auf genau eine Bestellung und genau eine
 * Lieferung beziehen darf.
 * <li>Nettopreis als verbindliche Preisinformation
 * <li>Der Nettopreis des Artikels in diesem Zusammenhang ist der Preis eines
 * Artikels ohne Umsatzsteuer nach Abzug des Nachlasses auf den Artikelpreis.
 * Der Nettobetrag der Rechnungsposition ist der „Netto“-Betrag d. h. ohne die
 * Umsatzsteuer, aber einschließlich aller für die Positionsebene geltenden Zu-
 * und Abschläge sowie sonstiger anfallender Steuern.
 * <li>Die EN 16931-1 unterstützt nur Nachlass auf den Bruttopreis des Artikels.
 * </ul>
 * </p>
 *
 */
public class XRechnungCreator extends AbstractEInvoiceCreator {

    @Inject
    private IEclipseContext context;
    
    private IPdfHelper pdfHelper;

    @Override
    public boolean createEInvoice(Optional<Invoice> invoice, ConformanceLevel zugferdProfile) {
        Serializable invoiceXml;
        // 2. create XML file
        IEinvoice eInvoice;
        switch (zugferdProfile) {
        case FACTURX_BASIC:
            throw new UnsupportedOperationException("not yet implemented");
        case FACTURX_COMFORT:
        case ZUGFERD_V2_COMFORT:
        case ZUGFERD_V2_EN16931:
            eInvoice = ContextInjectionFactory.make(XRechnung.class, context);
            invoiceXml = eInvoice.getInvoiceXml(invoice);
            break;
        case XRECHNUNG:
            eInvoice = ContextInjectionFactory.make(XRechnung.class, context);
            invoiceXml = eInvoice.getInvoiceXml(invoice);
            break;
        case ZUGFERD_V1_COMFORT:
            pdfHelper = new ZugferdHelper();
            eInvoice = ContextInjectionFactory.make(ZUGFeRD.class, context);
            invoiceXml = eInvoice.getInvoiceXml(invoice);
            break;
        default:
            eInvoice = ContextInjectionFactory.make(XRechnung.class, context);
            invoiceXml = eInvoice.getInvoiceXml(invoice);
            break;
        }
        
        // 3. merge XML & PDF/A-1 to PDF/A-3
        return createPdf(invoice.get(), () -> invoiceXml, zugferdProfile);
        //      testOutput(root);
    }

    @Override
    protected IPdfHelper getPdfHelper() {
        if(pdfHelper == null) {
            pdfHelper = new FacturXHelper();
        }
        return pdfHelper;
    }

}
