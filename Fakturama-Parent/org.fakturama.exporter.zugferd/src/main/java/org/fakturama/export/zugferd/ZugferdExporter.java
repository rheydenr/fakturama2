/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Ralf Heydenreich - initial API and implementation
 */
package org.fakturama.export.zugferd;

import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Component;

import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.office.IPdfPostProcessor;

/**
 * This is the main class for the exporter interface for the ZUGFeRD invoice. At the
 * moment, a COMFORT level ZUGFeRD document is generated. This will be changed
 * in future (more flexible). 
 * 
 * This code has many TODOs because the ZUGFeRD specification wasn't final. Furthermore,
 * the invoice document doesn't have all the needed fields (esp. for EXTENDED profile). 
 * 
 * NOTE: The injection of all the services has to be done by caller (because this is an OSGi service,
 * but the injected services are from Eclipse context).
 * 
 */
@Component()
public class ZugferdExporter implements IPdfPostProcessor {
    
    @Inject @org.eclipse.e4.core.di.annotations.Optional
    @Preference
    private IEclipsePreferences eclipsePrefs;

    @Inject @org.eclipse.e4.core.di.annotations.Optional
    public Shell shell;

    @Inject
    @Translation
    protected ZFMessages msg;

    @Inject
    private IEclipseContext eclipseContext;

	/**
	 * This is for distinguishing the different contact entries.
	 *
	 */
	enum ContactType { SELLER, BUYER }
	enum FinancialRole { DEBTOR, CREDITOR }
//	enum InvoiceeTradeParty { DERIVED, }
	enum PriceType {
	    GROSS_PRICE,
	    NET_PRICE,
	    NET_PRICE_DISCOUNTED
    }

	@Override
	public boolean canProcess() {
		/*
		* Zunächst muß geprüft werden, ob OO/LO auch PDF/A erzeugt. Dazu muß man in der Datei 
		d:\Programme\LibreOffice 5\share\registry\main.xcd
		den Schlüssel
		
        <oor:data>
			<oor:component-schema oor:package="org.openoffice.Office" oor:name="Common" xml:lang="en-US"><component>		
			   <group oor:name="Filter"><group oor:name="PDF"><group oor:name="Export">
				  <prop oor:name="SelectPdfVersion" oor:type="xs:int" oor:nillable="false"><value>0</value></prop>
				  
		prüfen. Der Wert muß auf "1" stehen. Siehe dazu https://wiki.openoffice.org/wiki/API/Tutorials/PDF_export
		Idee: Vor dem Speichern den Wert umsetzen und am Schluß wieder zurücksetzen.
		*/
	    return eclipsePrefs.getBoolean(ZFConstants.PREFERENCES_ZUGFERD_ACTIVE, Boolean.FALSE);
	}

	@Override
    public boolean processPdf(Optional<Document> invoice) {
	    
        boolean result = false;
        if(invoice.isPresent()) {
	    
			String conformanceLevel = eclipsePrefs.get(ZFConstants.PREFERENCES_ZUGFERD_PROFILE, "COMFORT");
			ConformanceLevel zugferdProfile = ConformanceLevel.valueOf(conformanceLevel);
    	    // create e-invoice according to selected preferences
			IEinvoiceCreator invoiceCreator;
    	    if(eclipsePrefs.getInt(ZFConstants.PREFERENCES_ZUGFERD_VERSION, 1) == 1) {
    	        invoiceCreator = ContextInjectionFactory.make(ZUGFeRDCreator.class, eclipseContext);
    	    } else { // 2.1
    	        invoiceCreator = ContextInjectionFactory.make(XRechnungCreator.class, eclipseContext);
    	    }
			
            result = invoiceCreator.createEInvoice(invoice, zugferdProfile);
            
			if(!result) {
				// Display an error message
				MessageDialog.openError(shell, msg.zugferdExportCommandTitle, msg.zugferdExportErrorCancelled);
			}
		} else {
			// Display a warning message
			MessageDialog.openWarning(shell, msg.zugferdExportCommandTitle, msg.zugferdExportWarningChooseinvoice);
		}
        return result;
	}

}
