/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2020 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package org.fakturama.export.facturx;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.money.MonetaryAmount;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.fakturama.export.einvoice.ConformanceLevel;
import org.fakturama.export.facturx.modelgen.AmountType;
import org.fakturama.export.facturx.modelgen.CodeType;
import org.fakturama.export.facturx.modelgen.CountryIDType;
import org.fakturama.export.facturx.modelgen.CreditorFinancialAccountType;
import org.fakturama.export.facturx.modelgen.CreditorFinancialInstitutionType;
import org.fakturama.export.facturx.modelgen.CrossIndustryInvoice;
import org.fakturama.export.facturx.modelgen.CurrencyCodeContentType;
import org.fakturama.export.facturx.modelgen.CurrencyCodeType;
import org.fakturama.export.facturx.modelgen.DateTimeType;
import org.fakturama.export.facturx.modelgen.DebtorFinancialAccountType;
import org.fakturama.export.facturx.modelgen.DocumentCodeType;
import org.fakturama.export.facturx.modelgen.DocumentContextParameterType;
import org.fakturama.export.facturx.modelgen.DocumentLineDocumentType;
import org.fakturama.export.facturx.modelgen.ExchangedDocumentContextType;
import org.fakturama.export.facturx.modelgen.ExchangedDocumentType;
import org.fakturama.export.facturx.modelgen.FormattedDateTimeType;
import org.fakturama.export.facturx.modelgen.FormattedDateTimeType.DateTimeString;
import org.fakturama.export.facturx.modelgen.HeaderTradeAgreementType;
import org.fakturama.export.facturx.modelgen.HeaderTradeDeliveryType;
import org.fakturama.export.facturx.modelgen.HeaderTradeSettlementType;
import org.fakturama.export.facturx.modelgen.IDType;
import org.fakturama.export.facturx.modelgen.LegalOrganizationType;
import org.fakturama.export.facturx.modelgen.LineTradeAgreementType;
import org.fakturama.export.facturx.modelgen.LineTradeDeliveryType;
import org.fakturama.export.facturx.modelgen.LineTradeSettlementType;
import org.fakturama.export.facturx.modelgen.NoteType;
import org.fakturama.export.facturx.modelgen.ObjectFactory;
import org.fakturama.export.facturx.modelgen.PaymentMeansCodeType;
import org.fakturama.export.facturx.modelgen.QuantityType;
import org.fakturama.export.facturx.modelgen.ReferencedDocumentType;
import org.fakturama.export.facturx.modelgen.SpecifiedPeriodType;
import org.fakturama.export.facturx.modelgen.SupplyChainEventType;
import org.fakturama.export.facturx.modelgen.SupplyChainTradeLineItemType;
import org.fakturama.export.facturx.modelgen.SupplyChainTradeTransactionType;
import org.fakturama.export.facturx.modelgen.TaxCategoryCodeContentType;
import org.fakturama.export.facturx.modelgen.TaxCategoryCodeType;
import org.fakturama.export.facturx.modelgen.TaxRegistrationType;
import org.fakturama.export.facturx.modelgen.TaxTypeCodeContentType;
import org.fakturama.export.facturx.modelgen.TaxTypeCodeType;
import org.fakturama.export.facturx.modelgen.TextType;
import org.fakturama.export.facturx.modelgen.TradeAddressType;
import org.fakturama.export.facturx.modelgen.TradeAllowanceChargeType;
import org.fakturama.export.facturx.modelgen.TradeContactType;
import org.fakturama.export.facturx.modelgen.TradeCountryType;
import org.fakturama.export.facturx.modelgen.TradePartyType;
import org.fakturama.export.facturx.modelgen.TradePaymentTermsType;
import org.fakturama.export.facturx.modelgen.TradePriceType;
import org.fakturama.export.facturx.modelgen.TradeProductType;
import org.fakturama.export.facturx.modelgen.TradeSettlementHeaderMonetarySummationType;
import org.fakturama.export.facturx.modelgen.TradeSettlementLineMonetarySummationType;
import org.fakturama.export.facturx.modelgen.TradeSettlementPaymentMeansType;
import org.fakturama.export.facturx.modelgen.TradeTaxType;
import org.fakturama.export.facturx.modelgen.UniversalCommunicationType;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.dto.Transaction;
import com.sebulli.fakturama.dto.VatSummaryItem;
import com.sebulli.fakturama.dto.VatSummarySetManager;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.EmbeddedProperties;
import com.sebulli.fakturama.misc.Util;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BankAccount;
import com.sebulli.fakturama.model.CEFACTCode;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.office.Placeholders;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.util.DocumentTypeUtil;

/**
 * Create an XRechnung XML.
 */
public class XRechnung extends AbstractEInvoice {
    
    private ContactUtil contactUtil;

    private DocumentAllowances itemAllowances;

 // GS/ embedded properties from contact.note
    EmbeddedProperties propertiesFromContactNote = null;


    @Override
    public CrossIndustryInvoice getInvoiceXml(Optional<Invoice> invoiceDoc) {
        if(!invoiceDoc.isPresent()) {
            return null;
        }
        contactUtil = ContextInjectionFactory.make(ContactUtil.class, eclipseContext);
        factory = new ObjectFactory();
        itemAllowances = new DocumentAllowances();

        Document invoice = invoiceDoc.get();
// GS/ contact.note.properties
        DocumentReceiver documentReceiver = addressManager.getBillingAdress(invoice);
        Contact contact = getOriginContact(documentReceiver);
        if (contact != null) {
    		propertiesFromContactNote = new EmbeddedProperties(contact.getNote());
        } else {
        	propertiesFromContactNote = new EmbeddedProperties();
        }
// GS/ -end-

        // Recalculate the sum of the document before exporting
        DocumentSummaryCalculator documentSummaryCalculator = ContextInjectionFactory.make(DocumentSummaryCalculator.class, eclipseContext);
        DocumentSummary documentSummary = documentSummaryCalculator.calculate(invoice);

        CrossIndustryInvoice root = factory.createCrossIndustryInvoice();
        // at first create a reasonable context
        DocumentContextParameterType ctxParam = factory.createDocumentContextParameterType()
                .withID(createIdFromString(ConformanceLevel.XRECHNUNG.getUrn()));
        
        // TODO specify the type in DocumentEditor (free text?)
        ExchangedDocumentContextType exchangedDocCtx = factory.createExchangedDocumentContextType()
                .withGuidelineSpecifiedDocumentContextParameter(ctxParam)
        /*      .withBusinessProcessSpecifiedDocumentContextParameter(
                        factory.createDocumentContextParameterType()
                        .withID(createIdFromString("Baurechnung")))*/;
        
        root.setExchangedDocumentContext(exchangedDocCtx);
        
        // now the header information follows
        ExchangedDocumentType exchangedDocumentType = factory.createExchangedDocumentType()
                .withID(createIdFromString(invoice.getName()));
        DocumentType documentType = DocumentTypeUtil.findByBillingType(invoice.getBillingType());
        
        if(documentType.getCode() > 0) {
            DocumentCodeType docTypeCode = factory.createDocumentCodeType()
                    .withValue(Integer.toString(documentType.getCode()));
            exchangedDocumentType.setTypeCode(docTypeCode);
        }
        exchangedDocumentType.setIssueDateTime(createDateTime(invoice.getDocumentDate()));
        
        Optional.ofNullable(createNote(invoice.getMessage())).ifPresent(n -> exchangedDocumentType.getIncludedNote().add(n));
        Optional.ofNullable(createNote(invoice.getMessage2())).ifPresent(n -> exchangedDocumentType.getIncludedNote().add(n));
        Optional.ofNullable(createNote(invoice.getMessage3())).ifPresent(n -> exchangedDocumentType.getIncludedNote().add(n));
        
        NoteType note = factory.createNoteType();
        
        String owner = String.format("%s%n%s%n%s%n%s %s%n%s", 
                preferences.getString(Constants.PREFERENCES_YOURCOMPANY_OWNER),
                preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME),
                preferences.getString(Constants.PREFERENCES_YOURCOMPANY_STREET),
                preferences.getString(Constants.PREFERENCES_YOURCOMPANY_ZIP),
                preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CITY),
                preferences.getString(Constants.PREFERENCES_YOURCOMPANY_VATNR));
        if(StringUtils.isBlank(owner)) {
            MessageDialog.openWarning(shell, messages.dialogMessageboxTitleWarning, zfMsg.zugferdExportErrorEmptycompanypref);
            owner = "(unknown)";
        }

        note.setContent(createText(owner));  // should only be free text for information about the invoice
        note.setSubjectCode(createCode("REG")); // see UNTDID 4451, explains the note content
        exchangedDocumentType.getIncludedNote().add(note);
        root.setExchangedDocument(exchangedDocumentType);
        
        // now follows the huge part for trade transaction
        SupplyChainTradeTransactionType tradeTransaction = factory.createSupplyChainTradeTransactionType();
