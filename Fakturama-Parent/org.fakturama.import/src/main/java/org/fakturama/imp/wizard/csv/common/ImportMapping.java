package org.fakturama.imp.wizard.csv.common;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public class ImportMapping {
    
    /**
     * The name from CSV header
     */
    private String leftItem;
    private int id;
    /**
     * A {@link Map} which consists of field name and its corresponding I18N'ed
     * qualifier.
     */
    private Pair<String, String> rightItem;

    public ImportMapping(String leftItem, Pair<String, String> rightItem) {
        this.leftItem = leftItem;
        this.rightItem = rightItem;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return new StringBuffer("ImportMapping {").append(leftItem).append(" -> ")
                .append(rightItem != null ? rightItem.getKey() + " (" + rightItem.getValue() + ") " : "<null>").append("}").toString();
    }

    /**
     * Create a {@link ImportMapping} where only the key is set (value is
     * <code>null</code>).
     * 
     * @param column
     *            the key to set
     * @return {@link ImportMapping}
     */
    public static ImportMapping ofNullValue(String column) {
        return new ImportMapping(column, null);
    }

}
