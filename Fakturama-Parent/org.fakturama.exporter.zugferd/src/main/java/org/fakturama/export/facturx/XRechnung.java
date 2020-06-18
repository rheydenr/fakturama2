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
import com.sebulli.fakturama.model.BankAccount;
import com.sebulli.fakturama.model.CEFACTCode;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.office.Placeholders;
import com.sebulli.fakturama.util.DocumentTypeUtil;

/**
 * Create an XRechnung XML.
 */
public class XRechnung extends AbstractEInvoice {

    @Override
    public CrossIndustryInvoice getInvoiceXml(Optional<Document> invoiceDoc) {
        if(!invoiceDoc.isPresent()) {
            return null;
        }
        factory = new ObjectFactory();

        Document invoice = invoiceDoc.get();
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
        /*                .withBusinessProcessSpecifiedDocumentContextParameter(
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
            MessageDialog.openWarning(shell, "Warning", "Your company information is empty. Please specify it "
                    + "in preferences. The generated file could be not valid.");
            owner = "(unknown)";
        }

        note.setContent(createText(owner));  // should only be free text for information about the invoice
        note.setSubjectCode(createCode("REG")); // see UNTDID 4451, explains the note content
        exchangedDocumentType.getIncludedNote().add(note);
        root.setExchangedDocument(exchangedDocumentType);
        
        // now follows the huge part for trade transaction
        SupplyChainTradeTransactionType tradeTransaction = factory.createSupplyChainTradeTransactionType();
        HeaderTradeAgreementType tradeAgreement = factory.createHeaderTradeAgreementType()
                .withBuyerReference(createText(invoice.getCustomerRef()));  // "Kundenreferenz"
        tradeTransaction.setApplicableHeaderTradeAgreement(tradeAgreement);
        
        // create seller information
        TradePartyType seller = factory.createTradePartyType()
              .withID(createIdFromString(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TAXNR)))
              .withName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME)))
//              .withDescription(createText(""))
              .withSpecifiedLegalOrganization(createLegalOrganizationType(invoice))
              .withDefinedTradeContact(createContact(invoice, ContactType.SELLER))
                .withPostalTradeAddress(createAddress(invoice, ContactType.SELLER))
//                .withGlobalID(createIdWithSchemeFromString("EAN", preferences.getString(Constants.PREFERENCES_YOURCOMPANY_GLN)))  // according to ISO 6523
                .withSpecifiedTaxRegistration(createTaxNumber(invoice, ContactType.SELLER))
                ;
        tradeAgreement.setSellerTradeParty(seller);
        
        // create buyer information
        TradePartyType buyer = factory.createTradePartyType()
                .withID(createIdFromString(addressManager.getBillingAdress(invoice).getCustomerNumber()))
//              .withGlobalID(createIdWithSchemeFromString("EAN|BIC or whatever", 
//invoice.getReceiver().get(0).getOriginContactId()))
                .withName(createText(invoice.getAddressFirstLine()))
//                .withSpecifiedLegalOrganization(createLegalOrganizationType(invoice))
//                .withDefinedTradeContact(value)
                .withPostalTradeAddress(createAddress(invoice, ContactType.BUYER))
                ;
        tradeAgreement.setBuyerTradeParty(buyer);
        
//        tradeAgreement.setSellerTaxRepresentativeTradeParty(value);
//        tradeAgreement.setSellerOrderReferencedDocument(value);
        
        // referenced order
        Transaction transaction = ContextInjectionFactory.make(Transaction.class, eclipseContext).of(invoice);
        if(transaction != null) {
            ReferencedDocumentType orderRef = factory.createReferencedDocumentType()
                    .withIssuerAssignedID(createIdFromString(transaction.getReference(DocumentType.ORDER)));
            // only if ID is not empty!
            if(!StringUtils.isEmpty(transaction.getReference(DocumentType.ORDER))) {
                tradeAgreement.setBuyerOrderReferencedDocument(orderRef);
            }
        }

        // there is no contract information in Fakturama!
          ReferencedDocumentType contractRef = factory.createReferencedDocumentType()
                  .withIssuerAssignedID(createIdFromString("contractNumber"));
//          tradeAgreement.setContractReferencedDocument(contractRef);
            
          ReferencedDocumentType additionalReferencedDocument = factory.createReferencedDocumentType()
                  .withIssuerAssignedID(createIdFromString("contractNumber"))
                  .withURIID(createIdFromString("URI"))
                  .withTypeCode(factory.createDocumentCodeType().withValue("130"))  // UNTDID 1001
                  .withName(createText("AttachmentName"))
                  .withAttachmentBinaryObject(factory.createBinaryObjectType()
                          .withFilename("filename").withMimeCode("mime").withValue(new byte[] {}))
                  ;
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
                .withPaymentReference(createText(invoice.getName())) /* customerref ? */
//                .withTaxCurrencyCode(createCurrencyCode(currency))  // see ISO 4217
                .withInvoiceCurrencyCode(createCurrencyCode(currency))
                ;

        // TODO tradeSettlement.setPayeeTradeParty(value);   // Zahlungsempfänger
        
        DocumentReceiver documentReceiver = addressManager.getBillingAdress(invoice);
        Contact contact = getOriginContact(documentReceiver);
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
        // Abschläge / Zuschläge nur aufführen wenn sie auch tatsächlich angefallen sind! Hier kommen auch die Versandkosten mit rein 
        // (die sind nur bei EXTENDED in einem extra Node)
        tradeSettlement.getSpecifiedTradeAllowanceCharge().add(createTradeAllowance(documentSummary));
        if(invoice.getShippingValue() > 0) {
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
        
        if(item.getItemRebate() != null) {
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
                .withBasisQuantity(createQuantity(1d, qunit))
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
        MonetaryAmount allowanceAmount = documentSummary.getDiscountGross();
        boolean isAllowance = allowanceAmount.isPositiveOrZero();
        if(!isAllowance) {
            allowanceAmount = allowanceAmount.multiply(-1.0);
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
        Contact contact = getOriginContact(documentReceiver);
        if (contact != null) {
    
            IDType id = StringUtils.isNotBlank(contact.getMandateReference())
                    ? factory.createIDType().withValue(contact.getMandateReference()).withSchemeID(
                            preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CREDITORID))
                    : null;
            tradePaymentTerms.setDirectDebitMandateID(id);
        }
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
        Double discountPercent = item.getItemRebate();
//        Double amount = item.getPrice() * discountPercent;
        Price price = new Price(item);
        Double amount = price.getTotalGross().multiply(discountPercent).getNumber().doubleValue();
//        double factor = Math.pow(10, DEFAULT_AMOUNT_SCALE);
//        double s = Math.round(price.getUnitNet().multiply(factor).getNumber().doubleValue()) / factor;
//        double t = Math.round(price.getUnitNetDiscounted().multiply(factor).getNumber().doubleValue()) / factor;
//        double u = Math.round((s-t) * factor) / factor;
        boolean isAllowance = amount > 0;
        if(!isAllowance) {
            amount *= -1;
        }
        
        
        TradeAllowanceChargeType tradeAllowanceCharge = factory.createTradeAllowanceChargeType()
            .withChargeIndicator(factory.createIndicatorType().withIndicator(isAllowance))
//            .withCalculationPercent(factory.createPercentType().withValue(BigDecimal.valueOf(item.getItemRebate()))) // [CII-SR-122]
//            .withBasisAmount(createAmount(price.getTotalNet(), 2))  // [CII-SR-123]
            .withActualAmount(createAmount(Money.of(amount, DataUtils.getInstance().getDefaultCurrencyUnit()), 2, false))
            // see UNTDID 5189 and UNTDID 7161
            ;
        if(withReason) {
            tradeAllowanceCharge.setReason(createText(msg.zugferdExportLabelRebate));
//            tradeAllowanceCharge.setReasonCode(factory.createAllowanceChargeReasonCodeType().withValue("95"))  // "Discount" [CII-SR-127]
        }
        return tradeAllowanceCharge
//            .withCategoryTradeTax(createTradeTax(item.getItemVat()))
            ;
    }

    private TradeAllowanceChargeType createTradeAllowance(DocumentSummary summary) {
        MonetaryAmount amount = summary.getDiscountGross();
        // Abschlag ==> false
        // Zuschlag ==> true
        boolean isAllowance = amount.isPositive();
        if(!isAllowance) {
            amount = amount.multiply(-1);
        }
        return factory.createTradeAllowanceChargeType()
            .withChargeIndicator(factory.createIndicatorType().withIndicator(isAllowance))
            .withActualAmount(createAmount(amount, 2, false))
            .withBasisAmount(createAmount(summary.getItemsNet(), 2))
            .withReason(createText(msg.zugferdExportLabelRebate))  // TODO Versandkosten!!!
            .withCategoryTradeTax(createTradeTax(summary.getTotalVat().getNumber().doubleValue()));
    }
    
    /**
     * Generate allowance for shipping costs (this is fo COMFORT profile only!)
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
            retval.setCategoryTradeTax(createTradeTax(invoice.getShipping().getShippingVat(), TaxCategoryCodeContentType.Z));
      //  }
        return retval;
    }

    private TradeTaxType createTradeTax(VAT vatValue) {
        return createTradeTax(vatValue, TaxCategoryCodeContentType.S);
    }
    
    private TradeTaxType createTradeTax(VAT vatValue, TaxCategoryCodeContentType taxCategoryCode) {
        return factory.createTradeTaxType()
                .withRateApplicablePercent(
                        factory.createPercentType().withValue(BigDecimal.valueOf(vatValue.getTaxValue()).multiply(BigDecimal.valueOf(100)).round(new MathContext(2))))
                .withCategoryCode(createTaxCategoryCode(taxCategoryCode))  // see UNTDID 5305
                .withTypeCode(createTaxTypeCode(TaxTypeCodeContentType.VAT))
                ;
    }

    private TradeTaxType createTradeTax(VatSummaryItem vatSummaryItem) {
        // VAT description
        // (unused) String key = vatSummaryItem.getVatName();
        // It's the VAT value
        MonetaryAmount basisAmount = Optional.ofNullable(netPricesPerVat.get(numberFormatterService.DoubleToFormatedPercent(vatSummaryItem.getVatPercent()))).orElse(Money.zero(DataUtils.getInstance().getDefaultCurrencyUnit()));
        TaxCategoryCodeContentType taxType = vatSummaryItem.getVatPercent() == 0 ? TaxCategoryCodeContentType.Z : TaxCategoryCodeContentType.S;
        TradeTaxType retval = factory.createTradeTaxType()
                .withCalculatedAmount(createAmount(basisAmount.multiply(vatSummaryItem.getVatPercent())))
                .withRateApplicablePercent(factory.createPercentType().withValue(BigDecimal.valueOf( DataUtils.getInstance().round(vatSummaryItem.getVatPercent() * 100))))
                .withBasisAmount(createAmount(basisAmount))
                .withCategoryCode(createTaxCategoryCode(taxType)) 
                //.withExemptionReason(TODO)
//                .withTaxPointDate(value)
//                .withDueDateTypeCode(value)
                .withTypeCode(createTaxTypeCode(TaxTypeCodeContentType.VAT))
                ; 
        return retval;
    }

    /**
     * Detailangaben zu Steuern 
     * @param vatPercent
     * @return
     */
    private TradeTaxType createTradeTax(Double vatPercent) {
        TradeTaxType retval = factory.createTradeTaxType()
                .withRateApplicablePercent(factory.createPercentType()
                .withValue(BigDecimal.valueOf(vatPercent * 100.0)))
                .withCategoryCode(createTaxCategoryCode(TaxCategoryCodeContentType.S)) // Standard rate, FIXME for other uses!
                //.withExemptionReason(TODO)
                .withTypeCode(createTaxTypeCode(TaxTypeCodeContentType.VAT));
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

    private AmountType createAmount(Double price, int scale) {
        return factory.createAmountType()
                .withValue(BigDecimal.valueOf(price).setScale(scale, RoundingMode.HALF_UP));      
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
                .withCityName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CITY)))
                .withCountryID(createCountry(countryCode));
            break;
        case BUYER:
            DocumentReceiver billingAddress = addressManager.getBillingAdress(invoice);
            // Korrektur
            countryCode = billingAddress.getCountryCode();
            retval = factory.createTradeAddressType()
                .withPostcodeCode(createCode(billingAddress.getZip()))
                .withLineOne(createText(billingAddress.getStreet()))
    //      .withLineTwo(is empty at the moment)
                .withCityName(createText(billingAddress.getCity()))
                .withCountryID(createCountry(countryCode));
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
            // this is only EXTENDED profile!
            contact.setPersonName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_OWNER))); // YOURCOMPANY.OWNER
            //      contact.setDepartmentName(createText(dept));  // unknown
            
            if(StringUtils.isNotBlank(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TEL))) {
                contact.setTelephoneUniversalCommunication((createCommunicationItem(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_TEL))));
            }
            //          contact.getFaxUniversalCommunication().add(createCommunicationItem(preferences.getString("YOURCOMPANY_COMPANY_FAX")));

            UniversalCommunicationType email = factory.createUniversalCommunicationType()
                    .withURIID(createIdFromString(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_EMAIL)));

            contact.setEmailURIUniversalCommunication(email);
            break;
        case BUYER:
            // "consultant"???
            //          contact.setPersonName(createText(invoice.getFormatedStringValueByKey("addressfirstline")));
            //            contact.setDepartmentName(createText(dept));  // unknown
            //          contact.getTelephoneUniversalCommunication().add(createCommunicationItem(invoice.getFormatedStringValueByKeyFromOtherTable("addressid.CONTACTS:phone")));
            //          contact.setEmailURIUniversalCommunication(createCommunicationItem(invoice.getFormatedStringValueByKeyFromOtherTable("addressid.CONTACTS:email")));
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
