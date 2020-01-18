/**
 * 
 */
package com.sebulli.fakturama.model;

import org.eclipse.emf.texo.converter.ObjectCopier;

/**
 * Helper class for duplicating model objects. Since not all references have to
 * be duplicated (e.g., VAT or Shipping objects), the duplicating process has to
 * be more intelligent than only stupid copying attributes and references.
 *
 */
public class ObjectDuplicator {

	@SuppressWarnings("unchecked")
	public <T extends Document> T duplicateDocument(T document) {
		T clonedDocument = null;
		if (document != null) {
			ObjectCopier objectCopier = new ObjectCopier();
			objectCopier.setCopyChildren(true);
			objectCopier.setCopyReferences(true);
			clonedDocument = (T) objectCopier.copy(document);

			/*
			 * Modify some references. Note, that VAT, Payment and the like are entities
			 * which are always referenced only. But entities like DocumentReceiver or
			 * DocumentItem have to be created newly for each copy of a document. Therefore
			 * we iterate through all relevant entity collections and set their item's id to
			 * 0. This causes the Entity Manager to store the complete list as new entities.
			 */
			// VAT, Shipment, Payment can be left unchanged
			if (clonedDocument != null) {
				// reset some attributes
				clonedDocument.getAdditionalInfo().setId(0);
				clonedDocument.setInvoiceReference(null);
				clonedDocument.setSourceDocument(null);
				clonedDocument.setTransactionId(null);
				clonedDocument.setVersion(Integer.valueOf(1));

				// set DocumentReceiver to new
				clonedDocument.getReceiver().forEach(r -> r.setId(0));

				// set DocumentItems to new
				clonedDocument.getItems().forEach(r -> r.setId(0));

				// make the new object really "new" :-)
				clonedDocument.setId(0);
			}
		}
		return clonedDocument;
	}

	public Product duplicateProduct(Product product) {
		Product clonedProduct = null;
		if (product != null) {
			clonedProduct = product.clone();
			ObjectCopier objectCopier = new ObjectCopier();
			objectCopier.setCopyChildren(true);
			objectCopier.setCopyReferences(true);
			clonedProduct = (Product) objectCopier.copy(product);
			// TODO set options for the product to new ones
			clonedProduct.setId(0);
		}
		return clonedProduct;
	}

}