// GS/
//        HeaderTradeAgreementType tradeAgreement = factory.createHeaderTradeAgreementType()
//                .withBuyerReference(createText(invoice.getCustomerRef()));  // "Kundenreferenz"
		HeaderTradeAgreementType tradeAgreement = factory.createHeaderTradeAgreementType();

		// BT-10 (Leitweg-ID)
		String sFieldValue = null;
		if (!propertiesFromContactNote.isEmpty()) {
			sFieldValue = propertiesFromContactNote.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT10, null, null);
		} else {
			sFieldValue = EmbeddedProperties.VALUE_FIELD_CUSTREF;
		}
		if (EmbeddedProperties.VALUE_FIELD_CUSTREF.equals(sFieldValue)) {
			sFieldValue = invoice.getCustomerRef();
		} else if (EmbeddedProperties.VALUE_FIELD_CUSTREF_LEFT.equals(sFieldValue)) {
			sFieldValue = EmbeddedProperties.left(invoice.getCustomerRef());
		}
		if (sFieldValue != null) {
			tradeAgreement.setBuyerReference(createText(sFieldValue));
		} else {
/* TODO GS/ this should not be, at least for XRechnung it's a required field
 * there shall be at least a rough validation on/after generation to inform user about missing/invalid data (in general)
 * idea: (2 B tested) in the info dialog after generation add link(s) to online validators
 *   to encourage user to validate the document ... maybe later on a link to a locally installed tool?
 */
		}
// GS/ -end-
        tradeTransaction.setApplicableHeaderTradeAgreement(tradeAgreement);
        
        // create seller information
        tradeAgreement.setSellerTradeParty(createSeller(invoice));
        
        // create buyer information
        TradePartyType buyer = createBuyer(invoice);
        if(buyer != null) {
            tradeAgreement.setBuyerTradeParty(buyer);
        }
        
// GS/
        if (!propertiesFromContactNote.isEmpty()) {
        	// BT-13 (Bestellnr.)
	        sFieldValue = propertiesFromContactNote.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT13, null, null);
	        if (EmbeddedProperties.VALUE_FIELD_CUSTREF.equals(sFieldValue)) {
	        	sFieldValue = invoice.getCustomerRef();
	        } else if (EmbeddedProperties.VALUE_FIELD_CUSTREF_LEFT.equals(sFieldValue)) {
	        	sFieldValue = EmbeddedProperties.left(invoice.getCustomerRef());
	        } else if (EmbeddedProperties.VALUE_FIELD_ORDER_NAME.equals(sFieldValue)) {
		        Transaction transaction = ContextInjectionFactory.make(Transaction.class, eclipseContext).of(invoice);
		        if(transaction != null)
		        	sFieldValue = Util.defaultIfEmpty(transaction.getReference(DocumentType.ORDER), null);
	        }
	        if (sFieldValue != null) {
		        tradeAgreement.setBuyerOrderReferencedDocument(factory.createReferencedDocumentType()
		        		.withIssuerAssignedID(createIdFromString(sFieldValue)));
	        }
        	// BT-12 (Vertragsnr.)
	        sFieldValue = propertiesFromContactNote.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT12, null, null);
	        if (EmbeddedProperties.VALUE_FIELD_CUSTREF.equals(sFieldValue)) {
	        	sFieldValue = invoice.getCustomerRef();
	        } else if (EmbeddedProperties.VALUE_FIELD_CUSTREF_LEFT.equals(sFieldValue)) {
	        	sFieldValue = EmbeddedProperties.left(invoice.getCustomerRef());
	        }
	        if (sFieldValue != null) {
		        tradeAgreement.setContractReferencedDocument(factory.createReferencedDocumentType()
		        		.withIssuerAssignedID(createIdFromString(sFieldValue)));
	        }
	        // BT-11 (Projektreferenz)
	        sFieldValue = propertiesFromContactNote.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT11, null, null);
	        if (EmbeddedProperties.VALUE_FIELD_CUSTREF.equals(sFieldValue)) {
	        	sFieldValue = invoice.getCustomerRef();
	        } else if (EmbeddedProperties.VALUE_FIELD_CUSTREF_LEFT.equals(sFieldValue)) {
	        	sFieldValue = EmbeddedProperties.left(invoice.getCustomerRef());
	        }
	        if (sFieldValue != null) {
		        tradeAgreement.setSpecifiedProcuringProject(factory.createProcuringProjectType()
		        		.withID(createIdFromString(sFieldValue))
		        		.withName(createText(sFieldValue)));
	        }
        }
// GS/ -end-
        
//        tradeAgreement.setSellerTaxRepresentativeTradeParty(value);  // Steuerbevollmächtigter des Verkäufers
//        tradeAgreement.setSellerOrderReferencedDocument(value); // Detailangaben zur zugehörigen Auftragsbestätigung
        
        // referenced order
        if (propertiesFromContactNote.isEmpty()) { // GS/
	        Transaction transaction = ContextInjectionFactory.make(Transaction.class, eclipseContext).of(invoice);
	        if(transaction != null) {
	            ReferencedDocumentType orderRef = factory.createReferencedDocumentType()
	                    .withIssuerAssignedID(createIdFromString(transaction.getReference(DocumentType.ORDER)));
	            // only if ID is not empty!
	            if(!StringUtils.isEmpty(transaction.getReference(DocumentType.ORDER))) {
	                tradeAgreement.setBuyerOrderReferencedDocument(orderRef);
	            }
	        }
        }

        // there is no contract information in Fakturama!
//          ReferencedDocumentType contractRef = factory.createReferencedDocumentType()
//                  .withIssuerAssignedID(createIdFromString("contractNumber"));
//          tradeAgreement.setContractReferencedDocument(contractRef);
            
//          ReferencedDocumentType additionalReferencedDocument = factory.createReferencedDocumentType()
//                  .withIssuerAssignedID(createIdFromString("contractNumber"))
//                  .withURIID(createIdFromString("URI"))
//                  .withTypeCode(factory.createDocumentCodeType().withValue("130"))  // UNTDID 1001
//                  .withName(createText("AttachmentName"))
//                  .withAttachmentBinaryObject(factory.createBinaryObjectType()
//                          .withFilename("filename").withMimeCode("mime").withValue(new byte[] {}))
//                  ;
//          tradeAgreement.setAdditionalReferencedDocument(additionalReferencedDocument);
          
