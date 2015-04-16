/**
 * 
 */
package com.sebulli.fakturama.i18n;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.eclipse.e4.core.services.nls.Message;

/**
 * This class contains all the message keys from
 * OSGI-INF/l10n/bundle*.properties for a convenient access to translated
 * strings.
 * 
 * @author rheydenr
 *
 */
@Message(contributionURI="platform:/plugin/com.sebulli.fakturama.rcp")
public class Messages {

    /*
     * Tip: Suche mit \.(\w)
     * Ersetze durch \U\1
     */
    
	public String AccountSettingsPage159;
	public String AccountSettingsPage160;
	public String AccountSettingsPage161;

    public String preferencesOfficeAppfieldNovalidapp;
    public String preferencesOfficeAppfieldNovalidfolder;

    public String editorBrowserButtonBack;
    public String editorBrowserButtonForward;
    public String editorBrowserButtonReload;
    public String editorBrowserButtonStop;
    public String editorBrowserButtonHome;
    public String editorBrowserButtonProject;

	public String preferencesBrowserUrl;
	public String preferencesBrowserType;
	public String preferencesBrowserShowaddressbar;
	public String preferencesGeneral;

    public String editorContactErrorCustomerid;
    public String editorContactErrorNotnextfreenumber;
    public String editorContactHintSeepreferences;
	public String editorContactLabelAddress;
    public String editorContactLabelBankaccount;
    public String editorContactFieldAccountnumberDisabledinfo;
    public String editorContactFieldBankcodeDisabledinfo;
    public String editorContactFieldNumberTooltip;
    public String editorContactFieldDeliveryaddressequalsName;
    public String editorContactFieldLastnamefirstnameName;
    public String editorContactFieldFirstnamelastnameName;
    public String editorContactFieldZipcityName;
    public String editorContactHintSethomecountry;
    public String editorContactFieldBirthdayName;
    public String editorContactFieldBirthdayTooltip;
    public String editorContactFieldDeliverersbirthdayTooltip;
    public String editorContactFieldCategoryTooltip;
    public String editorContactFieldSuppliernumberName;
    public String editorContactFieldPaymentTooltip;
    public String editorContactFieldDiscountTooltip;
    public String editorContactFieldNetgrossName;
    public String editorContactErrorCustomernumber;
    public String editorContactLabelNotice;
    public String editorContactFieldNumberName;

	public String preferencesContactFormatSalutation;
	public String preferencesContactFormatSalutationMen;
	public String preferencesContactFormatSalutationWomen;
	public String preferencesContactFormatSalutationCompany;
	public String preferencesContactFormatAddressfieldlabel;
	public String preferencesContactFormatHidecountries;
	public String preferencesContactFormatAddressfield;
	public String dataDefaultContactFormatSalutation;
	public String dataDefaultContactFormatSalutationMen;
	public String dataDefaultContactFormatSalutationWomen;
	
	/**
	 * Preference page "Contact Format" - Example format Strings (Hidden
	 * countries) Separate the country by a comma. If the county name is one in
	 * this list, it won't be displayed in the address field. E.g. for a German
	 * language you should enter "Deutschland,Germany". There should be at least
	 * 2 names, separated by a comma. So that the user can see the format. Even
	 * if 2 countries don't make much sense like USA,U.S.A. for the English
	 * language.
	 */
	public String dataDefaultContactFormatExcludedcountries;
	
	public String dataExpenditureItems;
	public String dataReceiptvoucherItems;

	public String preferencesContactUsedelivery;
	public String preferencesContactUsebankaccount;
	public String preferencesContactUsemiscpage;
	public String preferencesContactUsenotepage;
	public String preferencesContactUsegender;
	public String preferencesContactUsetitle;
	public String preferencesContactNameformat;
	public String preferencesContactFirstlastname;
	public String preferencesContactLastfirstname;
	public String preferencesContactUsecompany;
	public String preferencesContactUsecountry;
	public String preferencesContact;

	public String CountryCodes697;

	public String CreateOODocumentAction90;

	public String Data653;
	public String dataDefaultVat;
	public String dataDefaultVatDescription;
	public String dataDefaultShipping;
	public String dataDefaultShippingDescription;
	public String dataDefaultPayment;
	public String dataDefaultPaymentDescription;
	public String dataDefaultPaymentPaidtext;
	public String dataDefaultPaymentUnpaidtext;

