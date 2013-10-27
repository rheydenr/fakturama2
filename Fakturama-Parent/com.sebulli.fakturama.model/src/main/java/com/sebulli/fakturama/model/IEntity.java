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
	public String getCategory();

	/**
	 * Transaction Key, if available. Used in content provider for data table.
	 * @return
	 */
	public int getTransactionKey();
	
	/**
	 * The id of this entity
	 * @return
	 */
	public int getId();

	/**
	 * Address key, if available. Used in content provider for data table.
	 * @return
	 */
	public int getAddressKey();
	
	public String getFormatedStringValueByKey(String key);
}
