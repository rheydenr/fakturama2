/**
 * 
 */
package com.sebulli.fakturama.model;


/**
 * @author Administrator
 *
 */
public interface IDataSetArray<T> {

	public boolean getCategoryStringsChanged();

	public java.util.List<String> getCategoryStrings();

	public void resetCategoryChanged();

}
