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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.MonetaryAmount;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpParsingException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.fakturama.export.zugferd.modelgen.AmountType;
import org.fakturama.export.zugferd.modelgen.CodeType;
import org.fakturama.export.zugferd.modelgen.CountryIDType;
import org.fakturama.export.zugferd.modelgen.CreditorFinancialAccountType;
import org.fakturama.export.zugferd.modelgen.CreditorFinancialInstitutionType;
import org.fakturama.export.zugferd.modelgen.CrossIndustryDocument;
import org.fakturama.export.zugferd.modelgen.DateTimeType;
import org.fakturama.export.zugferd.modelgen.DateTimeType.DateTimeString;
import org.fakturama.export.zugferd.modelgen.DebtorFinancialAccountType;
import org.fakturama.export.zugferd.modelgen.DebtorFinancialInstitutionType;
import org.fakturama.export.zugferd.modelgen.DocumentCodeType;
import org.fakturama.export.zugferd.modelgen.DocumentContextParameterType;
import org.fakturama.export.zugferd.modelgen.DocumentLineDocumentType;
import org.fakturama.export.zugferd.modelgen.ExchangedDocumentContextType;
import org.fakturama.export.zugferd.modelgen.ExchangedDocumentType;
import org.fakturama.export.zugferd.modelgen.IDType;
import org.fakturama.export.zugferd.modelgen.LogisticsServiceChargeType;
import org.fakturama.export.zugferd.modelgen.NoteType;
import org.fakturama.export.zugferd.modelgen.ObjectFactory;
import org.fakturama.export.zugferd.modelgen.PaymentMeansCodeType;
import org.fakturama.export.zugferd.modelgen.QuantityType;
import org.fakturama.export.zugferd.modelgen.ReferencedDocumentType;
import org.fakturama.export.zugferd.modelgen.SupplyChainEventType;
import org.fakturama.export.zugferd.modelgen.SupplyChainTradeAgreementType;
import org.fakturama.export.zugferd.modelgen.SupplyChainTradeDeliveryType;
import org.fakturama.export.zugferd.modelgen.SupplyChainTradeLineItemType;
import org.fakturama.export.zugferd.modelgen.SupplyChainTradeSettlementType;
import org.fakturama.export.zugferd.modelgen.SupplyChainTradeTransactionType;
import org.fakturama.export.zugferd.modelgen.TaxCategoryCodeType;
import org.fakturama.export.zugferd.modelgen.TaxRegistrationType;
import org.fakturama.export.zugferd.modelgen.TaxTypeCodeType;
import org.fakturama.export.zugferd.modelgen.TextType;
import org.fakturama.export.zugferd.modelgen.TradeAddressType;
import org.fakturama.export.zugferd.modelgen.TradeAllowanceChargeType;
import org.fakturama.export.zugferd.modelgen.TradePartyType;
import org.fakturama.export.zugferd.modelgen.TradePaymentTermsType;
import org.fakturama.export.zugferd.modelgen.TradePriceType;
import org.fakturama.export.zugferd.modelgen.TradeProductType;
import org.fakturama.export.zugferd.modelgen.TradeSettlementMonetarySummationType;
import org.fakturama.export.zugferd.modelgen.TradeSettlementPaymentMeansType;
import org.fakturama.export.zugferd.modelgen.TradeTaxType;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.dao.CEFACTCodeDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.dto.Transaction;
import com.sebulli.fakturama.dto.VatSummaryItem;
import com.sebulli.fakturama.dto.VatSummarySetManager;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.CEFACTCode;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.office.Placeholders;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.util.DocumentTypeUtil;

/**
 * This is the main class for the exporter interface for the ZUGFeRD invoice. At the
 * moment, a COMFORT level ZUGFeRD document is generated. This will be changed
 * in future (more flexible). 
 * 
 * This code has many TODOs because the ZUGFeRD specification wasn't final. Furthermore,
 * the invoice document doesn't have all the needed fields (esp. for EXTENDED profile). 
 * 
 */
public class ZugferdExporter {

	@Inject
	@Translation
	protected ZFMessages msg;
    
    @Inject
    private IPreferenceStore preferences;
    
    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;

    @Inject
    private IEclipseContext eclipseContext;
    
    @Inject
    protected ILogger log;

    @Inject
	private CEFACTCodeDAO measureUnits;
    
    @Inject
    private ESelectionService selectionService;
    
    private Shell shell;
	
	/** The Constant DEFAULT_PRICE_SCALE. */
    private static final int DEFAULT_AMOUNT_SCALE = 4;

	public static final String CMD_EXPORT_ZUGFERD = "com.sebulli.fakturama.actions.exportZUGFeRD";

	private static SimpleDateFormat sdfDest = new SimpleDateFormat("yyyyMMdd");
	private ObjectFactory factory;
	private String workspace;

	private Map<String, MonetaryAmount> netPricesPerVat = new HashMap<>();;

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
	
	/**
	 * Constructor
	 */
	@PostConstruct
	public void initializeZugferdExporter() {

//		super(ACTIONTEXT);
//		ZFDefaultValuesInitializer make = ContextInjectionFactory.make(ZFDefaultValuesInitializer.class, eclipseContext);
//		make.initializeDefaultPreferences();
		factory = new ObjectFactory();

		//T: Tool Tip Text
//		setToolTipText(_("Export an invoice to a ZUGFeRD PDF"));
//
//		// The id is used to refer to the action in a menu or toolbar
//		setId(ICommandIds.CMD_EXPORT_ZUGFERD);
//
//		// Associate the action with a pre-defined command, to allow key
//		// bindings.
//		setActionDefinitionId(ICommandIds.CMD_EXPORT_ZUGFERD);
//
//		// sets a default 16x16 pixel icon.
//		setImageDescriptor(com.sebulli.fakturama.Activator.getImageDescriptor("/icons/16/shop_16.png"));
		if(preferences != null) {
			workspace = preferences.getString(Constants.GENERAL_WORKSPACE);
		} else {
			// this only happens at first run (from installation phase) and shouldn't be a problem
			workspace = "NO_WORKSPACE_SELECTED";
		}
	}

