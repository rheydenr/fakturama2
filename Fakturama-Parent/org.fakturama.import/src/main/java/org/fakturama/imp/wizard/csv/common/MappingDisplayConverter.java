package org.fakturama.imp.wizard.csv.common;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;

public class MappingDisplayConverter extends DisplayConverter {

    @SuppressWarnings("unchecked")
    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        String retval = "";
        if(canonicalValue != null && canonicalValue instanceof Pair<?,?>) {
            retval = ((Pair<String, String>)canonicalValue).getValue();
        }
        return retval;
    }

    @Override
    public Object displayToCanonicalValue(Object displayValue) {
        return null; // not used
    }

}
