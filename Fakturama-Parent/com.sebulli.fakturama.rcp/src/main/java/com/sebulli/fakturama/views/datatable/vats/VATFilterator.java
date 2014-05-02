/**
 * 
 */
package com.sebulli.fakturama.views.datatable.vats;

import java.util.List;

import com.sebulli.fakturama.model.VAT;

import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.TextFilterator;

/**
 * 
 * This {@link Filterator} is for filtering {@link VAT}s in a list view.
 * 
 * @author rheydenr
 *
 */
public class VATFilterator implements TextFilterator<VAT> {

	/**
	 * Which elements are relevant for filtering?
	 */
	@Override
	public void getFilterStrings(List<String> baseList, VAT element) {
		baseList.add(element.getDescription());
		baseList.add(element.getName());
	}
    
    /* Achtung: hier kommt später noch transactionFilter und contactFilter
     * dazu. Hintergrund: Beim Filtern über Dokumente kann man eins in der Liste
     * auswählen und dann auf den entsprechenden Kontakt gehen (im Baum). Dann werden alle Dokumente
     * zu diesem Kontakt angezeigt.
     * 
     * Wichtig: Selektiert man einen Kontakt bzw. eine Transaktion in der Liste, muß der Baum auch
     * angepaßt werden! 
    */

}