	/**
	 * At the moment we support only the COMFORT profile.
	 * 
	 */
	@Execute
    public Object execute(Shell shell, IEclipseContext context, EPartService partService) {
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
		this.shell = shell;
		Document invoice = findSelectedInvoice(partService);
		if(invoice != null) {
			// 1. check if PDF file exists
			// (neu erzeugte PDFs sind automatisch PDF/A-1
			//  siehe OfficeDocument#saveOODocument())
		    if(StringUtils.isEmpty(invoice.getPdfPath())) {
		    	MessageDialog.openError(shell, msg.zugferdExportCommandTitle, msg.zugferdExportErrorNosource);
		    	return null;
		    }
		    
		    netPricesPerVat.clear();
		    
		    if(!(new File(invoice.getPdfPath()).exists())) {
		    	MessageDialog.openError(shell, msg.zugferdExportCommandTitle, 
		    			MessageFormat.format(msg.zugferdExportErrorWrongpath, invoice.getPdfPath()));
		    	return null;
		    }
			
			// 2. create XML file
			CrossIndustryDocument root = createInvoiceFromDataset(invoice);

//			testOutput(root);
			
			// 3. merge XML & PDF/A-1 to PDF/A-3
			boolean result = createPdf(invoice, root);
			if(result) {
				// Display an info message
				MessageDialog.openInformation(shell, msg.zugferdExportCommandTitle, msg.zugferdExportInfoSuccessfully);
			} else {
				// Display an info message
				MessageDialog.openError(shell, msg.zugferdExportCommandTitle, msg.zugferdExportErrorCancelled);
			}
		} else {
			// Display a warning message
			MessageDialog.openWarning(shell, msg.zugferdExportCommandTitle, msg.zugferdExportWarningChooseinvoice);
		}
		return null;
	}
	
	/**
	 * tests the generated export file (only for debugging purposes)  
	 * 
	 * @param root the document 
	 */
	@SuppressWarnings("unused")
	private void testOutput(CrossIndustryDocument root) {
		/* * * * * * TEST ONLY!!! * * * * * */
		Path path = Paths.get("d:\\temp\\ZUGTEST.XML");
		try(BufferedWriter newBufferedWriter = Files.newBufferedWriter(path, Charset.defaultCharset(), StandardOpenOption.CREATE);) {
			
			DOMResult res = new DOMResult();
			JAXBContext testContext = JAXBContext.newInstance(root.getClass());
			testContext.createMarshaller().marshal(root, res);
			org.w3c.dom.Document doc = (org.w3c.dom.Document) res.getNode();
			printDocument(doc, new StreamResult(newBufferedWriter));
		}
		catch (JAXBException | IOException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* * * * * *  END TEST ONLY!!! * * * * * */
		
		
	}

	/**
	 * Erzeugt aus einem bereits gedruckten PDF-Dokument (PDF/A-1) und einem
	 * XML-Eingabestream eine ZUGFeRD-Datei (PDF/A-3).
	 * 
	 * @param invoice
	 * @param root
	 */
	private boolean createPdf(Document invoice, CrossIndustryDocument root) {
		boolean retval = true;
		ConformanceLevel level = ConformanceLevel.COMFORT;
		String pdfFile = invoice.getPdfPath();
		PDDocument pdfa3 = null;
		try (ByteArrayOutputStream buffo = new ByteArrayOutputStream()) {
			// create XML from structure
			DOMResult res = new DOMResult();
			JAXBContext context = JAXBContext.newInstance(root.getClass());
			context.createMarshaller().marshal(root, res);
			org.w3c.dom.Document zugferdXml = (org.w3c.dom.Document) res.getNode();
			printDocument(zugferdXml, buffo);

			PDDocument retvalPDFA3 = ZugferdHelper.makeA3Acompliant(pdfFile, level, zugferdXml, invoice.getName());

			// embed XML
			pdfa3 = ZugferdHelper.attachZugferdFile(retvalPDFA3, buffo.toByteArray());
			
			String fileSelected = eclipsePrefs.get(ZFConstants.PREFERENCES_ZUGFERD_PATH, "");
			
			// extract filename for further use
			// unify path name so that we can extract a filename
			int lastIndexOfPathSeparator = pdfFile.replaceAll("\\\\", "/").lastIndexOf('/');
			String fileName = ""; // filename w/o separator
			if(lastIndexOfPathSeparator > -1) { // found! (else no separator was found)
				fileName = pdfFile.substring(lastIndexOfPathSeparator + 1);
			}
			if(StringUtils.isBlank(fileSelected)) {
				// store file
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setFilterExtensions(new String[] { "*.pdf", "*.*" });
				dialog.setFilterPath(workspace); 
				dialog.setOverwrite(true); 
				dialog.setFileName("ZF-" + fileName);
				dialog.setFilterNames(new String[] { "PDF/A-3 File (ZUGFeRD)", "All Files" });
				fileSelected = dialog.open();
			}
			if (fileSelected != null) {
				pdfa3.save(fileSelected);
				//	Files.write(outFile, pdfa3, StandardOpenOption.CREATE);
			} else {  // dialog cancelled
				retval = false;
			}
		}
		catch (JAXBException | IOException | TransformerException | BadFieldValueException | XmpParsingException | XPathExpressionException e) {
			log.error(e, "creating ZUGFeRD document: " + e.getMessage());
			retval = false;
		} finally {
			if(pdfa3 != null) {
				try {
	                pdfa3.close();
                }
                catch (IOException ioex) {
        			log.error(ioex, "error closing ZUGFeRD PDF document: " + ioex.getMessage());
                }
			}
		}
		return retval;
	}

	private CrossIndustryDocument createInvoiceFromDataset(Document invoice) {
		// Recalculate the sum of the document before exporting
		DocumentSummaryCalculator documentSummaryCalculator = new DocumentSummaryCalculator();
	    DocumentSummary documentSummary = documentSummaryCalculator.calculate(invoice);

		CrossIndustryDocument root = factory.createCrossIndustryDocument();
		// at first create a reasonable context
		ExchangedDocumentContextType exchangedDocCtx = factory.createExchangedDocumentContextType()
				.withTestIndicator(factory.createIndicatorType().withIndicator(Boolean.TRUE));
		DocumentContextParameterType ctxParam = factory.createDocumentContextParameterType()
				.withID(createIdFromString("urn:ferd:CrossIndustryDocument:invoice:1p0:comfort"));
		exchangedDocCtx.getGuidelineSpecifiedDocumentContextParameter().add(ctxParam );
		root.setSpecifiedExchangedDocumentContext(exchangedDocCtx);
		
		// now the header information follows
		ExchangedDocumentType exchangedDocumentType = factory.createExchangedDocumentType()
				.withID(createIdFromString(invoice.getName()))
				.withName(createText(DocumentTypeUtil.findByBillingType(invoice.getBillingType()).name()));
		DocumentCodeType docTypeCode = factory.createDocumentCodeType()
				.withValue("380"); // this is static for BASIC and COMFORT
		exchangedDocumentType.setTypeCode(docTypeCode);
		exchangedDocumentType.setIssueDateTime(createDateTime(invoice.getDocumentDate()));
		
        createNote(invoice.getMessage(), exchangedDocumentType);
        createNote(invoice.getMessage2(), exchangedDocumentType);
        createNote(invoice.getMessage3(), exchangedDocumentType);
		
		NoteType note = factory.createNoteType();
		
		String owner = String.format("%s%n%s%n%s%n%s %s%n%s", 
				preferences.getString(Constants.PREFERENCES_YOURCOMPANY_OWNER),
				preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME),
				preferences.getString(Constants.PREFERENCES_YOURCOMPANY_STREET),
				preferences.getString(Constants.PREFERENCES_YOURCOMPANY_ZIP),
				preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CITY),
				preferences.getString(Constants.PREFERENCES_YOURCOMPANY_VATNR));
		if(StringUtils.isBlank(owner)) {
			owner = "(unknown)";
		}