//          tradeAgreement.setSpecifiedProcuringProject(factory.createProcuringProjectType().withName("").withID(createIdFromString("Project")));
            
        SupplyChainEventType deliveryEvent = factory.createSupplyChainEventType()
                .withOccurrenceDateTime(createDateTime(invoice.getServiceDate()));
        
        HeaderTradeDeliveryType headerTradeDeliveryType = factory.createHeaderTradeDeliveryType()
                .withShipToTradeParty(buyer)
                .withActualDeliverySupplyChainEvent(deliveryEvent)
//                .withDespatchAdviceReferencedDocument(value)
//                .withReceivingAdviceReferencedDocument(value)
                ;
        
        tradeTransaction.setApplicableHeaderTradeDelivery(headerTradeDeliveryType);
        
        CurrencyCodeContentType currency = getGlobalCurrencyCode();
      
      // Verwendungszweck, Kassenzeichen 
        HeaderTradeSettlementType tradeSettlement = factory.createHeaderTradeSettlementType()
//                .withCreditorReferenceID(createIdWithSchemeFromString(idString, scheme))
                .withPaymentReference(createText(invoice.getName()))
//                .withTaxCurrencyCode(createCurrencyCode(currency))  // see ISO 4217
                .withInvoiceCurrencyCode(createCurrencyCode(currency))
                ;

        // TODO tradeSettlement.setPayeeTradeParty(value);   // Zahlungsempfänger
// GS/ already used above (to read the properties from contact's note
//        DocumentReceiver documentReceiver = addressManager.getBillingAdress(invoice);
//        Contact contact = getOriginContact(documentReceiver);
// GS/ -end-
        if (contact != null) {
            DebtorFinancialAccountType debtorAccount = createDebtorAccount(contact.getBankAccount());

            TradeSettlementPaymentMeansType paymentType = factory.createTradeSettlementPaymentMeansType()
                    .withTypeCode(createPaymentTypeCode(invoice))
                    .withInformation(createText(invoice.getPayment().getName()))
//                    .withApplicableTradeSettlementFinancialCard(value)
                    .withPayerPartyDebtorFinancialAccount(debtorAccount)
    //                                .withPaymentReference(createText(invoice.getName())) /* customerref ? */
                    ;

            CreditorFinancialAccountType creditor = createCreditorAccount();
            if (creditor != null) {
                paymentType.setPayeePartyCreditorFinancialAccount(creditor);
                paymentType.setPayeeSpecifiedCreditorFinancialInstitution(createCreditorFinancialInstitution());
            }
            tradeSettlement.getSpecifiedTradeSettlementPaymentMeans().add(paymentType);
        }

        // Get the items of the UniDataSet document
        invoice.getItems().forEach(item -> tradeTransaction.getIncludedSupplyChainTradeLineItem().add(createLineItem(item)));
        
        // Detailinformationen zur Rechnungsperiode 
        if(invoice.getVestingPeriodStart() != null || invoice.getVestingPeriodEnd() != null) {
            SpecifiedPeriodType billingSpecificPeriod = createBillingSpecificPeriod(invoice);
            tradeSettlement.setBillingSpecifiedPeriod(billingSpecificPeriod);
        }
        
        // TODO tradeSettlement.setBillingSpecifiedPeriod(createPeriod(invoice));
        // Abschläge / Zuschläge nur aufführen wenn sie auch tatsächlich angefallen sind! 
        if(Optional.ofNullable(invoice.getItemsRebate()).orElse(Double.valueOf(0.0)).compareTo(Double.valueOf(0.0)) != 0) {
            tradeSettlement.getSpecifiedTradeAllowanceCharge().add(createTradeAllowance(documentSummary, invoice));
        }
        // Hier kommen auch die Versandkosten mit rein 
        // (die sind nur bei EXTENDED in einem extra Node)
        if(invoice.getShipping() != null && invoice.getShipping().getShippingValue() > 0 || invoice.getShippingValue() > 0) {
            tradeSettlement.getSpecifiedTradeAllowanceCharge().add(createTradeAllowance(invoice));
        }
        tradeSettlement.getSpecifiedTradePaymentTerms().add(createTradePaymentTerms(invoice, documentSummary));
        tradeSettlement.setSpecifiedTradeSettlementHeaderMonetarySummation(createTradeSettlementMonetarySummation(invoice, documentSummary));

//        tradeSettlement.setInvoiceReferencedDocument(factory.createReferencedDocumentType()
//                .withIssuerAssignedID(createIdFromString("previousInvoice"))
//                // Rechnungsdatum der vorausgegangenen Rechnung
//                .withFormattedIssueDateTime(createFormattedDateTime(new Date())))   
//        ;
        
        //  tradeSettlement.setReceivableSpecifiedTradeAccountingAccount(null);

        // Get the VAT summary of the UniDataSet document
        VatSummarySetManager vatSummarySetManager = ContextInjectionFactory.make(VatSummarySetManager.class, eclipseContext);
        vatSummarySetManager.getVatSummaryItems().clear();
        vatSummarySetManager.add(invoice, Double.valueOf(1.0));
        for (VatSummaryItem vatSummaryItem : vatSummarySetManager.getVatSummaryItems()) {
            // für jeden Steuerbetrag muß es einen eigenen Eintrag geben
            tradeSettlement.getApplicableTradeTax().add(createTradeTax(vatSummaryItem));
        }
        tradeTransaction.setApplicableHeaderTradeSettlement(tradeSettlement);
        root.setSupplyChainTradeTransaction(tradeTransaction);
        return root;
    }

    private TradePartyType createBuyer(Document invoice) {
        DocumentReceiver documentReceiver = addressManager.getBillingAdress(invoice);
        if(documentReceiver != null) {
            TradePartyType buyer = factory.createTradePartyType()
                    .withName(createText(invoice.getAddressFirstLine()))
    //                .withSpecifiedLegalOrganization(createLegalOrganizationType(invoice))  // not available
                    .withPostalTradeAddress(createAddress(invoice, ContactType.BUYER))
//                    .withURIUniversalCommunication(email)
                    .withSpecifiedTaxRegistration(createTaxNumber(invoice, ContactType.BUYER))
                    ;
/* GS/ why not the easier way?
            Long originContactId = invoice.getReceiver() != null && !invoice.getReceiver().isEmpty() 
                    ? invoice.getReceiver().get(0).getOriginContactId() 
                    : Long.valueOf(0);
            if(originContactId != null && originContactId != 0) {
                Contact originContact = contactsDAO.findById(originContactId);
*/
			Contact originContact = getOriginContact(documentReceiver);
			if (originContact != null) {
				if (!propertiesFromContactNote.isEmpty()) {
					// Buyer ID
			        String sFieldValue = Util.defaultIfEmpty(
			        		propertiesFromContactNote.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BUYER_ID),
			        		documentReceiver.getCustomerNumber());
					if (sFieldValue != null)
						buyer.getID().add(createIdFromString(sFieldValue));
					// Buyer GlobalID (+ schemeID)
			        sFieldValue = propertiesFromContactNote.getProperty
			        		(EmbeddedProperties.PROPERTY_EINVOICE_BUYER_GLOBALID, null, null);
			        String schemeID = null;
					if (sFieldValue != null) {
						schemeID = propertiesFromContactNote.getProperty
				        		(EmbeddedProperties.PROPERTY_EINVOICE_BUYER_GLOBALID_SCHEMEID, null, null);
					} else {
						// DocumentReceiver.GLN ?
						sFieldValue = (documentReceiver.getGln() != null ? documentReceiver.getGln().toString() : null);
						if (sFieldValue != null)
							schemeID = "0088"; // EAN Location Code
					}
					if (sFieldValue != null) {
						if (schemeID != null)
			                buyer.getGlobalID().add(createIdWithSchemeFromString(sFieldValue, schemeID));
						else
			                buyer.getGlobalID().add(createIdFromString(sFieldValue));
					}
				} else {
// GS/ -end-
	                String schemaId = "0088";
	                String globalId = Optional.ofNullable(originContact.getGln()).orElse(Long.valueOf(0)).toString();
	                String debtorId = documentReceiver.getCustomerNumber();
	                
	                // additional fields from customer note takes precedence over "regular" fields 
	                if(originContact.getNote() != null) {
	                    debtorId = grepFromNoteField(originContact.getNote(), ".*?Debtor ID=(\\p{Alnum}+).*", debtorId);
	                    globalId = grepFromNoteField(originContact.getNote(), ".*?Global ID=(\\p{Alnum}+).*", globalId);
	                    schemaId = grepFromNoteField(originContact.getNote(), ".*?Global schemeID=(\\d+).*", schemaId);
	                }
	                
	                if (StringUtils.isNotEmpty(globalId)) {
	                    buyer.getGlobalID().add(createIdWithSchemeFromString(globalId, StringUtils.defaultString(schemaId)));
	                } else {
	                    buyer.getID().add(createIdFromString(debtorId));
	                }
				}
            } else {
                 buyer.getID().add(createIdFromString(documentReceiver.getCustomerNumber()));
            }
            return buyer;
        }
        return null;
    }

    private String grepFromNoteField(String noteField, String searchPattern, String defaultString) {
        // try to find a global schema id from note field (workaround)
        Pattern p = Pattern.compile(searchPattern, Pattern.MULTILINE| Pattern.DOTALL| Pattern.CASE_INSENSITIVE  );
        Matcher m = p.matcher(noteField);noteField.matches("Global.*");
        return m.matches() ? m.group(1) : defaultString;
    }

    private TradePartyType createSeller(Document invoice) {
        UniversalCommunicationType email = factory.createUniversalCommunicationType()
                // EM = Electronic mail (SMPT)
                .withURIID(createIdWithSchemeFromString(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_EMAIL), "EM"));
