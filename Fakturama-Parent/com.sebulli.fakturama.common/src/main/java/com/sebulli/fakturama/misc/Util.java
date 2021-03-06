package com.sebulli.fakturama.misc;

public final class Util {

    public static void arrayCopyWithRemoval(Object [] src, Object [] dst, int idxToRemove) {
    	if (src == null || dst == null || src.length - 1 != dst.length || idxToRemove < 0 || idxToRemove >= src.length) {
			throw new IllegalArgumentException();
		}
    	
    	if (idxToRemove == 0) {
    		System.arraycopy(src, 1, dst, 0, src.length - 1);    		
    	}
    	else if (idxToRemove == src.length - 1) {
    		System.arraycopy(src, 0, dst, 0, src.length - 1);
    	}
    	else {
    		System.arraycopy(src, 0, dst, 0, idxToRemove);
    		System.arraycopy(src, idxToRemove + 1, dst, idxToRemove, src.length - idxToRemove - 1);
    	}    	
    }

    private Util() {
    }	
    
// GS/
	/**
	 * Helper method
	 * if theValue is null defaultValue is returned
	 * @param theValue
	 * @param defaultValue
	 * @return
	 */
	public static String defaultIfNull(String theValue, String defaultValue) {
		return theValue != null ? theValue : defaultValue;
	}

	/**
	 * Helper method
	 * if theValue is null or empty (theValue.trim().length() == 0) defaultValue is returned
	 * @param theValue
	 * @param defaultValue
	 * @return
	 */
	public static String defaultIfEmpty(String theValue, String defaultValue) {
		if (theValue != null && theValue.trim().length() > 0) {
			return theValue.trim();
		} else {
			return defaultValue;
		}
	}
}