		note.getContent().add(createText(owner));
		note.setSubjectCode(createCode("REG"));
		exchangedDocumentType.getIncludedNote().add(note);
		root.setHeaderExchangedDocument(exchangedDocumentType);
		
		// now follows the huge part for trade transaction
		SupplyChainTradeTransactionType tradeTransaction = factory.createSupplyChainTradeTransactionType();
		SupplyChainTradeAgreementType tradeAgreement = factory.createSupplyChainTradeAgreementType()
				.withBuyerReference(createText(invoice.getCustomerRef()));  // "Kundenreferenz"
		tradeTransaction.getApplicableSupplyChainTradeAgreement().add(tradeAgreement);
		
		// create seller information
		TradePartyType seller = factory.createTradePartyType()
//				.withID(createIdFromString("id assigned from customer(!)"))
				.withName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME)))
//TODO EXTENDED				.withDefinedTradeContact(createContact(invoice, ContactType.SELLER))
				.withPostalTradeAddress(createAddress(invoice, ContactType.SELLER))
//				.withGlobalID(createIdWithSchemeFromString("EAN|BIC or whatever", "according to ISO 6523"))
				.withSpecifiedTaxRegistration(createTaxNumber(invoice, ContactType.SELLER));
		tradeAgreement.setSellerTradeParty(seller);
		
		// create buyer information
		TradePartyType buyer = factory.createTradePartyType()
				.withID(createIdFromString(invoice.getBillingContact().getCustomerNumber()))
				.withName(createText(invoice.getAddressFirstLine()))
//TODO EXTENDED					.withDefinedTradeContact(createContact(invoice, ContactType.BUYER))
				.withPostalTradeAddress(createAddress(invoice, ContactType.BUYER))
//				.withGlobalID(createIdWithSchemeFromString("EAN|BIC or whatever", "according to ISO 6523"))
				.withSpecifiedTaxRegistration(createTaxNumber(invoice, ContactType.BUYER));
		tradeAgreement.setBuyerTradeParty(buyer);
			
			// EXTENDED: Applicable.Trade_DeliveryTerms (not yet available) => Incoterms
//			tradeAgreement.
		
		// referenced order
		Transaction transaction = ContextInjectionFactory.make(Transaction.class, eclipseContext).of(invoice);
		ReferencedDocumentType orderRef = factory.createReferencedDocumentType()
				.withIssueDateTime(createSimpleDateTime(invoice.getOrderDate()))
				.withID(createIdFromString(transaction.getReference(DocumentType.ORDER)));
		// only if ID is not empty!
		if(!StringUtils.isEmpty(transaction.getReference(DocumentType.ORDER))) {
			tradeAgreement.getBuyerOrderReferencedDocument().add(orderRef);
		}

		// there is no contract information in Fakturama!
//          ReferencedDocumentType contractRef = factory.createReferencedDocumentType();
//          contractRef.setIssueDateTime(value); // unknown
//			contractRef.getID().add(createIdFromString("contractNumber"))  // unknown
//			tradeAgreement.setContractReferencedDocument(contractRef);
			
			// EXTENDED: AdditionalReferencedDocument (IssueDateTime, TypeCode, ID)
			
			// unknown
