/**
 * 
 */
package com.sebulli.fakturama.model;

/**
 * @author Administrator
 *
 */
public interface IEntity {
	/**
	 * The category of this entity.
	 * @return
	 */
	public AbstractCategory getCategory();

	/**
	 * Transaction Key, if available. Used in content provider for data table.
	 * @return
	 */
	public int getTransactionKey();
	
	/**
	 * The id of this entity
	 * @return
	 */
	public Long getId();
	

	/**
	 * Returns the value of '<em><b>name</b></em>' feature.
	 * 
	 * @return the value of '<em><b>name</b></em>' feature
	 */
	public String getName();


	/**
	 * Address key, if available. Used in content provider for data table.
	 * @return
	 */
	public int getAddressKey();
	
	public String getFormatedStringValueByKey(String key);
}