// GS/ Supplier-Number
        IDType theId = null;
        DocumentReceiver documentReceiver = addressManager.getBillingAdress(invoice);
        if (documentReceiver != null) {
        	// DocumentReceiver.SupplierNumber ?
        	String sFieldValue = Util.defaultIfEmpty(documentReceiver.getSupplierNumber(), null);
        	if (sFieldValue != null)
        		theId = createIdFromString(sFieldValue);
        }
        TradePartyType seller = factory.createTradePartyType()
              .withID(theId)  // Kennung des Verkäufers (Durch den Kunden zugewiesene Lieferantennummer)
              .withName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME)))
//              .withDescription(createText(""))  // Sonstige rechtliche Informationen des Verkäufers
              .withSpecifiedLegalOrganization(createLegalOrganizationType(invoice))
              .withDefinedTradeContact(createContact(invoice, ContactType.SELLER))
              .withPostalTradeAddress(createAddress(invoice, ContactType.SELLER))
              .withURIUniversalCommunication(email)
              .withSpecifiedTaxRegistration(createTaxNumber(invoice, ContactType.SELLER))
              .withSpecifiedLegalOrganization(createLegalOrganization(invoice, ContactType.SELLER))
                ;
/* TODO GS/ is this really correct???
        if(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_VATNR) == null) {
            seller.getGlobalID().add(createIdFromString(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TAXNR)));
        } else {
            seller.getGlobalID().add(createIdWithSchemeFromString(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_VATNR), "0088"));  // "EAN" according to ISO 6523
        }
*/
        
        return seller;
    }


    /**
     * Details zur Organisation
     * @param invoice
     * @param seller
     * @return
     */
    private LegalOrganizationType createLegalOrganization(Document invoice, ContactType seller) {
        LegalOrganizationType retval = factory.createLegalOrganizationType()
                .withID(createIdFromString(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_VATNR)));
        if(!preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME).equalsIgnoreCase(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_OWNER))) {
            retval.setTradingBusinessName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME)));
        }
        return retval;
    }

    private LegalOrganizationType createLegalOrganizationType(Document invoice) {
        return factory.createLegalOrganizationType()
                .withID(createIdFromString("GTIN"))
                .withTradingBusinessName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME)))
                ;
    }

    /**
     * @param invoice
     * @param exchangedDocumentType
     */
    private NoteType createNote(String message) {
        NoteType note = null;
        if(!StringUtils.isEmpty(message)) {
            note = factory.createNoteType(); // free text on header level
            note.setContent(createText(message));
            note.setSubjectCode(createCode("AAK")); // see UNTDID 4451
        }
        return note;
    }

    /**
     * @return
     */
    private CurrencyCodeContentType getGlobalCurrencyCode() {
        String currency = DataUtils.getInstance().getDefaultCurrencyUnit().getCurrencyCode();
        CurrencyCodeContentType retval;
      // TODO later on we will use JSR 354...
      switch (currency) {
      case "€":
          retval = CurrencyCodeContentType.EUR;
          break;
      case "$":
          retval = CurrencyCodeContentType.USD;
          break;
      default:
          retval = CurrencyCodeContentType.EUR;
          break;
      }
        return retval;
    }
    
    /**
     * Gruppierung der Informationen zum Geschäftsvorfall
     * 
     * @param item
     * @return
     */
    private SupplyChainTradeLineItemType createLineItem(DocumentItem item) {
        SupplyChainTradeLineItemType retval = factory.createSupplyChainTradeLineItemType()
                .withAssociatedDocumentLineDocument(createDocumentLine(item))
                .withSpecifiedTradeProduct(createTradeProduct(item))
                .withSpecifiedLineTradeAgreement(createLineTradeAgreement(item))
                .withSpecifiedLineTradeDelivery(createLineTradeDelivery(item))
                .withSpecifiedLineTradeSettlement(createLineTradeSettlement(item))
                
                ;
        return retval;
    }

    /**
     * Gruppierung von Angaben zum Produkt bzw. zur erbrachten Leistung. Eine
     * Gruppe von betriebswirtschaftlichen Begriffen, die Informationen über die
     * in Rechnung gestellten Waren und Dienstleistungen enthält.
     * 
     * @param item
     * @return
     */
    private TradeProductType createTradeProduct(DocumentItem item) {
        TradeProductType retval = factory.createTradeProductType()
    //          .withGlobalID(createIdFromString("EAN")) // see ISO 6523
                .withSellerAssignedID(createIdFromString(item.getItemNumber()))  // perhaps GTIN?
                .withBuyerAssignedID(createIdFromString("buyerassigned ID"))
                .withName(createText(item.getName()))
                .withDescription(createText(item.getDescription()))
//                .withDesignatedProductClassification(values)  // see UNTDID 7143
//                .withApplicableProductCharacteristic(item.getProduct().getAttributes())
//                .withOriginTradeCountry(createTradeCountry(item))
                ;
        return retval;
    }

    private TradeCountryType createTradeCountry(DocumentItem item) {
        return factory.createTradeCountryType()
                .withID(createCountry("DE"));
    }

    /**
     * Gruppierung von Angaben zur Abrechnung auf Positionsebene 
     * @param item
     * @return
     */
    private LineTradeSettlementType createLineTradeSettlement(DocumentItem item) {
        LineTradeSettlementType retval = factory.createLineTradeSettlementType()
                .withApplicableTradeTax(createTradeTax(item.getItemVat()))
                .withSpecifiedTradeSettlementLineMonetarySummation(createTradeSettlementLineMonetarySummation(item))
 //               .withAdditionalReferencedDocument(null);
//                .withReceivableSpecifiedTradeAccountingAccount(null)
                ;
        if(item.getVestingPeriodStart() != null || item.getVestingPeriodEnd() != null) {
            retval.setBillingSpecifiedPeriod(createBillingSpecificPeriod(item));
        }
        
        if(item.getItemRebate() != null && item.getItemRebate().compareTo(Double.valueOf(0.0)) != 0) {
            retval.getSpecifiedTradeAllowanceCharge().addAll(createAllowanceCharges(item));
        }
        return retval;
    }

    private Collection<TradeAllowanceChargeType> createAllowanceCharges(DocumentItem item) {
        List<TradeAllowanceChargeType> charges = new ArrayList<>();
        TradeAllowanceChargeType charge = createTradeAllowance(item);
        charges.add(charge);
        return charges;
    }

    private SpecifiedPeriodType createBillingSpecificPeriod(DocumentItem item) {
        SpecifiedPeriodType retval = factory.createSpecifiedPeriodType();
        if(item.getVestingPeriodStart() != null) {
            retval.setStartDateTime(createDateTime(item.getVestingPeriodStart()));
        }
        
        if(item.getVestingPeriodEnd() != null) {
            retval.setEndDateTime(createDateTime(item.getVestingPeriodEnd()));
        }
        return retval;
    }

    private SpecifiedPeriodType createBillingSpecificPeriod(Document item) {
        SpecifiedPeriodType retval = factory.createSpecifiedPeriodType();
        if(item.getVestingPeriodStart() != null) {
            retval.setStartDateTime(createDateTime(item.getVestingPeriodStart()));
        }
        
        if(item.getVestingPeriodEnd() != null) {
            retval.setEndDateTime(createDateTime(item.getVestingPeriodEnd()));
        }
        return retval;
    }
    
    private LineTradeDeliveryType createLineTradeDelivery(DocumentItem item) {
        String qunit = determineQuantityUnit(item.getQuantityUnit());
        return factory.createLineTradeDeliveryType()
                .withBilledQuantity(createQuantity(item.getQuantity(), qunit))
                ;
    }

    private QuantityType createQuantity(Double value, String unit) {
        return factory.createQuantityType()
                .withValue(BigDecimal.valueOf(value))
                // use a default value since this field shouldn't be empty
                .withUnitCode(StringUtils.isBlank(unit) ? "C62" : unit);
    }

    /**
     * Detailinformationen zum Preis. Eine Gruppe von betriebswirtschaftlichen
     * Begriffen, die Informationen über den Preis für die in der betreffenden
     * Rechnungsposition in Rechnung gestellten Waren und Dienstleistungen
     * enthält.
     * 
     * @param item
     * @return
     */
    private LineTradeAgreementType createLineTradeAgreement(DocumentItem item) {
        LineTradeAgreementType retval = factory.createLineTradeAgreementType()
//          .withBuyerOrderReferencedDocument(createBuyerOrderReferencedDocument(item))
          .withGrossPriceProductTradePrice(createTradePrice(item, PriceType.GROSS_PRICE))
          .withNetPriceProductTradePrice(createTradePrice(item, PriceType.NET_PRICE_DISCOUNTED))
                ;
        return retval;
    }

  /**
   * Detailangaben zur zugehörigen Bestellung 
   * @param item
   * @return
   */
  private ReferencedDocumentType createBuyerOrderReferencedDocument(DocumentItem item) {
      // hier ist die Position in der zugehörigen Bestellung gemeint!
      return factory.createReferencedDocumentType()
              .withLineID(createIdFromString(Integer.toString(item.getPosNr())));
    }

    /**
     * Detailinformationen zum Bruttopreis des Artikels. 
     * @param item
     * @param priceType 
     * @return
     */
    private TradePriceType createTradePrice(DocumentItem item, PriceType priceType) {
        Price price = new Price(item);
        TradePriceType retval = null;
        String qunit = determineQuantityUnit(item.getQuantityUnit());
        double discount = item.getItemRebate();
        switch (priceType) {
        case GROSS_PRICE:
            retval = factory.createTradePriceType()
                // "ITEM.UNIT.NET.DISCOUNTED" oder "ITEM.TOTAL.NET"?
                // Preis nach Bruttokalkulation *ohne* Umsatzsteuer(!!!) 
                .withChargeAmount(createAmount(price.getUnitNet(), 2))
                // Die Anzahl von Artikeleinheiten, für die der Preis gilt (Preisbasismenge ==> 1, 10, 100,...)
            //.withBasisQuantity(createQuantity(item.getProduct().getBlock1(), qunit))
                ;
                if(discount != 0) {
                    // Rabatt / Zuschlag auf Positionsebene
                    retval.getAppliedTradeAllowanceCharge().add(createTradeAllowance(item, false));
                }
            break;
        case NET_PRICE:
            
            retval = factory.createTradePriceType()
            // Preis nach Bruttokalkulation ohne Umsatzsteuer 
            .withChargeAmount(createAmount(Money.of(item.getPrice(), DataUtils.getInstance().getDefaultCurrencyUnit()), 
                    DEFAULT_AMOUNT_SCALE))
            // TODO Preisbasismenge??? (1, 10, 100,...)
//          .withBasisQuantity(createQuantity(1d, qunit))
            ;
          if(discount != 0) {
              // Rabatt / Zuschlag auf Positionsebene
              retval.getAppliedTradeAllowanceCharge().add(createTradeAllowance(item));
          }
            break;
        case NET_PRICE_DISCOUNTED:
            // Detailinformationen zum Preis gemäß Nettokalkulation exklusive Umsatzsteuer
            retval = factory.createTradePriceType()
                // "ITEM.UNIT.NET.DISCOUNTED"
                // Preis nach Bruttokalkulation +- Zu-/Abschläge = Preis 
                // nach Nettokalkulation;
                .withChargeAmount(createAmount(price.getUnitNetDiscounted(), 2))
                // TODO Preisbasismenge??? (1, 10, 100,...)
//                .withBasisQuantity(createQuantity(1d, qunit))
                ;
//          if(discount != 0) {
//              // Rabatt / Zuschlag auf Positionsebene
//              retval.getAppliedTradeAllowanceCharge().add(createTradeAllowance(item));
//          }
            break;

        default:
            break;
        }
        return retval;
    }

    /**
     * Bestimmt aus der Mengeneinheit das entsprechende ISO-Kürzel.
     * Umrechnung entsprechend der von ZUGFeRD gelieferten Codeliste.
     * TODO in einer späteren Version sollten die Mengeneinheiten per
     * Auswahlbox angelegt werden.
     * 
     * @param userdefinedQuantityUnit
     * @return ISOquantityUnit
     */
    private String determineQuantityUnit(String userdefinedQuantityUnit) {
        String isoUnit = "";
        if(StringUtils.isNotBlank(userdefinedQuantityUnit)) {
            Optional<CEFACTCode> code = measureUnits.findByAbbreviation(userdefinedQuantityUnit, localeUtil.getDefaultLocale());
            isoUnit = code.isPresent() ? code.get().getCode() : "";
        }
        return isoUnit;
    }

    private DocumentLineDocumentType createDocumentLine(DocumentItem item) {
        DocumentLineDocumentType retval = factory.createDocumentLineDocumentType()
                .withLineID(createIdFromString(item.getPosNr().toString()));
        // TODO Detailinformationen zum Freitext zur Position 
        //Optional.ofNullable(createNote(item.getDescription())).ifPresent(n -> retval.getIncludedNote().add(n));
        return retval;
    }

    /**
     * Detailinformationen zu Belegsummen.
     * 
     * @param invoice
     * @param documentSummary 
     * @return
     */
    private TradeSettlementHeaderMonetarySummationType createTradeSettlementMonetarySummation(Document invoice, DocumentSummary documentSummary) {
        MonetaryAmount allowanceAmount = documentSummary.getDiscountNet();
        if(!allowanceAmount.isPositiveOrZero()) {
            allowanceAmount = allowanceAmount.multiply(-1.0);
        }
        
        if(!itemAllowances.getItemAllowances().isEmpty()) {
            MonetaryAmount allowance = itemAllowances.getItemAllowances().values().parallelStream()
                    .collect(() -> Money.of(BigDecimal.ONE, DataUtils.getInstance().getDefaultCurrencyUnit()), 
                            (a, t) -> t.add(a), 
                            (a, t) -> t.add(a));
            allowanceAmount.add(allowance);
        }
        MonetaryAmount totalAmount = Money.zero(DataUtils.getInstance().getDefaultCurrencyUnit());
        for (MonetaryAmount amt : netPricesPerVat.values()) {
            totalAmount = totalAmount.add(amt);
        }
        MonetaryAmount taxBasisTotalAmount = totalAmount.add(documentSummary.getShippingNet()).subtract(allowanceAmount);
        MonetaryAmount grandTotalAmount = taxBasisTotalAmount.add(documentSummary.getTotalVat());
        TradeSettlementHeaderMonetarySummationType retval = factory.createTradeSettlementHeaderMonetarySummationType()
                .withLineTotalAmount(createAmount(totalAmount))
                .withChargeTotalAmount(createAmount(documentSummary.getShippingNet()))
                .withAllowanceTotalAmount(createAmount(allowanceAmount))
                .withTaxBasisTotalAmount(createAmount(taxBasisTotalAmount))
                .withTaxTotalAmount(createAmount(documentSummary.getTotalVat(), 2, true))
                .withGrandTotalAmount(createAmount(grandTotalAmount))
                .withTotalPrepaidAmount(createAmount(Money.of(invoice.getPaidValue(), DataUtils.getInstance().getDefaultCurrencyUnit())))
                .withDuePayableAmount(
                      createAmount(grandTotalAmount
                              .subtract(Money.of(invoice.getPaidValue(), DataUtils.getInstance().getDefaultCurrencyUnit()))))
                ;
        return retval;
    }

    private TradeSettlementLineMonetarySummationType createTradeSettlementLineMonetarySummation(DocumentItem item) {
        /*
         * Der Gesamtpositionsbetrag ist der Nettobetrag unter Berücksichtigung von Zu- und Abschlägen ohne 
         * Angabe des Umsatzsteuerbetrages. 
         */
        Price price = new Price(item);
        TradeSettlementLineMonetarySummationType retval = factory.createTradeSettlementLineMonetarySummationType()
                .withLineTotalAmount(createAmount(price.getTotalNetRounded()));
        
        storeNetPrice(price.getVatPercent(), price.getTotalNetRounded());

        // alle anderen hier angebotenen Felder resultieren nur aus dem XSD, die sind in der Spezifikation
        // gar nicht aufgeführt (nur an anderer Stelle, wo sie sinnvoller sind)
        return retval;
    }

    /**
     * Stores the net price value per VAT for later use
     * @param vatPercent
     * @param totalNetRounded
     */
    private void storeNetPrice(String vatPercent, MonetaryAmount totalNetRounded) {
        MonetaryAmount newAmount = totalNetRounded;
        if(netPricesPerVat.get(vatPercent) != null) {
            newAmount = netPricesPerVat.get(vatPercent).add(totalNetRounded);
        }
        netPricesPerVat.put(vatPercent, newAmount);
    }

    /**
     * Detailinformationen zu Zahlungsbedingungen
     * 
     * @param invoice
     * @param documentSummary
     * @return
     */
    private TradePaymentTermsType createTradePaymentTerms(Document invoice, DocumentSummary documentSummary) {
        LocalDateTime dueDate = 
                DataUtils.getInstance().addToDate(invoice.getDocumentDate(), invoice.getDueDays());
        Placeholders placeholders = ContextInjectionFactory.make(Placeholders.class, eclipseContext);
        double percent = invoice.getPayment().getDiscountValue();
        
        Date out = Date.from(dueDate.atZone(ZoneId.systemDefault()).toInstant());
        Optional<String> paymentText = Optional.ofNullable(placeholders.createPaymentText(invoice, documentSummary, percent));
        TradePaymentTermsType tradePaymentTerms = factory.createTradePaymentTermsType()
            .withDescription(createText(paymentText.orElse("unknown")))
            .withDueDateDateTime(createDateTime(out));
        
        DocumentReceiver documentReceiver = addressManager.getBillingAdress(invoice);
/* GS/ use the value from documentReceiver as it might be overwritten for this invoice (or even no contact given / manual receiver entry)
        Contact contact = getOriginContact(documentReceiver);
        if (contact != null) {
    
            IDType id = StringUtils.isNotBlank(contact.getMandateReference())
                    ? factory.createIDType().withValue(contact.getMandateReference()).withSchemeID(
                            preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CREDITORID))
                    : null;
*/
            IDType id = StringUtils.isNotBlank(documentReceiver.getMandateReference())
                    ? factory.createIDType().withValue(documentReceiver.getMandateReference()).withSchemeID(
                            preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CREDITORID))
                    : null;
            tradePaymentTerms.setDirectDebitMandateID(id);
