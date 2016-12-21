/**
 * 
 */
package com.sebulli.fakturama.misc;

import java.util.Locale;

import org.eclipse.core.runtime.preferences.InstanceScope;


/**
 * Common constants for the Fakturama project.
 *
 */
public class Constants {
    public static final String DEFAULT_PREFERENCES_NODE="/" + InstanceScope.SCOPE + "/default/com.sebulli.fakturama.rcp";
    
    public static final String VIEWTABLE_PREFERENCES_FILE = "fakturamaviews.properties";
    public static final String GENERAL_WORKSPACE = "GENERAL_WORKSPACE";
        
    public static final String PREFERENCES_GENERAL_HAS_THOUSANDS_SEPARATOR = "GENERAL_HAS_THOUSANDS_SEPARATOR";
    public static final String PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES = "GENERAL_CURRENCY_DECIMALPLACES";
    public static final String PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES = "GENERAL_QUANTITY_DECIMALPLACES";
    public static final String PREFERENCES_GENERAL_COLLAPSE_EXPANDBAR = "GENERAL_COLLAPSE_EXPANDBAR";
    public static final String PREFERENCES_GENERAL_CLOSE_OTHER_EDITORS = "GENERAL_CLOSE_OTHER_EDITORS";
    public static final String PREFERENCE_GENERAL_CURRENCY = "GENERAL_CURRENCY";
    
    /**
     * Path to product pictures (relative to Workspace path!)
     */
    public static final String PRODUCT_PICTURE_FOLDER = "/Pics/Products/";

    
    /**
     * Default identifier for VAT
     */
    public static final String DEFAULT_VAT = "standardvat";
    
    /**
     * Default identifier for Shipping
     */
    public static final String DEFAULT_SHIPPING = "standardshipping";
    
    /**
     * Default identifier for Payment
     */
    public static final String DEFAULT_PAYMENT = "standardpayment";
    
    /**
     * The following constants are for hiding and showing the appropriate tool icons within the tool bar
     */
    public static final String TOOLBAR_SHOW_OPEN_CALCULATOR = "TOOLBAR_SHOW_OPEN_CALCULATOR";
    public static final String TOOLBAR_SHOW_OPEN_BROWSER = "TOOLBAR_SHOW_OPEN_BROWSER";
    public static final String TOOLBAR_SHOW_OPEN_PARCELSERVICE = "TOOLBAR_SHOW_OPEN_PARCELSERVICE";
    public static final String TOOLBAR_SHOW_NEW_RECEIPTVOUCHER = "TOOLBAR_SHOW_NEW_RECEIPTVOUCHER";
    public static final String TOOLBAR_SHOW_NEW_EXPENDITUREVOUCHER = "TOOLBAR_SHOW_NEW_EXPENDITUREVOUCHER";
    public static final String TOOLBAR_SHOW_NEW_CONTACT = "TOOLBAR_SHOW_NEW_CONTACT";
    public static final String TOOLBAR_SHOW_NEW_PRODUCT = "TOOLBAR_SHOW_NEW_PRODUCT";
    
    public static final String TOOLBAR_SHOW_DOCUMENT_NEW_LETTER = "TOOLBAR_SHOW_DOCUMENT_NEW_LETTER";
    public static final String TOOLBAR_SHOW_DOCUMENT_NEW_OFFER = "TOOLBAR_SHOW_DOCUMENT_NEW_OFFER";
    public static final String TOOLBAR_SHOW_DOCUMENT_NEW_ORDER = "TOOLBAR_SHOW_DOCUMENT_NEW_ORDER";
    public static final String TOOLBAR_SHOW_DOCUMENT_NEW_CONFIRMATION = "TOOLBAR_SHOW_DOCUMENT_NEW_CONFIRMATION";
    public static final String TOOLBAR_SHOW_DOCUMENT_NEW_CREDIT = "TOOLBAR_SHOW_DOCUMENT_NEW_CREDIT";
    public static final String TOOLBAR_SHOW_DOCUMENT_NEW_DELIVERY = "TOOLBAR_SHOW_DOCUMENT_NEW_DELIVERY";
    public static final String TOOLBAR_SHOW_DOCUMENT_NEW_DUNNING = "TOOLBAR_SHOW_DOCUMENT_NEW_DUNNING";
    public static final String TOOLBAR_SHOW_DOCUMENT_NEW_INVOICE = "TOOLBAR_SHOW_DOCUMENT_NEW_INVOICE";
    public static final String TOOLBAR_SHOW_DOCUMENT_NEW_PROFORMA = "TOOLBAR_SHOW_DOCUMENT_NEW_PROFORMA";
    