    public String contactFieldMrName;
    public String contactFieldMsName;
    public String contactFieldReliabilityPoorName;
    public String contactFieldReliabilityMediumName;
    public String contactFieldReliabilityGoodName;

    public String documentOrderStateNotshipped;

	public String DataSetListNames668;
	public String DataSetListNames669;
	public String DataSetListNames670;

	public String DataSetReceiptVoucher671;

    public String dataVatPurchasetax;
    public String dataVatSalestax;

	public String DeleteDataSetAction100;
	public String DeleteDataSetAction101;
	public String DeleteDataSetAction99;

    public String editorDocumentErrorDocnumberTitle;
    public String editorDocumentErrorDocnumberNotnextfree;
    public String editorDocumentErrorWrongcontactTitle;
    public String editorDocumentErrorWrongcontactMsg1;
    public String editorDocumentErrorWrongcontactMsg2;
    public String editorDocumentTotalgross;
    public String editorDocumentTotalnet;
    public String editorDocumentDialogGrossvalues;
    public String editorDocumentDialogNetvalues;
    public String editorDocumentDateofpayment;
    public String editorDocumentPaidvalue;
    public String editorDocumentDuedays;
    public String editorDocumentDuedaysTooltip;
    public String editorDocumentPayuntil;
    public String editorDocumentPayuntilTooltip;
    public String editorDocumentWarningDifferentaddress;
    public String editorDocumentWarningDifferentdeliveryaddress;
    public String dialogCustomerStatisticsPart1;
    public String editorDocumentRefnumberTooltip;
    public String editorDocumentDateTooltip;
    public String editorDocumentFieldCustref;
    public String editorDocumentFieldCustrefTooltip;
    public String editorDocumentFieldConsultant;
    public String editorDocumentFieldConsultantTooltip;
    public String editorDocumentFieldServicedate;
    public String editorDocumentFieldServicedateTooltip;
    public String editorDocumentFieldRequestdate;
    public String editorDocumentFieldRequestdateTooltip;
    public String editorDocumentFieldOrderdate;
    public String editorDocumentFieldOrderdateTooltip;
    public String editorDocumentFieldInvoice;
    public String editorDocumentFieldInvoiceTooltip;
    public String editorDocumentFieldDeposit;
    public String editorDocumentZerovatTooltip;
    public String editorDocumentCreateduplicate;
    public String dialogSelectaddressTooltip;
    public String dialogSelectaddressTitle;
    public String editorDocumentItems;
    public String dialogSelectproductTooltip;
    public String dialogSelectproductTitle;
    public String editorDocumentAdditemTooltip;
    public String editorDocumentDeleteitemTooltip;
    public String editorDocumentFieldPosition;
    public String editorDocumentFieldQunit;
    public String editorDocumentFieldRemarks;
    public String editorDocumentFieldRemarksTooltip;
    public String editorDocumentFieldCommentTooltip;
    public String editorDocumentSelecttemplateTooltip;
    public String editorDocumentSelecttextTitle;
    public String editorDocumentDiscountTooltip;
    public String editorDocumentFieldShipping;
    public String editorDocumentFieldShippingTooltip;
    public String editorDocumentCheckboxPaidTooltip;
    public String editorDocumentDialogWarningDocumentexists;
    public String editorDocumentNetgrossTooltip;
    public String editorDocumentCollectiveinvoiceTooltip;
	
	public String preferencesDocumentUsenetgross;
    public String preferencesDocumentCopymsgfield;
    public String preferencesDocumentCopydescfield;
    public String preferencesDocumentDisplaypreview;
    public String preferencesDocumentUsepos;
    public String preferencesDocumentUsediscountsingle;
    public String preferencesDocumentUsediscountall;
    public String preferencesDocumentShowcustomerstat;
    public String preferencesDocumentLabelCompare;
    public String preferencesDocumentOnlycontactid;
    public String preferencesDocumentAlsoaddress;
    public String preferencesDocumentNumberofremarkfields;
    public String preferencesDocument;
    public String preferencesDocumentShowitemsprices;
    public String preferencesDocumentAdddelnotenumber;
    public String preferencesDocumentLabelDepositrow;
    public String preferencesDocumentLabelFinalrow;
    
    public String documentTypeProforma;
    public String documentTypeLetterPlural;
    public String documentTypeOfferPlural;
    public String documentTypeOrderPlural;
    public String documentTypeConfirmationPlural;
    public String documentTypeInvoicePlural;
    public String documentTypeDeliverynotePlural;
    public String documentTypeCredititemsPlural;
    public String documentTypeDunningPlural;
    public String documentTypeProformaPlural;

