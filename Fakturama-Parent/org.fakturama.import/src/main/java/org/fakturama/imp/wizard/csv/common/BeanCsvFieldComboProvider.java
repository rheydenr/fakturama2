package org.fakturama.imp.wizard.csv.common;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;

public class BeanCsvFieldComboProvider implements IComboBoxDataProvider {
    public static final String EMPTY_ENTRY = "empty";

    private  List<Pair<String, String>> allEntries = null;
    
    public BeanCsvFieldComboProvider(Map<String, String> beanAttribs) {
            this.allEntries = beanAttribs
                    .entrySet()
                    .stream()
                    .map(Pair::of)
                    .sorted(Comparator.comparing(Pair::getValue))
                    .collect(Collectors.toList());
            this.allEntries.add(0, Pair.of("empty", "   "));
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider#getValues(int, int)
     */
    @Override
    public List<?> getValues(int columnIndex, int rowIndex) {
        return allEntries;
    }
}