    public static final String TOOLBAR_SHOW_SAVE = "TOOLBAR_SHOW_SAVE";
    public static final String TOOLBAR_SHOW_PRINT = "TOOLBAR_SHOW_PRINT";
    public static final String TOOLBAR_SHOW_WEBSHOP = "TOOLBAR_SHOW_WEBSHOP";
    
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_USE_EAN_AS_ITEMNR = "WEBSHOP_USE_EAN_AS_ITEMNR";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS = "WEBSHOP_ONLY_MODIFIED_PRODUCTS";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_MAX_PRODUCTS = "WEBSHOP_MAX_PRODUCTS";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_NOTIFY_SHIPPED = "WEBSHOP_NOTIFY_SHIPPED";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_NOTIFY_PROCESSING = "WEBSHOP_NOTIFY_PROCESSING";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_SHIPPING_CATEGORY = "WEBSHOP_SHIPPING_CATEGORY";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_CONTACT_CATEGORY = "WEBSHOP_CONTACT_CATEGORY";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_PRODUCT_CATEGORY = "WEBSHOP_PRODUCT_CATEGORY";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_PASSWORD = "WEBSHOP_PASSWORD";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_USER = "WEBSHOP_USER";
    /**
     * 
     */
    public static final String PREFERENCES_WEBSHOP_URL = "WEBSHOP_URL";

    /**
     * Preference for enabling / disabling webshop import
     */
    public static final String PREFERENCES_WEBSHOP_ENABLED = "WEBSHOP_ENABLED";

	/**
	 * 
	 */
	public static final String PREFERENCES_CONTACT_FORMAT_HIDE_COUNTRIES = "CONTACT_FORMAT_HIDE_COUNTRIES";

	/**
	 * 
	 */
	public static final String PREFERENCES_CONTACT_FORMAT_ADDRESS = "CONTACT_FORMAT_ADDRESS";

	/**
	 * 
	 */
	public static final String PREFERENCES_CONTACT_FORMAT_GREETING_COMPANY = "CONTACT_FORMAT_GREETING_COMPANY";

	/**
	 * 
	 */
	public static final String PREFERENCES_CONTACT_FORMAT_GREETING_MS = "CONTACT_FORMAT_GREETING_MS";

	/**
	 * 
	 */
	public static final String PREFERENCES_CONTACT_FORMAT_GREETING_MR = "CONTACT_FORMAT_GREETING_MR";

	/**
	 * 
	 */
	public static final String PREFERENCES_CONTACT_FORMAT_GREETING_COMMON = "CONTACT_FORMAT_GREETING_COMMON";

	/**
	 * 
	 */
	public static final String PREFERENCES_WEBSHOP_AUTHORIZATION_ENABLED = "WEBSHOP_AUTHORIZATION_ENABLED";

	/**
	 * 
	 */
	public static final String PREFERENCES_WEBSHOP_AUTHORIZATION_PASSWORD = "WEBSHOP_AUTHORIZATION_PASSWORD";

	/**
	 * 
	 */
	public static final String PREFERENCES_WEBSHOP_AUTHORIZATION_USER = "WEBSHOP_AUTHORIZATION_USER";

    /**
     * 
     */
    public static final String PREFERENCES_PRODUCT_USE_PICTURE = "PRODUCT_USE_PICTURE";

    /**
     * 
     */
    public static final String PREFERENCES_PRODUCT_USE_QUANTITY = "PRODUCT_USE_QUANTITY";

    /**
     * 
     */
    public static final String PREFERENCES_PRODUCT_USE_WEIGHT = "PRODUCT_USE_WEIGHT";

    /**
     * 
     */
    public static final String PREFERENCES_PRODUCT_USE_VAT = "PRODUCT_USE_VAT";

    /**
     * 
     */
    public static final String PREFERENCES_PRODUCT_SCALED_PRICES = "PRODUCT_SCALED_PRICES";

    /**
     * 
     */
    public static final String PREFERENCES_PRODUCT_USE_NET_GROSS = "PRODUCT_USE_NET_GROSS";

    /**
     * 
     */
    public static final String PREFERENCES_PRODUCT_USE_DESCRIPTION = "PRODUCT_USE_DESCRIPTION";