	public String commonButtonSetdefault;
	public String commonButtonSavechanges;
	public String commonButtonSavechangesquestion;

	public String viewErrorlogLabel;

	public String ExpenditureVoucherEditor641;

	public String ExportExpenditureWizard245;
	public String ExportExpenditureWizard246;

	public String ExportOptionPage167;
	public String ExportOptionPage227;
	public String ExportOptionPage228;
	public String ExportOptionPage229;
	public String ExportOptionPage243;
	public String ExportOptionPage253;

    public String preferencesExportUsepaydate;
    public String preferencesExport;

	public String ExportReceiptWizard254;

	public String ExportWizard154;
	public String ExportWizard155;
	public String ExportWizard156;
	public String ExportWizard157;
	public String ExportWizard158;
	public String ExportWizard168;
	public String ExportWizard169;
	public String ExportWizard170;
	public String ExportWizard171;
	public String ExportWizard172;
	public String ExportWizard173;
	public String ExportWizard203;
	public String ExportWizard204;
	public String ExportWizard205;
	public String ExportWizard206;
	public String ExportWizard207;
	public String ExportWizard208;
	public String ExportWizard218;
	public String ExportWizard230;
	public String ExportWizard231;
	public String ExportWizard244;
	public String ExportWizard256;

	public String ExportWizardPageStartEndDate219;
	public String ExportWizardPageStartEndDate220;
	public String ExportWizardPageStartEndDate221;
	public String ExportWizardPageStartEndDate222;

	public String ExportWizardUnpaid241;
	public String ExportWizardUnpaid242;

	public String commonFieldDate;
	public String commonFieldName;
	public String commonFieldText;
	public String commonFieldValue;
	public String commonFieldBalance;
	public String commonFieldDeliveryaddress;
	public String commonFieldCategory;
	public String commonFieldGender;
	public String commonFieldTitle;
	public String commonFieldFirstname;
	public String commonFieldLastname;
	public String commonFieldCompany;
	public String commonFieldStreet;
	public String commonFieldZipcode;
	public String commonFieldCity;
	public String commonFieldCountry;
	public String commonFieldAccountholder;
	public String commonFieldTotal;
	public String commonFieldOptional;
    public String commonFieldPicture;
    public String commonFieldUnitprice;
    public String editorContactFieldAccountnumberName;
    public String editorContactFieldBankcodeName;
    public String editorContactFieldBankName;
	public String exporterDataIban;
	public String exporterDataBic;
    public String editorContactFieldPaymentName;
    public String editorContactFieldReliabilityName;
    public String editorContactFieldMandaterefName;
	public String exporterDataTelephone;
	public String exporterDataTelefax;
	public String exporterDataMobile;
	public String exporterDataEmail;
	public String exporterDataWebsite;
	public String exporterDataVatno;
	public String exporterDataVatnoValid;
	public String exporterDataRebate;
	public String exporterDataItemnumber;
	public String commonFieldDescription;
	public String commonFieldPrice;
	public String commonFieldQuantity;
	public String commonFieldVat;
	public String Exporter214;
	public String Exporter215;
	public String Exporter216;
	public String Exporter217;
	public String Exporter223;
	public String Exporter224;
	public String Exporter225;
	public String Exporter226;
	public String Exporter232;
	public String Exporter233;
	public String Exporter234;
	public String Exporter235;
	public String Exporter236;
	public String Exporter237;
	public String Exporter238;
	public String Exporter239;
	public String Exporter240;
	public String Exporter247;
	public String Exporter248;
	public String Exporter249;
	public String productDataNet;
	public String productDataGross;
	public String Exporter252;
	public String Exporter255;

	public String FileOrganizer151;
	public String FileOrganizer152;
	public String FileOrganizer153;

	public String preferencesGeneralCollapsenavbar;
	public String preferencesGeneralCloseeditors;
	public String preferencesGeneralCurrency;
	public String preferencesGeneralThousandseparator;
	public String preferencesGeneralCurrencyLocale;
	public String preferencesGeneralCurrencyExample;
	public String preferencesGeneralCurrencyCashrounding;
	public String preferencesGeneralCurrencyCashroundingTooltip;

	public String ImportOptionPage122;
	public String ImportOptionPage123;
	public String ImportOptionPage124;
	public String ImportOptionPage125;