//			ReferencedDocumentType custOrder = factory.createReferencedDocumentType(); + IssueDateTime, ID
//			tradeAgreement.setCustomerOrderReferencedDocument(custOrder);
			
		SupplyChainTradeDeliveryType tradeDelivery = factory.createSupplyChainTradeDeliveryType();
			// Related.SupplyChain_Consignment => EXTENDED (not yet available)
		SupplyChainEventType deliveryEvent = factory.createSupplyChainEventType();
		deliveryEvent.getOccurrenceDateTime().add(createDateTime(invoice.getServiceDate()));
		tradeDelivery.getActualDeliverySupplyChainEvent().add(deliveryEvent);
			
		// Despatch Advice_ Referenced.Referenced_ Document => EXTENDED (not yet available)

		ReferencedDocumentType refDoc = factory.createReferencedDocumentType();
		String deliveryRef = transaction.getReference(DocumentType.DELIVERY);
		String splitted[] = StringUtils.isEmpty(deliveryRef) ? new String[]{} : deliveryRef.split(",");
		for (String string : splitted) {
			refDoc.getID().add(createIdFromString(string));
		}
		refDoc.setIssueDateTime(DataUtils.getInstance().DateAsISO8601String(transaction.getFirstReferencedDocumentDate(DocumentType.DELIVERY)));
		if(!refDoc.getID().isEmpty()) {
			tradeDelivery.setDeliveryNoteReferencedDocument(refDoc);
		}
		tradeTransaction.setApplicableSupplyChainTradeDelivery(tradeDelivery);
		String currency = getGlobalCurrencyCode();
//		
//		// Verwendungszweck, Kassenzeichen 
		SupplyChainTradeSettlementType tradeSettlement = factory.createSupplyChainTradeSettlementType()
				.withPaymentReference(createText(invoice.getName())) /* customerref ? */
				.withInvoiceCurrencyCode(createCurrencyCode(currency));
		// Detailinformationen zum abweichenden Rechnungsempfänger
//		tradeSettlement.setInvoiceeTradeParty(createTradeParty(invoice));  // das wird gar nicht erfaßt!

		Contact contact = invoice.getBillingContact();
		DebtorFinancialAccountType debtorAccount = createDebtorAccount(contact);
		IDType id = StringUtils.isNotBlank(contact.getMandateReference()) ? 
				factory.createIDType().withValue(contact.getMandateReference())
				.withSchemeAgencyID(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CREDITORID)) : 
					null;
		TradeSettlementPaymentMeansType paymentType = factory.createTradeSettlementPaymentMeansType()
				.withTypeCode(createPaymentTypeCode(invoice))
				.withInformation(createText(invoice.getPayment().getName()))
				.withID(id);
		CreditorFinancialAccountType creditor = createCreditorAccount();
		if(creditor != null) {
			paymentType.setPayeePartyCreditorFinancialAccount(creditor);
			paymentType.setPayeeSpecifiedCreditorFinancialInstitution(createCreditorFinancialInstitution());

		}
		if(debtorAccount != null) {
			paymentType.setPayerPartyDebtorFinancialAccount(debtorAccount);
			paymentType.setPayerSpecifiedDebtorFinancialInstitution(createDebtorFinancialInstitution(invoice));
		}
		tradeSettlement.getSpecifiedTradeSettlementPaymentMeans().add(paymentType);

		// Get the items of the UniDataSet document
		List<DocumentItem> itemDataSets = invoice.getItems();
		for (int row = 0; row < itemDataSets.size(); row++) {
			// Get the item
			DocumentItem item = itemDataSets.get(row);
			tradeTransaction.getIncludedSupplyChainTradeLineItem().add(createLineItem(item, row + 1));
		}
		
		// Detailinformationen zur Rechnungsperiode 
		// TODO tradeSettlement.setBillingSpecifiedPeriod(createPeriod(invoice));
		tradeSettlement.getSpecifiedTradeAllowanceCharge().add(createTradeAllowance(documentSummary));
		tradeSettlement.getSpecifiedLogisticsServiceCharge().add(createLogisticsServiceCharge(invoice, documentSummary));
		tradeSettlement.getSpecifiedTradePaymentTerms().add(createTradePaymentTerms(invoice, documentSummary));
		tradeSettlement.setSpecifiedTradeSettlementMonetarySummation(createTradeSettlementMonetarySummation(invoice, documentSummary));
		// TODO EXTENDED: tradeSettlement.setReceivableSpecifiedTradeAccountingAccount(null);
		
		tradeTransaction.setApplicableSupplyChainTradeSettlement(tradeSettlement);

		// Get the VAT summary of the UniDataSet document
		VatSummarySetManager vatSummarySetManager = new VatSummarySetManager();
		vatSummarySetManager.add(invoice, Double.valueOf(1.0));
		for (VatSummaryItem vatSummaryItem : vatSummarySetManager.getVatSummaryItems()) {
			// für jeden Steuerbetrag muß es einen eigenen Eintrag geben
			tradeSettlement.getApplicableTradeTax().add(createTradeTax(vatSummaryItem));
		}
		root.setSpecifiedSupplyChainTradeTransaction(tradeTransaction);
	    return root;
    }

	/**
	 * @param invoice
	 * @param exchangedDocumentType
	 */
	private void createNote(String message, ExchangedDocumentType exchangedDocumentType) {
		if(!StringUtils.isEmpty(message)) {
			NoteType note = factory.createNoteType(); // free text on header level
			note.getContent().add(createText(message));
			note.setSubjectCode(createCode("AAK"));
			exchangedDocumentType.getIncludedNote().add(note);
		}
	}
	/**
	 * Creates a DateTime value in the form dd.MM.yyyy'T00:00:00'
     * @param dateValue
     * @return
     */
    private String createSimpleDateTime(Date dateValue) {
    	String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .format(dateValue);
	    return formatted;
    }

	/**
     * @return
     */
    private String getGlobalCurrencyCode() {
	    String currency = DataUtils.getInstance().getDefaultCurrencyUnit().getCurrencyCode();
//
//	    // TODO later on we will use JSR 354...
//	    switch (currency) {
//	    case "€":
//	    	currency = "EUR";
//	    	break;
//	    case "$":
//	    	currency = "USD";
//	    	break;
//	    default:
//	    	break;
//	    }
	    return currency;
    }
	
	private SupplyChainTradeLineItemType createLineItem(DocumentItem item, int row) {
		SupplyChainTradeLineItemType retval = factory.createSupplyChainTradeLineItemType()
				.withAssociatedDocumentLineDocument(createDocumentLine(item, row))
				.withSpecifiedSupplyChainTradeAgreement(createSpecifiedSupplyChainTradeAgreement(item, row))
				.withSpecifiedSupplyChainTradeDelivery(createSupplyChainTradeDelivery(item))
				.withSpecifiedSupplyChainTradeSettlement(createSupplyChainTradeSettlement(item))
				.withSpecifiedTradeProduct(createTradeProduct(item))
				;
	    return retval;
    }

	/**
	 * Gruppierung von Angaben zum Produkt bzw. zur erbrachten Leistung
	 *   
	 * @param item
	 * @return
	 */
	private TradeProductType createTradeProduct(DocumentItem item) {
		TradeProductType retval = factory.createTradeProductType()
	//			.withGlobalID(createIdFromString("EAN"))
				.withSellerAssignedID(createIdFromString(item.getItemNumber()))
				.withBuyerAssignedID(createIdFromString("buyerassigned ID"))
				.withName(createText(item.getName()))
				.withDescription(createText(item.getDescription()))
// TODO EXTENDED	.withOriginTradeCountry(createTradeCountry(item))
				;
	    return retval;
    }

	/**
	 * Gruppierung von Angaben zur Abrechnung auf Positionsebene 
	 * @param item
	 * @return
	 */
	private SupplyChainTradeSettlementType createSupplyChainTradeSettlement(DocumentItem item) {
		SupplyChainTradeSettlementType retval = factory.createSupplyChainTradeSettlementType()
	    		.withApplicableTradeTax(createTradeTax(item.getItemVat()))
	    		.withSpecifiedTradeSettlementMonetarySummation(createTradeSettlementMonetarySummation(item))
	    		;
		return retval;
    }

	private SupplyChainTradeDeliveryType createSupplyChainTradeDelivery(DocumentItem item) {
		String qunit = determineQuantityUnit(item.getQuantityUnit());
	    return factory.createSupplyChainTradeDeliveryType()
	    		.withBilledQuantity(createQuantity(item.getQuantity(), qunit))
// TODO EXTENDED:  	.withActualDeliverySupplyChainEvent(createSupplyChainEvent(item))
// TODO EXTENDED:	Despatch Advice_ Referenced.Referenced_ Document   
// TODO EXTENDED:	Receiving Advice_ Referenced. Referenced_ Document    		
// TODO EXTENDED:	Delivery Note_ Referenced. Referenced_ Document   		
	    		;
    }

