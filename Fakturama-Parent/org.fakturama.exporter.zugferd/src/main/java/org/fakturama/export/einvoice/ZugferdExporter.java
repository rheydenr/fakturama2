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
package org.fakturama.export.einvoice;

import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.fakturama.export.facturx.XRechnungCreator;
import org.osgi.service.component.annotations.Component;

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.EmbeddedProperties;
import com.sebulli.fakturama.misc.Util;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.office.IPdfPostProcessor;
import com.sebulli.fakturama.office.TargetFormat;
import com.sebulli.fakturama.parts.DocumentReceiverEditorDialog;
import com.sebulli.fakturama.parts.EInvoiceNoticeDialog;

/**
 * This is the main class for the exporter interface for the ZUGFeRD invoice. At the
 * moment, a COMFORT level ZUGFeRD document is generated. This will be changed
 * in future (more flexible). 
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
    
    @Inject
    private IPreferenceStore preferences;

    @Inject @org.eclipse.e4.core.di.annotations.Optional
    public Shell shell;

    @Inject
    @Translation
    protected ZFMessages msg;

    @Inject
    private IEclipseContext eclipseContext;

// GS/
    @Inject
    protected IDocumentAddressManager addressManager;
    @Inject
    protected ContactsDAO contactsDAO;

	enum FinancialRole { DEBTOR, CREDITOR }

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
    public boolean processPdf(Optional<Invoice> invoice) {
	    
        boolean result = checkSettings();
        if(result && invoice.isPresent() && invoice.get().getBillingType().isINVOICE()) {
// GS/
        	EmbeddedProperties propertiesFromContactNote = null;
//			ConformanceLevel zugferdProfile;
			ConformanceLevel zugferdProfile = null;
    	    // create e-invoice according to selected preferences
			
// GS/ check in contact.note properties if e-invoice is requested
	        Document invoiceDoc = invoice.get();
	        DocumentReceiver documentReceiver = addressManager.getBillingAdress(invoiceDoc);
	        String eInvoiceFormat = null; // Format as set in properties
	        if (documentReceiver != null && documentReceiver.getOriginContactId() != null) {
	            Contact contact = contactsDAO.findById(documentReceiver.getOriginContactId());
		        if (contact != null) {
		        	propertiesFromContactNote = new EmbeddedProperties(contact.getNote());
		    		if (propertiesFromContactNote != null) {
		    			eInvoiceFormat = propertiesFromContactNote.getProperty(EmbeddedProperties.PROPERTY_EINVOICE, null, null);
		    		}
		    		contact = null;
		        }
		        documentReceiver = null;
	        }
	        if (eInvoiceFormat == null ) {
	        	// no e-invoice for this contact requested => we're done here ;-)
	        	return true;
	        } else {
	        	try {
	        		zugferdProfile = ConformanceLevel.valueOf(eInvoiceFormat);
	        	} catch(Exception e) {
	        		;
	        	}
	        }
			
			IEinvoiceCreator invoiceCreator;
			// default is 2.1 - XRechnung
			// Attention: for default settings (i.e., version is 2.1), no preference is returned! 
			// Therefore we check this at the beginning.
// GS/ if invalid setting in contact.note.properties 'e-invoice' ...
			if (zugferdProfile == null || !(
					   zugferdProfile == ConformanceLevel.XRECHNUNG
					|| zugferdProfile == ConformanceLevel.FACTURX_COMFORT
					|| zugferdProfile == ConformanceLevel.ZUGFERD_V2_COMFORT
					|| zugferdProfile == ConformanceLevel.ZUGFERD_V2_EN16931
					|| zugferdProfile == ConformanceLevel.ZUGFERD_V1_COMFORT) ) {
	    	    if(eclipsePrefs.get(ZFConstants.PREFERENCES_ZUGFERD_VERSION, "2.1").contentEquals("2.1")) {
	                // currently only COMFORT profile is supported
	    			String conformanceLevel = eclipsePrefs.get(ZFConstants.PREFERENCES_ZUGFERD_PROFILE, ConformanceLevel.ZUGFERD_V2_COMFORT.getDescriptor());
	    	        zugferdProfile = ConformanceLevel.valueOf(conformanceLevel);
	    	    } else { // V1
	    	        zugferdProfile = ConformanceLevel.ZUGFERD_V1_COMFORT;
	//    	        invoiceCreator = ContextInjectionFactory.make(ZUGFeRDCreator.class, eclipseContext);
	    	    }
			}
	        invoiceCreator = ContextInjectionFactory.make(XRechnungCreator.class, eclipseContext);
            result = invoiceCreator.createEInvoice(invoice, zugferdProfile);
            
			if(!result) {
				// Display an error message
				MessageDialog.openError(shell, msg.zugferdExportCommandTitle, msg.zugferdExportErrorCancelled);
			}
		}
        return result;
	}

	/**
	 * Check if PDFs can be created
	 */
    private boolean checkSettings() {
        boolean result = true;
        if (!eclipsePrefs.get(Constants.PREFERENCES_OPENOFFICE_ODT_PDF, preferences.getDefaultString(Constants.PREFERENCES_OPENOFFICE_ODT_PDF))
                .contains(TargetFormat.PDF.getPrefId())) {
            result = false;
            MessageDialog.openError(shell, msg.zugferdExportCommandTitle, msg.zugferdExportErrorNopdfset);
        }
        if (result && eclipsePrefs.get(Constants.PREFERENCES_OPENOFFICE_PDF_PATH_FORMAT, preferences.getDefaultString(Constants.PREFERENCES_OPENOFFICE_PDF_PATH_FORMAT)).isEmpty()) {
            result = false;
            MessageDialog.openError(shell, msg.zugferdExportCommandTitle, msg.zugferdExportErrorNopdfpath);
        }

        // TODO Check required company settings
        return result;
    }

}
