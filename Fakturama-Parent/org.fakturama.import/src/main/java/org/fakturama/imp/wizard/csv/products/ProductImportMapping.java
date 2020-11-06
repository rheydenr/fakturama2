package org.fakturama.imp.wizard.csv.products;

import org.eclipse.emf.ecore.EStructuralFeature;

public class ProductImportMapping {
    private String leftItem;
    private EStructuralFeature rightItem;

    public ProductImportMapping(String leftItem, EStructuralFeature rightItem) {
        this.leftItem = leftItem;
        this.rightItem = rightItem;
    }

    public String getLeftItem() {
        return leftItem;
    }

    public void setLeftItem(String leftItem) {
        this.leftItem = leftItem;
    }

    public EStructuralFeature getRightItem() {
        return rightItem;
    }

    public void setRightItem(EStructuralFeature rightItem) {
        this.rightItem = rightItem;
    }

    @Override
    public String toString() {
        return new StringBuffer("ProductImportMapping {").append(leftItem)
                .append(" -> ").append(rightItem != null ? rightItem.getName() : "<null>").append("}").toString();
    }

}