//	/**
//	 * Detailinformationen zur tatsächlichen Lieferung 
//	 * 
//	 * @param item
//	 * @return
//	 */
//	private SupplyChainEventType createSupplyChainEvent(DocumentItem item) {
//		return factory.createSupplyChainEventType()
//				.withOccurrenceDateTime(createDateTime(dateString))
//		;
//    }

	private QuantityType createQuantity(Double value, String unit) {
	    return factory.createQuantityType()
	    		.withValue(String.format(Locale.ENGLISH, "%.4f", value))
	    		// use a default value since this field shouldn't be empty
	    		.withUnitCode(StringUtils.isBlank(unit) ? "C62" : unit);
    }

	/**
	 * Gruppierung der Vertragsangaben auf Positionsebene
	 * @param item
	 * @param row
	 * @return
	 */
	private SupplyChainTradeAgreementType createSpecifiedSupplyChainTradeAgreement(DocumentItem item, int row) {
		SupplyChainTradeAgreementType retval = factory.createSupplyChainTradeAgreementType()
//TODO EXTENDED
//			.withBuyerOrderReferencedDocument(createBuyerOrderReferencedDocument(item))
//			.withContractReferencedDocument(createContractReferencedDocument(item))
//       - Additional_ Referenced.Referenced_ Document  
				.withGrossPriceProductTradePrice(createTradePrice(item, PriceType.GROSS_PRICE))
				.withNetPriceProductTradePrice(createTradePrice(item, PriceType.NET_PRICE_DISCOUNTED))
// Detailangaben zur zugehörigen Endkundenbestellung
//TODO EXTENDED			.withCustomerOrderReferencedDocument(value)
				;
	    return retval;
    }

