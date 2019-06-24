/**
 * 
 */
package com.sebulli.fakturama.parts;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.nebula.widgets.opal.multichoice.MultiChoice;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.model.ContactType;

/**
 * Helper class for storing widgets for address tab
 *
 */
final class AddressTabWidget {
	Text localConsultant, street, zip, city, cityAddon;
	ComboViewer countryCombo;
	MultiChoice<ContactType> contactTypeWidget;

	public MultiChoice<ContactType> getContactTypeWidget() {
		return contactTypeWidget;
	}

	public void setContactTypeWidget(MultiChoice<ContactType> contacttype) {
		this.contactTypeWidget = contacttype;
	}

	public Text getLocalConsultant() {
		return localConsultant;
	}

	public void setLocalConsultant(Text localConsultant) {
		this.localConsultant = localConsultant;
	}

	public Text getStreet() {
		return street;
	}

	public void setStreet(Text street) {
		this.street = street;
	}

	public Text getZip() {
		return zip;
	}

	public void setZip(Text zip) {
		this.zip = zip;
	}

	public Text getCity() {
		return city;
	}

	public void setCity(Text city) {
		this.city = city;
	}

	public Text getCityAddon() {
		return cityAddon;
	}

	public void setCityAddon(Text cityAddon) {
		this.cityAddon = cityAddon;
	}

	public ComboViewer getCountryCombo() {
		return countryCombo;
	}

	public void setCountryCombo(ComboViewer countryCombo) {
		this.countryCombo = countryCombo;
	}

}