    /**
     * 
     */
    public static final String PREFERENCES_PRODUCT_USE_QUNIT = "PRODUCT_USE_QUNIT";

    /**
     * 
     */
    public static final String PREFERENCES_PRODUCT_USE_ITEMNR = "PRODUCT_USE_ITEMNR";

    /**
     * The {@link Locale} for currency (might be different from application locale!)
     */
    public static final String PREFERENCE_CURRENCY_LOCALE = "PREFERENCE_CURRENCY_LOCALE";

    /**
     * Use cash rounding (Switzerland only). This is for rounding to full 0.05 SFr.
     */
    public static final String PREFERENCES_CURRENCY_USE_CASHROUNDING = "CURRENCY_USE_CASHROUNDING";

    /**
     * This preference is read-only and only used for an example string (formatting of a currency amount)
     */
    public static final String PREFERENCE_CURRENCY_FORMAT_EXAMPLE = "CURRENCY_FORMAT_EXAMPLE";

    /**
     * Use currency symbol or ISO code (3-letter-code)
     */
    public static final String PREFERENCES_CURRENCY_USE_SYMBOL = "CURRENCY_USE_SYMBOL";

    /**
     * 
     */
    public static final String PREFERENCES_FINALPAYMENT_TEXT = "DOCUMENT_FINALPAYMENT_TEXT";

    /**
     * 
     */
    public static final String PREFERENCES_DEPOSIT_TEXT = "DOCUMENT_DEPOSIT_TEXT";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_MESSAGES = "DOCUMENT_MESSAGES";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD = "DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_DIALOG = "DOCUMENT_CUSTOMER_STATISTICS_DIALOG";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE = "DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_DELIVERY_NOTE_ITEMS_WITH_PRICE = "DOCUMENT_DELIVERY_NOTE_ITEMS_WITH_PRICE";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS = "DOCUMENT_USE_DISCOUNT_ALL_ITEMS";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_USE_DISCOUNT_EACH_ITEM = "DOCUMENT_USE_DISCOUNT_EACH_ITEM";
	public static final String PREFERENCES_DOCUMENT_USE_VESTINGPERIOD = "DOCUMENT_USE_VESTINGPERIOD";

    /**
     * 
     */
//    public static final String PREFERENCES_DOCUMENT_USE_ITEM_POS = "DOCUMENT_USE_ITEM_POS";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_USE_PREVIEW_PICTURE = "DOCUMENT_USE_PREVIEW_PICTURE";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG = "DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_COPY_MESSAGE_FROM_PARENT = "DOCUMENT_COPY_MESSAGE_FROM_PARENT";

    /**
     * 
     */
    public static final String PREFERENCES_DOCUMENT_USE_NET_GROSS = "DOCUMENT_USE_NET_GROSS";

    /**
     * 
     */
    public static final String PREFERENCES_OPTIONALITEMS_OPTIONALITEM_TEXT = "OPTIONALITEMS_OPTIONALITEM_TEXT";

    /**
     * 
     */
    public static final String PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT = "OPTIONALITEMS_PRICE_REPLACEMENT";

    /**
     * 
     */
    public static final String PREFERENCES_OPTIONALITEMS_REPLACE_PRICE = "OPTIONALITEMS_REPLACE_PRICE";

    /**
     * 
     */
    public static final String PREFERENCES_OPTIONALITEMS_USE = "OPTIONALITEMS_USE";

    /**
     * 
     */
    public static final String PREFERENCES_BROWSER_SHOW_URL_BAR = "BROWSER_SHOW_URL_BAR";

    /**
     * 
     */
    public static final String PREFERENCES_BROWSER_TYPE = "BROWSER_TYPE";

    /**
     * 
     */
    public static final String PREFERENCES_GENERAL_WEBBROWSER_URL = "GENERAL_WEBBROWSER_URL";

    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_COUNTRY = "CONTACT_USE_COUNTRY";

    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_COMPANY = "CONTACT_USE_COMPANY";

    /** The Constant PREFERENCES_CONTACT_NAME_FORMAT. */
    public static final String PREFERENCES_CONTACT_NAME_FORMAT = "CONTACT_NAME_FORMAT";
    
    public static final int CONTACT_FORMAT_FIRSTNAME_LASTNAME = 0;
    public static final int CONTACT_FORMAT_LASTNAME_FIRSTNAME = 1;

    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_TITLE = "CONTACT_USE_TITLE";

    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_GENDER = "CONTACT_USE_GENDER";

    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_NOTE = "CONTACT_USE_NOTE";

    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_MISC = "CONTACT_USE_MISC";

    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_BANK = "CONTACT_USE_BANK";

