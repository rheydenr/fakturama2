/**
 * 
 */
package com.sebulli.fakturama.dto;

import java.util.List;

import com.sebulli.fakturama.model.Contacts;
import com.sebulli.fakturama.model.IDataSetArray;

/**
 * @author Administrator
 * @deprecated don't know if we need this...
 */
public class ContactsDataSet implements IDataSetArray<Contacts> {
	private List<Contacts> dataSet;

	/**
	 * 
	 */
	public ContactsDataSet() {
	}

	/**
	 * @param dataSet
	 */
	public ContactsDataSet(List<Contacts> dataSet) {
		this.dataSet = dataSet;
	}

	/**
	 * @return the dataSet
	 */
	public final List<Contacts> getDataSet() {
		return dataSet;
	}

	/**
	 * @param dataSet the dataSet to set
	 */
	public final void setDataSet(List<Contacts> dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public boolean getCategoryStringsChanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getCategoryStrings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetCategoryChanged() {
		// TODO Auto-generated method stub
		
	}

}