//        }
        return tradePaymentTerms;
    }

    private TradeAllowanceChargeType createTradeAllowance(DocumentItem item) {
        return createTradeAllowance(item, true);
    }

    /**
     * Detailinformationen zu Zu- und Abschlägen.
     * 
     * @param item
     * @return
     */
    private TradeAllowanceChargeType createTradeAllowance(DocumentItem item, boolean withReason) {
        Price price = new Price(item);
        MonetaryAmount amount = price.getTotalAllowance();
        boolean isAllowance = amount.isPositiveOrZero();
        if(!isAllowance) {
            amount = amount.multiply(-1);
        }
        
        itemAllowances.add(item.getItemVat(), price.getTotalAllowance());
        
        TradeAllowanceChargeType tradeAllowanceCharge = factory.createTradeAllowanceChargeType()
            .withChargeIndicator(factory.createIndicatorType().withIndicator(isAllowance))
//            .withCalculationPercent(factory.createPercentType().withValue(BigDecimal.valueOf(item.getItemRebate()))) // [CII-SR-122]
//            .withBasisAmount(createAmount(price.getTotalNet(), 2))  // [CII-SR-123]
            
            // Der gesamte zur Berechnung des Nettopreises vom Bruttopreis subtrahierte Rabatt
            // (Gilt nur, wenn der Rabatt je Einheit gegeben wird und nicht im Bruttopreis enthalten ist.)
            .withActualAmount(createAmount(amount, 2, false))
            // see UNTDID 5189 and UNTDID 7161
            ;
        if(withReason) {
            tradeAllowanceCharge.setReason(createText(zfMsg.zugferdExportLabelRebate));
//            tradeAllowanceCharge.setReasonCode(factory.createAllowanceChargeReasonCodeType().withValue("95"))  // "Discount" [CII-SR-127]
        }
        return tradeAllowanceCharge
//            .withCategoryTradeTax(createTradeTax(item.getItemVat()))
            ;
    }

    private TradeAllowanceChargeType createTradeAllowance(DocumentSummary summary, Document invoice) {
        MonetaryAmount amount = summary.getDiscountNet();
        // Abschlag ==> false
        // Zuschlag ==> true
        boolean isAllowance = amount.isPositive();
        if(!isAllowance) {
            amount = amount.multiply(-1);
        }
        return factory.createTradeAllowanceChargeType()
            .withChargeIndicator(factory.createIndicatorType().withIndicator(isAllowance))
            .withActualAmount(createAmount(amount, 2, false))
            .withBasisAmount(createAmount(summary.getItemsNet().add(amount), 2))
            .withReason(createText(zfMsg.zugferdExportLabelRebate))
            .withCategoryTradeTax(createTradeTax(invoice.getItems().get(0).getItemVat()))
            ;
    }
    
    /**
     * Generate allowance for shipping costs (this is for COMFORT profile only!)
     * @param invoice
     * @return
     */
    private TradeAllowanceChargeType createTradeAllowance(Document invoice) {
        Double amount = invoice.getShipping() != null ? invoice.getShipping().getShippingValue() : invoice.getShippingValue();
        
        TradeAllowanceChargeType retval = factory.createTradeAllowanceChargeType()
                .withChargeIndicator(factory.createIndicatorType().withIndicator(true))
                .withActualAmount(createAmount(Money.of(amount, DataUtils.getInstance().getDefaultCurrencyUnit()), 2, false))
                .withBasisAmount(createAmount(Money.of(invoice.getTotalValue(), DataUtils.getInstance().getDefaultCurrencyUnit()), 2, false))
                .withReason(createText("Shipping costs"));  // TODO Versandkosten!!!
     //   if(invoice.getShipping() != null && invoice.getShipping().getShippingVat().getTaxValue() > 0.0) {
            retval.setCategoryTradeTax(createTradeTax(invoice.getShipping().getShippingVat()));
      //  }
        return retval;
    }

    private TradeTaxType createTradeTax(VAT vatValue) {
        return createTradeTax(vatValue, vatValue.getTaxValue() > 0 ? TaxCategoryCodeContentType.S : TaxCategoryCodeContentType.Z);
    }
    
    private TradeTaxType createTradeTax(VAT vatValue, TaxCategoryCodeContentType taxCategoryCode) {
        return factory.createTradeTaxType()
                .withRateApplicablePercent(
                        factory.createPercentType().withValue(BigDecimal.valueOf(vatValue.getTaxValue()).multiply(BigDecimal.valueOf(100), new MathContext(2)).stripTrailingZeros()))
                .withCategoryCode(createTaxCategoryCode(taxCategoryCode))  // see UNTDID 5305
                .withTypeCode(createTaxTypeCode(TaxTypeCodeContentType.VAT))
                ;
    }

    /**
     * Detailangaben zu Steuern 
     * @param vatSummaryItem
     * @return
     */
    private TradeTaxType createTradeTax(VatSummaryItem vatSummaryItem) {
        // VAT description
        // (unused) String key = vatSummaryItem.getVatName();
        // It's the VAT value
        MonetaryAmount basisAmount = Optional.ofNullable(vatSummaryItem.getNet()).orElse(Money.zero(DataUtils.getInstance().getDefaultCurrencyUnit()));
        TaxCategoryCodeContentType taxType = vatSummaryItem.getVatPercent() == 0 ? TaxCategoryCodeContentType.Z : TaxCategoryCodeContentType.S;
        TradeTaxType retval = factory.createTradeTaxType()
                .withCalculatedAmount(createAmount(basisAmount.multiply(vatSummaryItem.getVatPercent())))
                .withRateApplicablePercent(
                        factory.createPercentType().withValue(BigDecimal.valueOf(vatSummaryItem.getVatPercent()).multiply(BigDecimal.valueOf(100), new MathContext(2)).stripTrailingZeros()))
                .withBasisAmount(createAmount(basisAmount))
                .withCategoryCode(createTaxCategoryCode(taxType)) 
                //.withExemptionReason(TODO)
//                .withTaxPointDate(value)
//                .withDueDateTypeCode(value)
                .withTypeCode(createTaxTypeCode(TaxTypeCodeContentType.VAT))
                ; 
        return retval;
    }

    private TaxTypeCodeType createTaxTypeCode(TaxTypeCodeContentType string) {
        return factory.createTaxTypeCodeType()
                .withValue(string);
    }

    private TaxCategoryCodeType createTaxCategoryCode(TaxCategoryCodeContentType taxCat) {
        return factory.createTaxCategoryCodeType()
                .withValue(taxCat);
    }

    /**
     * creates an Amount field
     * @param the VAT value
     * @return
     */
    private AmountType createAmount(MonetaryAmount amount) {
        return createAmount(amount, 2, false);
    }
    
    private AmountType createAmount(MonetaryAmount amount, int scale) {
        return createAmount(amount, scale, false);
    }

    /**
     * Creates an Amount with given value, scale and currency.
     * @param amount
     * @param scale
     * @param withCurrency
     * @return
     */
    private AmountType createAmount(MonetaryAmount amount, int scale, boolean withCurrency) {
        BigDecimal scaledValue = BigDecimal.valueOf(amount.getNumber().doubleValue()).setScale(scale, RoundingMode.HALF_UP);
        
        return factory.createAmountType()
                .withValue(scaledValue)
        .withCurrencyID(withCurrency ? amount.getCurrency().getCurrencyCode() : null);      
    }

    private CreditorFinancialInstitutionType createCreditorFinancialInstitution() {
        return factory.createCreditorFinancialInstitutionType()
                .withBICID(createIdFromString(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_BIC)))