//	/**
//	 * Detailangaben zum zugehörigen Vertrag 
//	 * @param item
//	 * @return
//	 */
//	private ReferencedDocumentType createContractReferencedDocument(DocumentItem item) {
//	    // TODO Auto-generated method stub
//	    return null;
//    }
//
//	/**
//	 * Detailangaben zur zugehörigen Bestellung 
//	 * @param item
//	 * @return
//	 */
//	private ReferencedDocumentType createBuyerOrderReferencedDocument(DocumentItem item) {
//	    // TODO Auto-generated method stub
//	    return null;
//    }

	/**
	 * Detailinformationen zum Preis gemäß Bruttokalkulation 
	 * exklusive Umsatzsteuer.
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
				.withChargeAmount(createAmount(price.getUnitNet(), DEFAULT_AMOUNT_SCALE))
				// TODO Preisbasismenge??? (1, 10, 100,...)
// EXTENDED				.withBasisQuantity(createQuantity(1d, qunit))
				;
				if(discount != 0) {
					// Rabatt / Zuschlag auf Positionsebene
					retval.getAppliedTradeAllowanceCharge().add(createTradeAllowance(item));
				}
			break;
		case NET_PRICE:
			
			retval = factory.createTradePriceType()
			// Preis nach Bruttokalkulation ohne Umsatzsteuer 
			.withChargeAmount(createAmount(Money.of(item.getPrice(), DataUtils.getInstance().getDefaultCurrencyUnit()), 
					DEFAULT_AMOUNT_SCALE))
			// TODO Preisbasismenge??? (1, 10, 100,...)
//			.withBasisQuantity(createQuantity(1d, qunit))
			;
//			if(discount != 0) {
//				// Rabatt / Zuschlag auf Positionsebene
//				retval.getAppliedTradeAllowanceCharge().add(createTradeAllowance(item));
//			}
			break;
		case NET_PRICE_DISCOUNTED:
			// Detailinformationen zum Preis gemäß Nettokalkulation exklusive Umsatzsteuer
			retval = factory.createTradePriceType()
				// "ITEM.UNIT.NET.DISCOUNTED"
				// Preis nach Bruttokalkulation +- Zu-/Abschläge = Preis 
				// nach Nettokalkulation;
				.withChargeAmount(createAmount(price.getUnitNetDiscounted(), DEFAULT_AMOUNT_SCALE))
				// TODO Preisbasismenge??? (1, 10, 100,...)
				.withBasisQuantity(createQuantity(1d, qunit))
				;
//			if(discount != 0) {
//				// Rabatt / Zuschlag auf Positionsebene
//				retval.getAppliedTradeAllowanceCharge().add(createTradeAllowance(item));
//			}
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
		if(StringUtils.isNotBlank(userdefinedQuantityUnit))	{
			Optional<CEFACTCode> code = measureUnits.findByAbbreviation(userdefinedQuantityUnit, LocaleUtil.getInstance().getDefaultLocale());
			isoUnit = code.isPresent() ? code.get().getCode() : "";
		}
		return isoUnit;
	}

	private DocumentLineDocumentType createDocumentLine(DocumentItem item, int row) {
		DocumentLineDocumentType retval = factory.createDocumentLineDocumentType()
		// TODO Detailinformationen zum Freitext zur Position 
//		.withIncludedNote(null)
				.withLineID(createIdFromString(Integer.toString(row)));
	    return retval;
    }

	/**
	 * Detailinformationen zu Belegsummen.
	 * 
	 * @param invoice
	 * @param documentSummary 
	 * @return
	 */
	private TradeSettlementMonetarySummationType createTradeSettlementMonetarySummation(Document invoice, DocumentSummary documentSummary) {
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
		TradeSettlementMonetarySummationType retval = factory.createTradeSettlementMonetarySummationType()
				.withLineTotalAmount(createAmount(totalAmount))
				.withChargeTotalAmount(createAmount(documentSummary.getShippingNet()))
				.withAllowanceTotalAmount(createAmount(allowanceAmount))
				.withTaxBasisTotalAmount(createAmount(taxBasisTotalAmount))
				.withTaxTotalAmount(createAmount(documentSummary.getTotalVat()))
				.withGrandTotalAmount(createAmount(taxBasisTotalAmount.add(documentSummary.getTotalVat())))
			//	.withTotalPrepaidAmount(createAmount(Money.of(invoice.getPaidValue(), DataUtils.getInstance().getDefaultCurrencyUnit())))
			//	.withDuePayableAmount(createAmount(documentSummary.getTotalGross().subtract(Money.of(invoice.getPaidValue(), DataUtils.getInstance().getDefaultCurrencyUnit()))
			//			)
				;
	    return retval;
    }

	private TradeSettlementMonetarySummationType createTradeSettlementMonetarySummation(DocumentItem item) {
		/*
		 * Der Gesamtpositionsbetrag ist der Nettobetrag unter Berücksichtigung von Zu- und Abschlägen ohne 
		 * Angabe des Umsatzsteuerbetrages. 
		 */
		Price price = new Price(item);
		TradeSettlementMonetarySummationType retval = factory.createTradeSettlementMonetarySummationType()
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

	private TradePaymentTermsType createTradePaymentTerms(Document invoice, DocumentSummary documentSummary) {
		LocalDateTime dueDate = 
				DataUtils.getInstance().addToDate(invoice.getDocumentDate(), invoice.getDueDays());
		Placeholders placeholders = ContextInjectionFactory.make(Placeholders.class, eclipseContext);
		double percent = invoice.getPayment().getDiscountValue();
		
		Date out = Date.from(dueDate.atZone(ZoneId.systemDefault()).toInstant());
		
		TradePaymentTermsType tradePaymentTerms = factory.createTradePaymentTermsType()
			.withDescription(createText(placeholders.createPaymentText(invoice, documentSummary, percent)))
			.withDueDateDateTime(createDateTime(out));
		// TODO EXTENDED: Applicable. Trade_ Payment Penalty Terms
		// TODO EXTENDED: Applicable. Trade_ Payment Discount Terms
		return tradePaymentTerms;
    }

	private LogisticsServiceChargeType createLogisticsServiceCharge(Document invoice, DocumentSummary documentSummary) {
		LogisticsServiceChargeType logisticsServiceCharge;
		if(invoice.getShipping() != null) {
			logisticsServiceCharge = factory.createLogisticsServiceChargeType()
				.withDescription(createText(invoice.getShipping().getDescription()))			
				.withAppliedAmount(createAmount(documentSummary.getShippingNet(), 2, true));
			VAT shippingVat = invoice.getShipping().getShippingVat();
			if(shippingVat != null) {
				logisticsServiceCharge.getAppliedTradeTax().add(createTradeTax(shippingVat));
			}
		} else if(invoice.getShippingValue() != null) {
			logisticsServiceCharge = factory.createLogisticsServiceChargeType()
					.withAppliedAmount(createAmount(documentSummary.getShippingNet(), 2, true));
		} else {
			logisticsServiceCharge = factory.createLogisticsServiceChargeType();
		}
		
		return logisticsServiceCharge;
    }

	/**
	 * Detailinformationen zu Zu- und Abschlägen.
	 * 
	 * @param item
	 * @return
	 */
	private TradeAllowanceChargeType createTradeAllowance(DocumentItem item) {
		Double discountPercent = item.getItemRebate();
		Double amount = item.getPrice() * discountPercent;
		Price price = new Price(item);
		double factor = Math.pow(10, DEFAULT_AMOUNT_SCALE);
		double s = Math.round(price.getUnitNet().multiply(factor).getNumber().doubleValue()) / factor;
		double t = Math.round(price.getUnitNetDiscounted().multiply(factor).getNumber().doubleValue()) / factor;
		double u = Math.round((s-t) * factor) / factor;
		boolean isAllowance = amount > 0;
		if(!isAllowance) {
			amount *= -1;
		}
		return factory.createTradeAllowanceChargeType()
			.withChargeIndicator(factory.createIndicatorType().withIndicator(isAllowance))
			.withActualAmount(createAmount(Money.of(u/*amount*/, DataUtils.getInstance().getDefaultCurrencyUnit()), DEFAULT_AMOUNT_SCALE, true))
//			.withBasisAmount(createAmount(item.getDoubleValueByKey("price")))
			.withReason(createText(msg.zugferdExportLabelRebate));
//			.withCategoryTradeTax(createTradeTax(item.getDoubleValueByKey("vatvalue")));
	}

	private TradeAllowanceChargeType createTradeAllowance(DocumentSummary summary) {
		MonetaryAmount amount = summary.getDiscountGross();
		boolean isAllowance = amount.isPositive();
		if(!isAllowance) {
			amount = amount.multiply(-1);
		}
		return factory.createTradeAllowanceChargeType()
			.withChargeIndicator(factory.createIndicatorType().withIndicator(isAllowance))
			.withActualAmount(createAmount(amount, 2 /*DEFAULT_AMOUNT_SCALE*/))
//			.withBasisAmount(createAmount(summary.getItemsNet().asRoundedDouble(), true))
			.withReason(createText(msg.zugferdExportLabelRebate))
// TODO which VAT?			.withCategoryTradeTax(createTradeTax(item.getDoubleValueByKey("vatvalue")));
			;
    }

	private TradeTaxType createTradeTax(VAT vatValue) {
	    return factory.createTradeTaxType()
	    		.withApplicablePercent(factory.createPercentType().withValue(String.format(Locale.ENGLISH, "%.2f", vatValue.getTaxValue() * 100)))
				.withCategoryCode(createTaxCategoryCode("S"))   // Standard rate, FIXME for other uses!
				.withTypeCode(createTaxTypeCode("VAT"))
				;
    }

	private TradeTaxType createTradeTax(VatSummaryItem vatSummaryItem) {
		// VAT description
		// (unused) String key = vatSummaryItem.getVatName();
		// It's the VAT value
		MonetaryAmount basisAmount = netPricesPerVat.get(DataUtils.getInstance().DoubleToFormatedPercent(vatSummaryItem.getVatPercent()));
		TradeTaxType retval = factory.createTradeTaxType()
				.withCalculatedAmount(createAmount(basisAmount.multiply(vatSummaryItem.getVatPercent())))
				.withApplicablePercent(factory.createPercentType().withValue(String.format(Locale.ENGLISH, "%.2f", DataUtils.getInstance().round(vatSummaryItem.getVatPercent() * 100))))
				.withBasisAmount(createAmount(basisAmount))
				.withCategoryCode(createTaxCategoryCode("S"))   // Standard rate, FIXME for other uses!
				//.withExemptionReason(TODO)
				.withTypeCode(createTaxTypeCode("VAT"))
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
		        .withApplicablePercent(factory.createPercentType()
		        .withValue(String.format(Locale.ENGLISH, "%.2f", (vatPercent * 100.0))))
		        .withCategoryCode(createTaxCategoryCode("S")) // Standard rate, FIXME for other uses!
		        //.withExemptionReason(TODO)
		        .withTypeCode(createTaxTypeCode("VAT"));
		return retval;
	}

	private TaxTypeCodeType createTaxTypeCode(String string) {
		return factory.createTaxTypeCodeType()
				.withValue(string);
	}

	private TaxCategoryCodeType createTaxCategoryCode(String taxCat) {
		return factory.createTaxCategoryCodeType()
				.withValue(taxCat);
	}

	/**
	 * creates an Amount field
	 * @param the VAT value
	 * @return
	 */
	private AmountType createAmount(MonetaryAmount amount) {
		return createAmount(amount, 2, true);
	}
	
	private AmountType createAmount(MonetaryAmount amount, int scale) {
		return createAmount(amount, scale, true);
	}

	/**
	 * Creates an Amount with given value, scale and currency.
	 * @param amount
	 * @param scale
	 * @param withCurrency
	 * @return
	 */
	private AmountType createAmount(MonetaryAmount amount, int scale, boolean withCurrency) {
		return factory.createAmountType()
				.withValue(String.format(Locale.ENGLISH, "%."+scale+"f", amount.getNumber().doubleValue()))
				.withCurrencyID(withCurrency ? amount.getCurrency().getCurrencyCode() : null);		
	}

	private CreditorFinancialInstitutionType createCreditorFinancialInstitution() {
		return factory.createCreditorFinancialInstitutionType()
				.withBICID(createIdFromString(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_BIC)))
//				.withGermanBankleitzahlID(createIdFromString(preferences.getString("YOURCOMPANY_COMPANY_BANKCODE")))
				.withName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_BANK)));
	}

	private DebtorFinancialInstitutionType createDebtorFinancialInstitution(Document invoice) {
		if(!StringUtils.isEmpty(invoice.getBillingContact().getBankAccount().getBic())) {
			return factory.createDebtorFinancialInstitutionType()
					.withBICID(createIdFromString(invoice.getBillingContact().getBankAccount().getBic()))
//					.withGermanBankleitzahlID(createIdFromString(invoice.getFormatedStringValueByKeyFromOtherTable("addressid.CONTACTS:bank_code")))
					.withName(createText(invoice.getBillingContact().getBankAccount().getBankName()));
		} else return null;
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

	private DebtorFinancialAccountType createDebtorAccount(Contact contact) {
		if(contact.getBankAccount() != null 
				&& !StringUtils.isEmpty(contact.getBankAccount().getIban())) {
			return factory.createDebtorFinancialAccountType()
					.withIBANID(createIdFromString(contact.getBankAccount().getIban()))
//					.withProprietaryID(createIdFromString(invoice.getFormatedStringValueByKeyFromOtherTable("addressid.CONTACTS:account")))
					;
		} else return null;
	}

	private PaymentMeansCodeType createPaymentTypeCode(Document invoice) {
		// TODO implement lookup:  UNCL 4461 !!!
		
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

//	private TradePartyType createTradeParty(Document invoice) {
//		String customerNumber = invoice.getFormatedStringValueByKeyFromOtherTable("addressid.CONTACTS:nr");
//		TradePartyType retval = factory.createTradePartyType().withID(createIdWithSchemeFromString(customerNumber, "SELLER_ASSIGNED"))
//		//	.withGlobalID(...)
//			.withName(createText(invoice.getStringValueByKey("addressfirstline")))
//			// .withDefinedTradeContact(createContact(invoice, contactType)) TODO EXTENDED
//			.withPostalTradeAddress(createAddress(invoice, ContactType.BUYER))
//			.withSpecifiedTaxRegistration(createTaxNumber(invoice, ContactType.BUYER))
//			;
//		return retval;
//	}

	private CodeType createCurrencyCode(String currency) {
		return factory.createCodeType().withValue(currency);
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
			if(!StringUtils.isEmpty(invoice.getBillingContact().getVatNumber())) {
				retval = factory.createTaxRegistrationType()
						.withID(createIdWithSchemeFromString(getNullCheckedValue(invoice.getBillingContact().getVatNumber()), "VA"));
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
	//		.withLineTwo(is empty at the moment);
				.withCityName(createText(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CITY)))
				.withCountryID(createCountry(countryCode));
				//.withCountryID(createCountry(preferences.getString("YOURCOMPANY_COMPANY_COUNTRY")));
			break;
		case BUYER:
			// Korrektur
			countryCode = invoice.getBillingContact().getAddress().getCountryCode();
			retval = factory.createTradeAddressType()
				.withPostcodeCode(createCode(invoice.getBillingContact().getAddress().getZip()))
				.withLineOne(createText(invoice.getBillingContact().getAddress().getStreet()))
	//		.withLineTwo(is empty at the moment)
				.withCityName(createText(invoice.getBillingContact().getAddress().getCity()))
				.withCountryID(createCountry(countryCode));
//			.withCountryID(createCountry(invoice.getFormatedStringValueByKeyFromOtherTable("addressid.CONTACTS:country")));
			break;
		default:
			break;
		}
		return retval;
	}

	private CountryIDType createCountry(String value) {
		if(StringUtils.length(value) > 2) {
			value = LocaleUtil.getInstance().findCodeByDisplayCountry(value);
			if(value == null) {
				value = "DE"; // null values aren't allowed!
			}
		}
		return factory.createCountryIDType().withValue(value);
	}

//	private TradeContactType createContact(Document invoice, ContactType contactType) {
//		TradeContactType contact = factory.createTradeContactType();
//		switch (contactType) {
//		case SELLER:
//			// this is only EXTENDED profile!
//			contact.setPersonName(createText(preferences.getString("YOURCOMPANY_COMPANY_OWNER"))); // YOURCOMPANY.OWNER
//			//		contact.setDepartmentName(createText(dept));  // unknown
//			contact.getTelephoneUniversalCommunication().add((createCommunicationItem(preferences.getString("YOURCOMPANY_COMPANY_TEL"))));
//			contact.getFaxUniversalCommunication().add(createCommunicationItem(preferences.getString("YOURCOMPANY_COMPANY_FAX")));
//			contact.setEmailURIUniversalCommunication(createCommunicationItem(preferences.getString("YOURCOMPANY_COMPANY_EMAIL")));
//			break;
//		case BUYER:
//			// "consultant"???
//			contact.setPersonName(createText(invoice.getFormatedStringValueByKey("addressfirstline")));
////			contact.setDepartmentName(createText(dept));  // unknown
//			// this is only EXTENDED profile!
//			contact.getTelephoneUniversalCommunication().add(createCommunicationItem(invoice.getFormatedStringValueByKeyFromOtherTable("addressid.CONTACTS:phone")));
//			contact.getFaxUniversalCommunication().add(createCommunicationItem(invoice.getFormatedStringValueByKeyFromOtherTable("addressid.CONTACTS:fax")));
//			contact.setEmailURIUniversalCommunication(createCommunicationItem(invoice.getFormatedStringValueByKeyFromOtherTable("addressid.CONTACTS:email")));
//			break;
//		default:
//			break;
//		}
//		return contact;
//	}
	
//	private UniversalCommunicationType createCommunicationItem(String communicationItem) {
//		return factory.createUniversalCommunicationType().withCompleteNumber(createText(communicationItem));
//	}
	
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
			DateTimeString dateTypeString = factory.createDateTimeTypeDateTimeString();
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
	
	private void printDocument(org.w3c.dom.Document doc, StreamResult streamResult) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), streamResult);
	}
	
	private void printDocument(org.w3c.dom.Document doc, OutputStream out) throws IOException, TransformerException {
	    StreamResult streamResult = new StreamResult(new OutputStreamWriter(out, "UTF-8"));
		printDocument(doc, streamResult);
	}
	
	private Document findSelectedInvoice(EPartService partService) {
		Document retval = null;
		
		// at first we try to use an open editor
		if(partService != null && StringUtils.equalsIgnoreCase(partService.getActivePart().getElementId(), DocumentEditor.ID)) {
			DocumentEditor editor = (DocumentEditor)partService.getActivePart().getObject();
			retval = editor.getDocument();
		} else if(selectionService != null && selectionService.getSelection() != null) {
			List<Document> tmpList = (List<Document>) selectionService.getSelection();
			retval = tmpList.get(0);
		}

		return retval;
	}
}