	public String ImportProgressDialog147;

	public String ImportWizard126;
	public String ImportWizard127;
	public String ImportWizard128;
	public String ImportWizard129;
	public String ImportWizard139;

	public String Importer130;
	public String Importer131;
	public String Importer132;
	public String Importer133;
	public String Importer134;
	public String Importer135;
	public String Importer136;
	public String Importer137;
	public String Importer138;
	public String Importer140;
	public String Importer141;
	public String Importer142;
	public String Importer143;
	public String Importer144;
	public String Importer145;
	public String Importer146;

	public String InstallAction94;

	public String ListEditor473;
	public String ListEditor474;
	public String ListEditor475;

    public String commandMarkorderCompleted;
    public String commandMarkorderWarnStockzero;

    public String commandDocumentsMoveDownName;
    public String commandDocumentsMoveDownTooltip;

    public String commandDocumentsMoveUpName;
    public String commandDocumentsMoveUpTooltip;

    public String widgetNovatproviderWithvatLabel;

	public String preferencesNumberrangeFormatCustomernoLabel;
	public String preferencesNumberrangeFormatItemnoLabel;
	public String preferencesNumberrangeFormatInvoicenoLabel;
	public String preferencesNumberrangeFormatDeliverynotenoLabel;
	public String preferencesNumberrangeFormatOffernoLabel;
	public String preferencesNumberrangeFormatOrdernoLabel;
	public String preferencesNumberrangeFormatConfirmationoLabel;
	public String preferencesNumberrangeFormatCreditnoLabel;
	public String preferencesNumberrangeFormatDunningnoLabel;
	public String preferencesNumberrangeFormatProformanoLabel;
	public String preferencesNumberrangeFormat;
	public String preferencesNumberrangeFormatCustomernoValue;
	public String preferencesNumberrangeFormatItemnoValue;
	public String preferencesNumberrangeFormatInvoicenoValue;
	public String preferencesNumberrangeFormatDeliverynotenoValue;
	public String preferencesNumberrangeFormatOffernoValue;
	public String preferencesNumberrangeFormatOrdernoValue;
	public String preferencesNumberrangeFormatConfirmationoValue;
	public String preferencesNumberrangeFormatCreditnoValue;
	public String preferencesNumberrangeFormatDunningnoValue;
	public String preferencesNumberrangeFormatProformanoValue;

	public String preferencesNumberrangeValuesLabelNextno;
	public String preferencesNumberrangeValuesLabelNextitemno;
	public String preferencesNumberrangeValuesLabelNextinvoiceno;
	public String preferencesNumberrangeValuesLabelNextdeliverynoteno;
	public String preferencesNumberrangeValuesLabelNextofferno;
	public String preferencesNumberrangeValuesLabelNextorderno;
	public String preferencesNumberrangeValuesLabelNextconfirmno;
	public String preferencesNumberrangeValuesLabelNextcreditno;
	public String preferencesNumberrangeValuesLabelNextdunningno;
	public String preferencesNumberrangeValuesLabelNextproformano;
	public String preferencesNumberrangeValuesLabel;

	public String OOCalcExporter257;
	public String OOCalcExporter258;
	public String OOCalcExporter259;

    public String preferencesOfficeExampleshort;
    public String preferencesOfficeApp;
    public String preferencesOfficeFolder;
    public String preferencesOfficeExportasLabel;
    public String preferencesOfficeOnlyodtLabel;
    public String preferencesOfficeOnlypdfLabel;
    public String preferencesOfficeOdtpdfLabel;
    public String preferencesOfficeFormatandpathodt;
    public String preferencesOfficeFormatandpathpdf;
    public String preferencesOfficeStartnewthread;
    public String preferencesOffice;

	public String viewErrorlogName;
	public String OfficeStarter149;
	public String OfficeStarter150;

    public String commandBrowserOpenUrllabel;
    public String commandBrowserOpenStartpage;

	public String OpenParcelServiceAction110;

	public String preferencesOptionalitemsUse;
	public String preferencesOptionalitemsReplaceprice;
	public String preferencesOptionalitemsPricereplacement;
	public String preferencesOptionalitemsLabel;
	public String preferencesOptionalitems;
	public String preferencesOptionalitemsItemlabel;

    public String dialogOrderstatusTitle;
    public String dialogOrderstatusFieldNotify;

	public String ParcelServiceFormFiller648;
	public String ParcelServiceFormFiller649;
	public String ParcelServiceFormFiller650;

