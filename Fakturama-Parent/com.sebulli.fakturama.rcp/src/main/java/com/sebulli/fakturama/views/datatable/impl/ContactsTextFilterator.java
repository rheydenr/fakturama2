/**
 * 
 */
package com.sebulli.fakturama.views.datatable.impl;

import java.util.List;

import com.sebulli.fakturama.model.Contact;

import ca.odell.glazedlists.TextFilterator;

/**
 * Definition of all search fields for the contacts list.
 * 
 * @author Ralf Heydenreich
 *
 */
public class ContactsTextFilterator implements TextFilterator<Contact> {

	@Override
	public void getFilterStrings(List<String> baseList, Contact contact) {
		baseList.add(contact.getNr());
		baseList.add(contact.getFirstname());
		baseList.add(contact.getName());
		baseList.add(contact.getCompany());
		baseList.add(contact.getZip());
		baseList.add(contact.getCity());
	}
}