//              .withGermanBankleitzahlID(createIdFromString(preferences.getString("YOURCOMPANY_COMPANY_BANKCODE")))
        /*.withName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_BANK)))*/;
    }

    private CreditorFinancialAccountType createCreditorAccount() {
        CreditorFinancialAccountType retval = null;
        if(!StringUtils.isEmpty(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_IBAN)) 
                || !StringUtils.isEmpty(preferences.getString("YOURCOMPANY_COMPANY_BANKACCOUNTNR"))) {
            retval = factory.createCreditorFinancialAccountType()
                    .withIBANID(createIdFromString(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_IBAN)))/*
                    .withProprietaryID(createIdFromString(preferences.getString("YOURCOMPANY_COMPANY_BANKACCOUNTNR")))*/;
        }
        return retval;
    }

    private DebtorFinancialAccountType createDebtorAccount(BankAccount bankAccount) {
        if(bankAccount != null 
                && !StringUtils.isEmpty(bankAccount.getIban())) {
            return factory.createDebtorFinancialAccountType()
                    .withIBANID(createIdFromString(bankAccount.getIban()))
//                  .withProprietaryID(createIdFromString(invoice.getFormatedStringValueByKeyFromOtherTable("addressid.CONTACTS:account")))
                    ;
        } else return null;
    }

    private PaymentMeansCodeType createPaymentTypeCode(Document invoice) {
        // TODO implement lookup:  UNTDID 4461 !!!
        
/*
* Payment type code gem. "Payment Means Code" lt. Codeliste ZUGFeRD
* 10 ... bar
* 31 ... Payment by debit movement of funds from one account to another.  International Transfers
*        ==> SEPA-Überweisung!
* 48 ... Bank card
* 49 ... Direct debit (Lastschrift)
*                   
*/
        return factory.createPaymentMeansCodeType().withValue("31");
    }

    private CurrencyCodeType createCurrencyCode(CurrencyCodeContentType currency) {
        return factory.createCurrencyCodeType().withValue(currency);
    }

    private TaxRegistrationType createTaxNumber(Document invoice, ContactType contactType) {
        TaxRegistrationType retval = null;
        switch (contactType) {
        case SELLER:
            String companyVatNo = preferences.getString(Constants.PREFERENCES_YOURCOMPANY_VATNR);
            if(!StringUtils.isEmpty(companyVatNo)) {
                retval = factory.createTaxRegistrationType()
                        .withID(createIdWithSchemeFromString(getNullCheckedValue(companyVatNo), "VA"));
            }
            break;
        case BUYER:
            DocumentReceiver billingAddress = addressManager.getBillingAdress(invoice);
            if(!StringUtils.isEmpty(billingAddress.getVatNumber())) {
                retval = factory.createTaxRegistrationType()
                        .withID(createIdWithSchemeFromString(getNullCheckedValue(billingAddress.getVatNumber()), "VA"));
            }
            break;
        default:
            break;
        }
        return retval;
    }

    /**
     * Returns the given value only if it is not <code>null</code> or not empty. This is to prevent
     * empty XML tags.
     *  
     * @param string string to test
     * @return <code>null</code> if the given string is empty or <code>null</code>
     */
    private String getNullCheckedValue(String formatedStringValueByKeyFromOtherTable) {
        return StringUtils.isEmpty(formatedStringValueByKeyFromOtherTable) ? null : formatedStringValueByKeyFromOtherTable;
    }

    private CodeType createCode(String value) {
        return factory.createCodeType().withValue(value);
    }
    
    private TradeAddressType createAddress(Document invoice, ContactType contactType) {
        TradeAddressType retval = null;
        switch (contactType) {
        case SELLER:
            String countryCode = preferences.getString(Constants.PREFERENCES_YOURCOMPANY_COUNTRY);

            retval = factory.createTradeAddressType()
                .withPostcodeCode(createCode(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_ZIP)))
                .withLineOne(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_STREET)))
    //      .withLineTwo(is empty at the moment);
    //      .withLineThree(is empty at the moment);
                .withCityName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CITY)))
                .withCountryID(createCountry(countryCode))  // Nur die Alpha-2 Darstellung darf verwendet werden