	public String parcelServiceName;

    public String editorPaymentNameTooltip;
    public String editorPaymentAccountTooltip;
    public String editorPaymentDiscount;
    public String editorPaymentDiscountTooltip;
    public String editorPaymentDiscountDays;
    public String editorPaymentDiscountDaysTooltip;
    public String editorPaymentNetdaysTooltip;
    public String editorPaymentPaidName;
    public String editorPaymentPaidTooltip;
    public String editorPaymentUnpaidName;
    public String editorPaymentUnpaidTooltip;
    public String editorPaymentPlaceholderInfo;
    public String editorPaymentDefaultTooltip;
    public String editorPaymentDefaultButtonName;
    public String editorPaymentDefaultButtonHint;
    public String editorPaymentDepositName;
    public String editorPaymentDepositTooltip;
    
	public String ProductEditor516;
	public String ProductEditor517;
	public String ProductEditor519;
	public String ProductEditor520;
	public String ProductEditor521;
	public String ProductEditor522;
	public String ProductEditor523;
	public String ProductEditor524;
	public String ProductEditor525;
	public String ProductEditor526;
	public String ProductEditor527;
	public String ProductEditor528;

    public String dialogProductPicturePreview;

	public String preferencesProductUseitemno;
	public String preferencesProductUsequantityunit;
	public String preferencesProductUsedescription;
	public String preferencesProductNetorgrossprices;
	public String preferencesProductNetandgross;
	public String preferencesProductScaledprices;
	public String preferencesProductSelectvat;
	public String preferencesProductUseweight;
	public String preferencesProductUsequantity;
	public String preferencesProductUsepicture;
	public String preferencesProduct;

	public String ReceiptVoucherEditor642;
	public String receiptvoucherFieldBook;

    public String dialogMessageboxTitleWarning;
	public String ReorganizeDocumentsAction118;
	public String ReorganizeDocumentsAction119;

	public String commonLabelSearchfield;

	public String productFieldItemno;

    public String editorShippingTitle;
    public String editorShippingNameTooltip;
    public String editorShippingCategoryTooltip;
    public String editorShippingFieldAutovatcalculationName;
    public String editorShippingFieldAutovatcalculationTooltip;
    public String editorShippingFieldAutovatConstantName;
    public String editorShippingFieldAutovatFromvalueGross;
    public String editorShippingFieldAutovatFromvalueNet;
    public String editorShippingDefaultTooltip;
    public String editorShippingDefaultButton;
    public String editorShippingDefaultButtonTooltip;

	public String TextEditor540;
	public String TextEditor541;
	public String TextEditor542;
	public String TextEditor543;
	public String TextEditor544;

	public String preferencesToolbarShowicon;
	public String preferencesToolbarIcons;

	public String topictreeAll;
	public String topictreeAllDocumentsTooltip;
	public String topictreeAllCustomersTooltip;
	public String topictreeTransaction;
	public String topictreeLabelThistransaction;

    public String documentOrderStatePaid;
    public String documentOrderStateUnpaid;
    public String UniDataSetTableColumn282;
    public String documentOrderStateOpen;
    public String documentOrderStateInprogress;
    public String documentOrderStateShipped;
    public String documentOrderStateClosed;
    public String documentDeliveryStateHasinvoice;
    public String documentDeliveryStateHasnoinvoice;

	public String UpdateAction111;

	public String editorVatHeader;
	public String editorVatTitle;
	public String editorVatDefaultTooltip;
	public String editorVatCategoryTooltip;
	public String editorVatDescriptionTooltip;
	public String editorVatDefaultbutton;
	public String editorVatDefaultbuttonTooltip;
	public String editorVatNameTooltip;

	public String commonFieldNumber;

    public String commonFieldPrinted;
    public String commonFieldState;

	public String commonFieldDiscount;  // TODO evtl. "rebate" ???
	public String commonFieldDiscountDays;
	public String commonFieldNetDays;

	public String ViewReceiptVoucherTable292;

	public String commonLabelDefault;

	public String receiptvoucherFieldVoucher;
	public String commonFieldDocument;
	public String receiptvoucherFieldSupplier;
	public String receiptvoucherFieldState;

