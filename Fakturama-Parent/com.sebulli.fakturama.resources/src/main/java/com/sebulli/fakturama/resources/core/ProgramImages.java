/**
 * 
 */
package com.sebulli.fakturama.resources.core;

/**
 * contains all possible images that are used in Fakturama
 * (which aren't icons)
 */
public enum ProgramImages {
	NO_PICTURE("/icons/product/nopicture.png"),
	NOT_FOUND_PICTURE("/icons/product/picturenotfound.png"),
	
	// preview pictures for exporters
	EXPORT_CONTACTS_CSV("/icons/preview/contacts_csv.png"),
	EXPORT_CONTACTS("/icons/preview/contacts.png"),
	EXPORT_CONTACTS2("/icons/preview/contacts2.png"),
	EXPORT_CONTACTS_VCF("/icons/preview/export_vcf_contacts.png"),
	EXPORT_EXPENDITURES("/icons/preview/export_expenditures.png"),
	EXPORT_PRODUCT_BUYERS("/icons/preview/export_product_buyers.png"),
	EXPORT_SALES("/icons/preview/export_sales.png"),
	EXPORT_PRODUCTS("/icons/preview/products.png"),
	EXPORT_PRODUCTS_CSV("/icons/preview/products_csv.png"),
	EXPORT_PRODUCTS2("/icons/preview/products2.png"),

	;
	
	private String path;

	/**
	 * @param path
	 */
	private ProgramImages(String path) {
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	
}
