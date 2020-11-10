package org.fakturama.imp.wizard.csv.products;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public class ProductImportMapping {
    /**
     * The name from CSV header
     */
    private String leftItem;
    
    /**
     * A {@link Map} which consists of field name and its corresponding I18N'ed qualifier.
     */
    private Pair<String, String> rightItem;

    public ProductImportMapping(String leftItem, Pair<String, String> rightItem) {
        this.leftItem = leftItem;
        this.rightItem = rightItem;
    }

    public String getLeftItem() {
        return leftItem;
    }

    public void setLeftItem(String leftItem) {
        this.leftItem = leftItem;
    }

    public Pair<String, String> getRightItem() {
        return rightItem;
    }

    public void setRightItem(Pair<String, String> rightItem) {
        this.rightItem = rightItem;
    }

    @Override
    public String toString() {
        return new StringBuffer("ProductImportMapping {").append(leftItem)
                .append(" -> ").append(rightItem != null ? rightItem.getKey() + " (" + rightItem.getValue() + ") " : "<null>").append("}").toString();
    }

    /**
     * Create a {@link ProductImportMapping} where only the key is set (value is <code>null</code>).
     * 
     * @param column the key to set
     * @return {@link ProductImportMapping}
     */
    public static ProductImportMapping ofNullValue(String column) {
        return new ProductImportMapping(column, null);
    }

}
