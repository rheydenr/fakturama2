package com.sebulli.fakturama.parts.widget.contacttree;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sebulli.fakturama.dao.DebitorAddress;

import ca.odell.glazedlists.TreeList;

public class DebitorAddressTreeFormat<K extends DebitorAddress> implements TreeList.Format<K> {

    private Map<String, K> parentMapping = new HashMap<>();

    @Override
    public void getPath(List<K> path, K element) {
        if (this.parentMapping.get(element.getCustomerNumber()) != null) {
            path.add(this.parentMapping.get(element.getCustomerNumber()));
        } else {
            this.parentMapping.put(element.getCustomerNumber(), element);
        }
        path.add(element);
    }

    /**
     * Simply always return <code>true</code>.
     *
     * @return <code>true</code> if this element can have child elements, or
     *         <code>false</code> if it is always a leaf node.
     */
    @Override
    public boolean allowsChildren(K element) {
        return true;
    }

    @Override
    public Comparator<? super K> getComparator(int depth) {
        return (o1, o2) -> o1.getCustomerNumber().compareTo(o2.getCustomerNumber());
    }
}