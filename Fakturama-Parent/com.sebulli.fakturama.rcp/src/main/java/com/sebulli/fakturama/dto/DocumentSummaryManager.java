/**
 * 
 */
package com.sebulli.fakturama.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Document;

/**
 * Manager for {@link DocumentSummary} and {@link VatSummarySet} of various
 * {@link Document}s.
 *
 */
public class DocumentSummaryManager {

	private DocumentSummaryCalculator calculator;

	private Map<Document, DocumentSummary> summaryMap = new HashMap<>();
	protected IPreferenceStore defaultValuePrefs;

	@Inject
	public DocumentSummaryManager(IEclipseContext context, IPreferenceStore defaultValuePrefs) {
		this.calculator = ContextInjectionFactory.make(DocumentSummaryCalculator.class, context);
		this.defaultValuePrefs = defaultValuePrefs;
		calculator
				.setUseSET(this.defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX));
	}
	
	public DocumentSummary calculate(Document document) {
		DocumentSummary summary = calculator.calculate(document);
		summaryMap.put(document, summary);
		return summary;
	}

	public DocumentSummary calculate(Document document, DocumentSummaryParam param) {
		DocumentSummary summary = calculator.calculate(param);
		summaryMap.put(document, summary);
		return summary;
	}

	/**
	 * Add a {@link Document} to the calculation.
	 * 
	 * @param document
	 */
	public void addDocument(Document document) {
		calculator.calculate(document);
	}

	/**
	 * Get calculation results for the given document.
	 * 
	 * @param document
	 * @return
	 */
	public DocumentSummary getDocumentSummary(Document document) {
		if (summaryMap.get(document) == null) {
			calculator.calculate(document);
		}
		return summaryMap.get(document);
	}
	
	public int getCalculatedSummarySets() {
		return summaryMap.size();
	}

	public VatSummarySet getVatSummary(Document document) {
		DocumentSummary documentSummary = getDocumentSummary(document);
		return documentSummary != null ? documentSummary.getVatSummary() : null;
	}

	/**
	 * Get the {@link VatSummaryItem}s for a given tax value. If more than one tax value exists,
	 * all items with the same tax value are returned.
	 * 
	 * @param taxValue
	 * @return
	 */
	public List<VatSummaryItem> getVatSummaryItemForTaxValue(double taxValue) {
		List<VatSummaryItem> vatSummaryItems = null;
		for (DocumentSummary docSumm : summaryMap.values()) {
			vatSummaryItems = docSumm.getVatSummary()
					.stream().filter(v -> v.getVatPercent() == taxValue)
					.collect(Collectors.toList());
		}
		return vatSummaryItems;
	}

	/**
	 * Clear all previously collected {@link Document}s and calculation results,
	 */
	public void reset() {
		summaryMap.clear();
	}

}
