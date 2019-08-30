package com.sebulli.fakturama.dialogs;
import java.util.Comparator;
import java.util.List;

import com.sebulli.fakturama.dao.DebitorAddress;

import ca.odell.glazedlists.TreeList;

public class TreeItemFormat implements TreeList.Format<TreeItem<DebitorAddress>> {

    @Override
    public void getPath(List<TreeItem<DebitorAddress>> path, TreeItem<DebitorAddress> element) {
        if (element.getParent() != null) {
            path.add(element.getParent());
        }
        path.add(element);
    }

    @Override
    public boolean allowsChildren(TreeItem<DebitorAddress> element) {
        return element.hasChildren();
    }

    @Override
    public Comparator<? super TreeItem<DebitorAddress>> getComparator(int depth) {
        return (o1, o2) -> o1.getItem().getCustomerNumber().compareTo(o2.getItem().getCustomerNumber());
    }
}