    /**
     * 
     */
    public static final String PREFERENCES_CONTACT_USE_DELIVERY = "CONTACT_USE_DELIVERY";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_STREET = "YOURCOMPANY_COMPANY_STREET";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_COUNTRY = "YOURCOMPANY_COMPANY_COUNTRY";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_TEL = "YOURCOMPANY_COMPANY_TEL";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_FAX = "YOURCOMPANY_COMPANY_FAX";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_EMAIL = "YOURCOMPANY_COMPANY_EMAIL";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_WEBSITE = "YOURCOMPANY_COMPANY_WEBSITE";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_VATNR = "YOURCOMPANY_COMPANY_VATNR";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_TAXOFFICE = "YOURCOMPANY_COMPANY_TAXOFFICE";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_BANK = "YOURCOMPANY_COMPANY_BANK";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_IBAN = "YOURCOMPANY_COMPANY_IBAN";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_BIC = "YOURCOMPANY_COMPANY_BIC";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_CREDITORID = "YOURCOMPANY_CREDITORID";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_CITY = "YOURCOMPANY_COMPANY_CITY";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_ZIP = "YOURCOMPANY_COMPANY_ZIP";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_OWNER = "YOURCOMPANY_COMPANY_OWNER";

    /**
     * 
     */
    public static final String PREFERENCES_YOURCOMPANY_COMPANY_NAME = "YOURCOMPANY_COMPANY_NAME";

    /**
     * 
     */
    public static final String PREFERENCES_OPENOFFICE_START_IN_NEW_THREAD = "OPENOFFICE_START_IN_NEW_THREAD";

    /**
     * 
     */
    public static final String PREFERENCES_OPENOFFICE_PDF_PATH_FORMAT = "OPENOFFICE_PDF_PATH_FORMAT";

	/** The Constant PREFERENCES_ADDITIONAL_OPENOFFICE_PDF_PATH_FORMAT. */
	public static final String PREFERENCES_ADDITIONAL_OPENOFFICE_PDF_PATH_FORMAT = "OPENOFFICE_ADDITIONAL_PDF_PATH_FORMAT";

    /**
     * 
     */
    public static final String PREFERENCES_OPENOFFICE_ODT_PATH_FORMAT = "OPENOFFICE_ODT_PATH_FORMAT";

    /**
     * 
     */
    public static final String PREFERENCES_OPENOFFICE_ODT_PDF = "OPENOFFICE_ODT_PDF";

    /**
     * 
     */
    public static final String PREFERENCES_OPENOFFICE_PATH = "OPENOFFICE_PATH";

	public static final String PROPERTY_CONTACTS_CLICKHANDLER = "fakturama.datatable.contacts.clickhandler";

	public static final String PROPERTY_PRODUCTS_CLICKHANDLER = "fakturama.datatable.products.clickhandler";
	public static final String PROPERTY_TEXTMODULES_CLICKHANDLER = "fakturama.datatable.texts.clickhandler";
	public static final String PROPERTY_DELIVERIES_CLICKHANDLER = "fakturama.datatable.deliveries.clickhandler";

	public static final String COMMAND_SELECTITEM = "com.sebulli.fakturama.command.selectitem";

	public static final int RC_OK = 0;

	public static final String WEBSHOP_NAMESPACE = "http://www.fakturama.org";

	public final static String PARAM_START_DATE = "startdate";

	public final static String PARAM_END_DATE = "enddate";

	/**
	 * 
	 */
	public static final String PREFERENCES_EXPORTSALES_PAIDDATE = "EXPORTSALES_PAIDDATE";

	/** The Constant CONTEXT_VATVALUE. */
	public static final String CONTEXT_VATVALUE = "vatvalue";

	/** The Constant CONTEXT_NETVALUE. */
	public static final String CONTEXT_NETVALUE = "netvalue";

	/** The Constant CONTEXT_STYLE. */
	public static final String CONTEXT_STYLE = "style";

	/** The Constant CONTEXT_CANVAS. */
	public static final String CONTEXT_CANVAS = "canvas";

	/** The Constant DETAILPANEL_ID. */
	public static final String DETAILPANEL_ID = "com.sebulli.fakturama.rcp.detailpanel";

	public static final String PREFERENCES_OPENPDF = "OPENPDF";

}
