/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2020 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package org.fakturama.export.wizard.contacts;

import java.util.Calendar;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.fakturama.export.wizard.OOCalcExporter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.sebulli.fakturama.exporter.IContactExporter;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * Exports a contact's datasheet.
 *
 */
@Component()
public class ContactDatasheetWriter extends OOCalcExporter implements IContactExporter {

    @Reference
    private ILocaleService localeUtil;

    //    @Reference
    private IEclipseContext context;

    @Reference
    private IDocumentAddressManager addressManager;

    @Override
    public boolean writeDatasheet(Contact contact) {
        if (contact != null) {

            // Try to generate a spreadsheet
            if (!createSpreadSheet())
                return false;

            context = EclipseContextFactory.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext());
            ContactUtil contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);

            // Counter for the current row in the Calc document
            int row = 0;

            setCellTextInBold(row++, 0, "Datenblatt Kunde");
            setCellTextInBold(row, 0, msg.editorContactFieldNumberName);
            // Place the contact information into the table
            setCellText(row, 1, contact.getCustomerNumber());
            /*
             * Das folgende ist eine Umgehungslösung.Beim Hinzufügen einer Spalte 
             * werden in den davor liegenden Zeilen diese Spalten ebenfalls mit ergänzt. 
             * Das funktioniert aber offenbar nicht, wenn diese Aktion erst ziemlich weit
             * am Ende der Tabelle gemacht wird. In diesem Fall hatte es dazu
             * geführt, daß die Felder bei Rabatt und Modified dupliziert  und mit 
             * ungültigen Werten befüllt wurden. Deswegen wurden hier bereits zu Beginn
             * der Tabelle zwei (eigentlich überflüssige) Leerzellen eingefügt.
             */
            setCellText(row, 2, " ");
            setCellText(row++, 3, " ");
            setCellTextInBold(row, 0, "TYPE");
            setCellText(row++, 1, contact.getClass().getSimpleName());
            if (contact.getCategories() != null) {
                setCellTextInBold(row, 0, msg.commonFieldCategory);
                setCellText(row++, 1, contact.getCategories().getName());
            }
            setCellTextInBold(row, 0, msg.commonFieldGender);
            setCellText(row++, 1, contactUtil.getSalutationString(contact.getGender()));
            setCellTextInBold(row, 0, msg.commonFieldTitle);
            setCellText(row++, 1, contact.getTitle());
            setCellTextInBold(row, 0, msg.commonFieldFirstname);
            setCellText(row++, 1, contact.getFirstName());
            setCellTextInBold(row, 0, msg.commonFieldLastname);
            setCellText(row++, 1, contact.getName());
            setCellTextInBold(row, 0, msg.commonFieldCompany);
            setCellText(row++, 1, contact.getCompany());
            
            for (Address address : contact.getAddresses()) {
                setCellTextInBold(row, 0, msg.editorContactFieldContacttype);
                if(address.getContactTypes() != null && !address.getContactTypes().isEmpty()) {
                    int col = 1;
                    for (ContactType contactType : address.getContactTypes()) {
                        setCellText(row++, col++, contactType.getName());
                    }
                }
                setCellTextInBold(row, 0, msg.commonFieldStreet);
                setCellText(row++, 1, address.getStreet());
                setCellTextInBold(row, 0, msg.commonFieldZipcode);
                setCellText(row++, 1, address.getZip());
                setCellTextInBold(row, 0, msg.commonFieldCity);
                setCellText(row++, 1, address.getCity());
                setCellTextInBold(row, 0, msg.commonFieldCountry);
                setCellText(row++, 1, localeUtil.findByCode(address.getCountryCode()).orElse(localeUtil.getDefaultLocale()).getDisplayCountry());
                setCellTextInBold(row, 0, msg.exporterDataTelephone);
                setCellText(row++, 1, address.getPhone());
                setCellTextInBold(row, 0, msg.exporterDataTelefax);
                setCellText(row++, 1, address.getFax());
                setCellTextInBold(row, 0, msg.exporterDataMobile);
                setCellText(row++, 1, address.getMobile());
                setCellTextInBold(row, 0, msg.exporterDataEmail);
                setCellText(row++, 1, address.getEmail());
            }


            if (contact.getBankAccount() != null) {
                setCellTextInBold(row, 0, msg.commonFieldAccountholder);
                setCellText(row++, 1, contact.getBankAccount().getAccountHolder());
                setCellTextInBold(row, 0, msg.editorContactFieldBankName);
                setCellText(row++, 1, contact.getBankAccount().getBankName());
                setCellTextInBold(row, 0, msg.exporterDataIban);
                setCellText(row++, 1, contact.getBankAccount().getIban());
                setCellTextInBold(row, 0, msg.exporterDataBic);
                setCellText(row++, 1, contact.getBankAccount().getBic());
            }

            setCellTextInBold(row, 0, msg.editorContactLabelNotice);
            setCellText(row++, 1, contact.getNote());

            if (contact.getPayment() != null) {
                setCellTextInBold(row, 0, msg.editorContactFieldPaymentName);
                setCellText(row++, 1, contact.getPayment().getDescription());
            }

            if (contact.getReliability() != null) {
                setCellTextInBold(row, 0, msg.editorContactFieldReliabilityName);
                setCellText(row++, 1, contactUtil.getReliabilityString(contact.getReliability()));
            }
            setCellTextInBold(row, 0, msg.exporterDataWebsite);
            setCellText(row++, 1, contact.getWebsite());
            setCellTextInBold(row, 0, msg.exporterDataVatno);
            setCellText(row++, 1, contact.getVatNumber());
            if (contact.getVatNumber() != null && !contact.getVatNumber().isEmpty()) {
                setCellTextInBold(row, 0, msg.exporterDataVatnoValid);
                setCellValueAsBoolean(row++, 1, contact.getVatNumberValid());
            }
            setCellTextInBold(row, 0, msg.exporterDataRebate);
            setCellValueAsPercent(row++, 1, contact.getDiscount());

            Calendar cal = Calendar.getInstance();
            if (contact.getBirthday() != null) {
                setCellTextInBold(row, 0, msg.editorContactFieldBirthdayName);
                cal.clear();
                cal.setTime(contact.getBirthday());
                setCellValueAsDate(row++, 1, cal);
            }
            
            cal.clear();
            cal.setTime(contact.getDateAdded());
            setCellTextInBold(row, 0, "added");
            setCellValueAsDate(row++, 1, cal);

            cal.clear();
            cal.setTime(contact.getModified());
            setCellTextInBold(row, 0, "modified");
            setCellValueAsDate(row, 1, cal);
            setCellText(row, 2, "by");
            setCellText(row++, 3, contact.getModifiedBy());

            save();
        }

        // True = Export was successful
        return true;
    }

    @Override
    protected String getOutputFileName() {
        return "Contacts_Data.ods";
    }
}
