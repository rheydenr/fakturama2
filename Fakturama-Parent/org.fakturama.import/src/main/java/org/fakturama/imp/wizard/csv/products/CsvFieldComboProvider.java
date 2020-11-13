package org.fakturama.imp.wizard.csv.products;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;

import com.sebulli.fakturama.i18n.Messages;

public class CsvFieldComboProvider implements IComboBoxDataProvider {

    private  List<Pair<String, String>> allEntries = null;
    
    public CsvFieldComboProvider(Messages msg) {
            List<Pair<String, String>> retList = ProductBeanCSV.createProductsAttributeMap(msg)
                    .entrySet()
                    .stream()
                    .map(Pair::of)
                    .sorted(Comparator.comparing(Pair::getValue))
                    .collect(Collectors.toList());
            this.allEntries = retList;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider#getValues(int, int)
     */
    @Override
    public List<?> getValues(int columnIndex, int rowIndex) {
        return allEntries;
    }


}