	public String VoucherEditor545;
	public String VoucherEditor546;
	public String VoucherEditor547;
	public String VoucherEditor548;
	public String VoucherEditor549;
    public String commonFieldAccount;
    public String VoucherEditor551;
	public String VoucherEditor552;
	public String VoucherEditor553;
	public String VoucherEditor554;
	public String VoucherEditor555;
	public String VoucherEditor556;
	public String VoucherEditor557;
	public String VoucherEditor558;
	public String VoucherEditor559;
	public String VoucherEditor560;
	public String VoucherEditor561;
	public String VoucherEditor562;
	public String VoucherEditor563;
	public String VoucherEditor564;
	public String VoucherEditor565;
	public String VoucherEditor566;

    public String preferencesWebshopAuthorizationPasswordproteced;
    public String preferencesWebshopAuthorization;

	public String importWebshopActionError;

    public String importWebshopErrorUrlnotset;
    public String importWebshopInfoConnection;
    public String importWebshopInfoConnected;
    public String importWebshopErrorCantconnect;
    public String importWebshopErrorCantread;
    public String importWebshopInfoLoading;
    public String importWebshopErrorNodata;
    public String importWebshopErrorCantopen;
    public String importWebshopDataCashondelivery;
    public String importWebshopDataPrepayment;
    public String importWebshopDataCreditcard;
    public String importWebshopDataCheque;
    public String importWebshopErrorMalformedurl;
    public String importWebshopErrorCantopenpicture;
    public String importWebshopInfoWebshopno;
    public String importWebshopInfoTotalsum;
    public String importWebshopErrorTotalsumincorrect;
    public String importWebshopErrorTotalsumcheckit;
    public String importWebshopInfoLoadingpicture;
    public String importWebshopInfoImportorders;
    public String importWebshopErrorCantconvertnumber;
    public String importWebshopInfoSuccess;
    public String importWebshopActionLabel;

    public String preferencesWebshopEnabled;
    public String preferencesWebshopUrl;
    public String preferencesWebshopUser;
    public String preferencesWebshopPassword;
    public String preferencesWebshopLabelProductsincategory;
    public String preferencesWebshopLabelCustomersincategory;
    public String preferencesWebshopLabelShippingsincategory;
    public String preferencesWebshopNoifycustomerOnprogress;
    public String preferencesWebshopNoifycustomerOnshipped;
    public String preferencesWebshopMaxproducts;
    public String preferencesWebshopModifiedproducts;
    public String preferencesWebshopEanasitemno;
    public String preferencesWebshopImport;
    public String preferencesWebshopDefaulturl;
    public String preferencesWebshop;
    public String preferencesWebshopCustomer;

	public String preferencesYourcompany;
	public String preferencesYourcompanyName;
	public String preferencesYourcompanyOwner;
	public String preferencesYourcompanyStreet;
	public String preferencesYourcompanyVatno;
	public String preferencesYourcompanyTaxoffice;
	public String preferencesYourcompanyBankaccount;
	public String preferencesYourcompanyBankname;
	public String preferencesYourcompanyCreditorid;

	public String aboutText;

	public String applicationName;

	public String commandBrowserCommand;
	public String commandBrowserName;
	public String commandBrowserTooltip;
	public String commandCalculatorName;
	public String commandCalculatorTooltip;
	public String commandContactsName;
	public String commandContactsTooltip;
	public String commandDataName;
	public String commandDataNameTooltip;
	public String commandDocumentsName;
	public String commandDocumentsTooltip;
	public String commandExpenditurevouchersName;
	public String commandExpenditurevouchersTooltip;
	public String commandListsName;
	public String commandListsTooltip;
	public String commandNavigationImport;
	public String commandNavigationImportTooltip;
	public String commandNavigationMisc;
	public String commandNavigationMiscTooltip;
	public String commandNewConfirmationName;
	public String commandNewContactName;
	public String commandNewContactTooltip;
	public String commandNewCreditName;
	public String commandNewDeliveryName;
	public String commandNewDeliverynoteName;
	public String commandNewDocumentCredit;
	public String commandNewDocumentDunning;
	public String commandNewDocumentProforma;
	public String commandNewDunningName;
	public String commandNewExpenditureName;
	public String commandNewExpenditureTooltip;
	public String commandNewInvoiceName;
	public String commandNewLetterName;
	public String commandNewName;
	public String commandNewNameTooltip;
	public String commandNewOfferName;
	public String commandNewOrderName;
	public String commandNewProductName;
	public String commandNewProductTooltip;
	public String commandNewReceiptName;
	public String commandNewTooltip;
	public String commandOpenContactTooltip;
	public String commandOpenParcelName;
	public String commandOpenWwwName;
	public String commandParcelserviceName;
	public String commandParcelserviceTooltip;
	public String commandPaymentsName;
	public String commandPaymentsTooltip;
	public String commandProductsName;
	public String commandProductsTooltip;
	public String commandReceiptvouchersName;
	public String commandReceiptvouchersTooltip;
	public String commandSelectoldworkspaceName;
	public String commandSelectworkspaceName;
	public String commandSelectworkspaceTooltip;
	public String commandShippingsName;
	public String commandShippingsTooltip;
	public String commandTextsName;
	public String commandTextsTooltip;
	public String commandVatsName;
	public String commandVatsTooltip;
	public String commandWebshopName;
	public String commandWebshopTooltip;