//                .withCountrySubDivisionName(null)
                ;
            break;
        case BUYER:
            DocumentReceiver billingAddress = addressManager.getBillingAdress(invoice);
            // attention! Mind the manualAddress!
// GS/ parsing w/o respecting the configurable pattern does not make much sense
//            if (billingAddress.getManualAddress() == null) {
            if (true || billingAddress.getManualAddress() == null) {
                retval = factory.createTradeAddressType()     //
                        .withPostcodeCode(createCode(billingAddress.getZip()))  //
                        .withLineOne(createText(billingAddress.getStreet()))    //
                        //      .withLineTwo(is empty at the moment)
                        //      .withLineThree(is empty at the moment);
                        .withCityName(createText(billingAddress.getCity()))     //
                        .withCountryID(createCountry(billingAddress.getCountryCode())) // Nur die Alpha-2 Darstellung darf verwendet werden
                //              .withCountrySubDivisionName(null)
            ;

            } else {
                Address addressFromString = contactUtil.createAddressFromString(billingAddress.getManualAddress());
                retval = factory.createTradeAddressType()     //
                        .withPostcodeCode(createCode(addressFromString.getZip()))  //
                        .withLineOne(createText(addressFromString.getStreet()))    //
                        //      .withLineTwo(is empty at the moment)
                        //      .withLineThree(is empty at the moment);
                        .withCityName(createText(addressFromString.getCity()))     //
                        .withCountryID(createCountry(addressFromString.getCountryCode())) // Nur die Alpha-2 Darstellung darf verwendet werden
                //              .withCountrySubDivisionName(null)
            ;
            }

            break;
        default:
            break;
        }
        return retval;
    }

    private CountryIDType createCountry(final String value) {
        String countryStr = null;
        // FIXME CHANGE THIS!!!
        if(StringUtils.length(value) > 2) {
            countryStr = localeUtil.findCodeByDisplayCountry(value, "DE");
        }
        // null values aren't allowed!
        return factory.createCountryIDType().withValue(Optional.ofNullable(countryStr).orElse("DE"));
    }

    private TradeContactType createContact(Document invoice, ContactType contactType) {
        TradeContactType contact = factory.createTradeContactType();
        switch (contactType) {
        case SELLER:
            contact.setPersonName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_OWNER)));
            //      contact.setDepartmentName(createText(dept));  // unknown (Kontaktstelle des Verkäufers)
            
            if(StringUtils.isNotBlank(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TEL))) {
                contact.setTelephoneUniversalCommunication((createCommunicationItem(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TEL))));
            }
            
            UniversalCommunicationType email = factory.createUniversalCommunicationType()
                    .withURIID(createIdFromString(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_EMAIL)));

            contact.setEmailURIUniversalCommunication(email);
            break;
        case BUYER:
            
            DocumentReceiver billingAdress = addressManager.getBillingAdress(invoice);
            if (billingAdress != null) {
                email = factory.createUniversalCommunicationType()
                        .withURIID(createIdWithSchemeFromString(billingAdress.getEmail(), "EM"));

                contact.setPersonName(createText(invoice.getAddressFirstLine()));
                //            contact.setDepartmentName(createText(dept));  // unknown
                contact.setTelephoneUniversalCommunication(createCommunicationItem(billingAdress.getPhone()));
                contact.setEmailURIUniversalCommunication(email);
            }
            break;
        default:
            break;
        }
        return contact;
    }
    
  private UniversalCommunicationType createCommunicationItem(String communicationItem) {
      return factory.createUniversalCommunicationType()
              .withCompleteNumber(createText(communicationItem));
  }
    
    /**
     * Creates a {@link FormattedDateTimeType} from a given date string ("YYYY-MM-DD").
     * 
     * @param dateString the date String
     * @return {@link FormattedDateTimeType}
     */
    private DateTimeType createDateTime(final Date dateString) {
        DateTimeType dateValue = null;
        if(dateString != null) {
            dateValue = factory.createDateTimeType();
            DateTimeType.DateTimeString dateTypeString = factory.createDateTimeTypeDateTimeString();
            dateTypeString.setValue(sdfDest.format(dateString));
            dateTypeString.setFormat("102");
            dateValue.setDateTimeString(dateTypeString);
        }
        return dateValue;
    }
    /**
     * Creates a {@link FormattedDateTimeType} from a given date string ("YYYY-MM-DD").
     * 
     * @param dateString the date String
     * @return {@link FormattedDateTimeType}
     */
    private FormattedDateTimeType createFormattedDateTime(final Date dateString) {
        FormattedDateTimeType dateValue = null;
        if(dateString != null) {
            dateValue = factory.createFormattedDateTimeType();
            DateTimeString dateTypeString = factory.createFormattedDateTimeTypeDateTimeString();
            dateTypeString.setValue(sdfDest.format(dateString));
            dateTypeString.setFormat("102");
            dateValue.setDateTimeString(dateTypeString);
        }
        return dateValue;
    }

    /**
     * @param invoice
     * @return
     */
    private TextType createText(String text) {
        TextType retval = null;
        if(!StringUtils.isBlank(text)) {
            retval = factory.createTextType().withValue(text);
        }
        return retval;
    }
    
    private IDType createIdFromString(String idString) {
        return createIdWithSchemeFromString(idString, null);
    }

    private IDType createIdWithSchemeFromString(String idString, String scheme) {
        return idString != null ? factory.createIDType().withValue(idString).withSchemeID(scheme) : null;
    }


}
