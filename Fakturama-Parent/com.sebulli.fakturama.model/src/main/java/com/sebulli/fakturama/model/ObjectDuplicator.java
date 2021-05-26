/**
 * 
 */
package com.sebulli.fakturama.model;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.texo.converter.ObjectCopier;
import org.eclipse.emf.texo.model.ModelObject;
import org.eclipse.emf.texo.model.ModelResolver;
import org.eclipse.emf.texo.model.ModelResolver.ModelDescriptor;

import com.ibm.icu.util.Calendar;

/**
 * Helper class for duplicating model objects. Since not all references have to
 * be duplicated (e.g., VAT or Shipping objects), the duplicating process has to
 * be more intelligent than only stupid copying attributes and references.
 *
 */
public class ObjectDuplicator {
    
    public void einTest(Document invoice) {
        
//        FakturamaModelPackage fakturamaModelPackage = FakturamaModelPackage.INSTANCE;
        ModelDescriptor modelDescriptor = ModelResolver.getInstance().getModelDescriptor(invoice.getClass(), true);
//        modelDescriptor.setModelPackage(fakturamaModelPackage);
//        EClassifier eClassifier = fakturamaModelPackage.getEPackage().getEClassifiers().get(FakturamaModelPackage.DOCUMENT_CLASSIFIER_ID);
//        modelDescriptor.setEClassifier(eClassifier);
//      productAttributes = ((EClass) FakturamaModelPackage.INSTANCE.getEPackage().getEClassifiers().get(FakturamaModelPackage.PRODUCT_CLASSIFIER_ID)) //
//      .getEAllAttributes().stream() //
//      .filter(f -> !f.isMany()) //
//      .sorted(Comparator.comparing(EStructuralFeature::getName))
//      .collect(Collectors.toList());
        
        ModelObject<?> adapter = modelDescriptor.createAdapter(invoice);
        EStructuralFeature eStructuralFeature = adapter.eClass().getEStructuralFeature(FakturamaModelPackage.INVOICE_CUSTOMERREF_FEATURE_ID);
        Object feat = adapter.eGet(eStructuralFeature);
        System.out.println(feat);
    }

	@SuppressWarnings("unchecked")
	public <T extends Document> T duplicateDocument(T document) {
		T clonedDocument = null;
		if (document != null) {
			ObjectCopier objectCopier = new ObjectCopier();
			objectCopier.setCopyChildren(true);
			objectCopier.setCopyReferences(true);
			clonedDocument = (T) objectCopier.copy(document);

			/*
			 * Modify some references. Note that VAT, Payment and the like are entities
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
				
				// set date to actual date
				clonedDocument.setDocumentDate(Calendar.getInstance().getTime());
				clonedDocument.setServiceDate(Calendar.getInstance().getTime());
				clonedDocument.setOrderDate(Calendar.getInstance().getTime());
				
				// reset paid values, if any
				clonedDocument.setPaid(Boolean.FALSE);
				clonedDocument.setPayDate(null);
				clonedDocument.setPaidValue(null);

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