	public String configWorkspaceTemplatesName;

	public String dialogMessageboxTitleInfo;

	public String introRoot;

	public String mainMenuData;
	public String mainMenuDataTooltip;
	public String mainMenuEdit;
	public String mainMenuEditDeleteName;
	public String mainMenuEditDeleteTooltip;
	public String mainMenuEditMarkaspaid;
	public String mainMenuEditMarkaspending;
	public String mainMenuEditMarkasprocessing;
	public String mainMenuEditMarkasshipped;
	public String mainMenuEditMarkasunpaid;
	public String mainMenuExtra;
	public String mainMenuExtraCalculator;
	public String mainMenuExtraReorganize;
	public String mainMenuFile;
	public String mainMenuFileClose;
	public String mainMenuFileCloseall;
	public String mainMenuFileExit;
	public String mainMenuFileExitQuestion;
	public String mainMenuFileExport;
	public String mainMenuFileImport;
	public String mainMenuFileOpenpreferences;
	public String mainMenuFilePrint;
	public String mainMenuFilePrintTooltip;
	public String mainMenuFilePrintTooltipDeprecated;
	public String mainMenuFileSave;
	public String mainMenuFileSaveall;
	public String mainMenuHelp;
	public String mainMenuHelpAbout;
	public String mainMenuHelpContents;
	public String mainMenuHelpDynamic;
	public String mainMenuHelpInstall;
	public String mainMenuHelpIntro;
	public String mainMenuHelpReset;
	public String mainMenuHelpSearch;
	public String mainMenuNew;
	public String mainMenuNewConfirmation;
	public String mainMenuNewCredit;
	public String mainMenuNewContactName;
	public String mainMenuNewDeliverynote;
	public String mainMenuNewDunning;
	public String mainMenuNewExpenditurevoucher;
	public String mainMenuNewInvoice;
	public String mainMenuNewLetter;
	public String mainMenuNewListentry;
	public String mainMenuNewListentryTooltip;
	public String mainMenuNewOffer;
	public String mainMenuNewOrder;
	public String mainMenuNewPayment;
	public String mainMenuNewPaymentTooltip;
	public String mainMenuNewProforma;
    public String mainMenuNewProductName;
	public String mainMenuNewReceiptvoucher;
	public String mainMenuNewReceiptvoucherTooltip;
	public String mainMenuNewShipping;
	public String mainMenuNewShippingTooltip;
	public String mainMenuNewText;
	public String mainMenuNewTextTooltip;
	public String mainMenuNewTooltip;
	public String mainMenuNewVat;
	public String mainMenuNewVatTooltip;
	public String mainMenuWindow;

	public String pageBrowser;
//	public String pageColumnwidth;
//	public String pageColumnwidthcontacts;
//	public String pageColumnwidthdialogcontacts;
//	public String pageColumnwidthdialogproducts;
//	public String pageColumnwidthdialogtexts;
//	public String pageColumnwidthdocuments;
//	public String pageColumnwidthitems;
//	public String pageColumnwidthlist;
//	public String pageColumnwidthpayments;
//	public String pageColumnwidthproducts;
//	public String pageColumnwidthshippings;
//	public String pageColumnwidthtexts;
//	public String pageColumnwidthvat;
//	public String pageColumnwidthvoucheritems;
//	public String pageColumnwidthvouchers;
	public String pageCompany;
	public String pageContacts;
	public String pageContactsFormat;
	public String pageDocuments;
	public String pageExport;
	public String pageGeneral;
	public String pageNumberrange;
	public String pageNumberrangeFormat;
	public String pageOffice;
	public String pageOpenoffice;
	public String pageOptionalitems;
	public String pageParcelservice;
	public String pageProducts;
	public String pageToolbar;
	public String pageWebshop;
	public String pageWebshopauthorization;

