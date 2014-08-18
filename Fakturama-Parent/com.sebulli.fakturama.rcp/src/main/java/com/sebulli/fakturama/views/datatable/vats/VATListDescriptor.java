package com.sebulli.fakturama.views.datatable.vats;

public enum VATListDescriptor {
    
    DEFAULT("default", 0, 55),
    NAME("name", 1, 120),
    DESCRIPTION("description", 2, 200),
    VALUE("taxValue", 3, 70)
    ;
    
    private String propertyName;
    private int position, defaultWidth;
    
    /**
     * @param propertyName
     * @param position
     * @param defaultWidth
     */
    private VATListDescriptor(String propertyName, int position, int defaultWidth) {
        this.propertyName = propertyName;
        this.position = position;
        this.defaultWidth = defaultWidth;
    }

    /**
     * @return the propertyName
     */
    public final String getPropertyName() {
        return propertyName;
    }

    /**
     * @return the position
     */
    public final int getPosition() {
        return position;
    }

    /**
     * @return the defaultWidth
     */
    public final int getDefaultWidth() {
        return defaultWidth;
    }

    public static VATListDescriptor getDescriptorFromColumn(int columnIndex) {
        for (VATListDescriptor descriptor : values()) {
            if(descriptor.getPosition() == columnIndex) {
                return descriptor;
            }
        }
        return null;
    }
    
    public static final String[] getVATPropertyNames() {
        return new String[]{
        VATListDescriptor.DEFAULT.getPropertyName(), 
        VATListDescriptor.NAME.getPropertyName(), 
        VATListDescriptor.DESCRIPTION.getPropertyName(), 
        VATListDescriptor.VALUE.getPropertyName()};
    }
}