	public String partdescDocview;

	public String partsContacts;
	public String partsDocuments;

	public String startFirstTitle;
	public String startFirstRestartmessage;
	public String startFirstSelectDbCredentialsJdbc;
	public String startFirstSelectDbCredentialsName;
	public String startFirstSelectDbCredentialsPassword;
	public String startFirstSelectDbCredentialsUser;
	public String startFirstSelectOldworkdirShort;
	public String startFirstSelectOldworkdirVerbose;
	public String startFirstSelectWorkdir;
	public String startFirstSelectWorkdirShort;
	public String startFirstSelectWorkdirVerbose;
	public String startMigrationBegin;
	public String startMigrationConvert;
	public String startMigrationEnd;
	public String startMigration;
	public String startMigrationWorking;
	
	
	public String toolbarNewConfirmationName;
	public String toolbarNewContactName;
	public String toolbarNewCreditName;
	public String toolbarNewDeliveryName;
	public String toolbarNewDeliverynoteName;
	public String toolbarNewDocumentCreditName;
	public String toolbarNewDocumentDunningName;
	public String toolbarNewDocumentProformaName;
	public String toolbarNewExpenditurevoucherName;
	public String toolbarNewInvoiceName;
	public String toolbarNewLetterName;
	public String toolbarNewOfferName;
	public String toolbarNewOrderName;
	public String toolbarNewProductName;
	public String toolbarNewReceiptvoucherName;
	
	public String wizardExportAccountsDescription;
	public String wizardExportAccountsName;
	public String wizardExportBuyersDescription;
	public String wizardExportBuyersName;
	public String wizardExportContactsDescription;
	public String wizardExportContactsName;
	public String wizardExportCsvContactsDescription;
	public String wizardExportCsvContactsName;
	public String wizardExportCsvListsCategory;
	public String wizardExportCsvProductsDescription;
	public String wizardExportCsvProductsName;
	public String wizardExportExpenditurevouchersDescription;
	public String wizardExportExpenditurevouchersName;
	public String wizardExportListsCategory;
	public String wizardExportProductandbuyersCategory;
	public String wizardExportProductbuyersDescription;
	public String wizardExportProductbuyersName;
	public String wizardExportProductsDescription;
	public String wizardExportProductsName;
	public String wizardExportReceiptvouchersDescription;
	public String wizardExportReceiptvouchersName;
	public String wizardExportSalesDescription;
	public String wizardExportSalesName;
	public String wizardExportSalesUnpaidDescription;
	public String wizardExportSalesUnpaidName;
	public String wizardExportSalesandvouchersCategory;
	public String wizardExportVcfContactsDescription;
	public String wizardExportVcfContactsName;
	public String wizardImportCsvContactsDescription;
	public String wizardImportCsvContactsName;
	public String wizardImportCsvExpendituresDescription;
	public String wizardImportCsvExpendituresName;
	public String wizardImportCsvProductsDescription;
	public String wizardImportCsvProductsName;
    
    public String getPurchaseTaxString() {
          //T: Name of the tax that is raised when goods are purchased
          return dataVatPurchasetax;
      }

      public String getSalesTaxString() {
          //T: Name of the tax that is raised when goods are sold
          return dataVatSalestax;
      }

    /**
     * This method helps you to create messages from compound keys, i.e. if you
     * try to use "some.key"+".tooltip". Don't use it for simple known keys
     * because this method uses reflection for finding the appropriate string
     * representation.
     * 
     * @param key
     *            the key which has to be looked up
     * @return message or (if not found) the key itself (enclosed in "!")
     */
    public String getMessageFromKey(final String key) {
        if(key == null) {
            return "";
        }
		String retval = key.replaceAll("-", ".");
		// try to make the string representation of this key via reflection

		try {
			Class<?> c = this.getClass();
			// make the key java-like :-)
			StringBuffer sb = new StringBuffer();
			StrTokenizer st = new StrTokenizer(retval, ".");
			while (st.hasNext()) {
				if(st.previousIndex() == -1) {
					sb.append(st.next());
					continue;
				}
				sb.append(StringUtils.capitalize(st.next()));
			}

			Field chap = c.getDeclaredField(sb.toString());
			retval = (String) chap.get(this);
		}
		catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			retval = "?" + retval + "?";
		}
		return retval;
	}

}
