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
	private Text localConsultant, street, zip, city, cityAddon, phone, fax, mobile, email;
	private ComboViewer countryCombo;
	private MultiChoice<ContactType> contactTypeWidget;

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

	public Text getPhone() {
		return phone;
	}

	public void setPhone(Text phone) {
		this.phone = phone;
	}

	public Text getFax() {
		return fax;
	}

	public void setFax(Text fax) {
		this.fax = fax;
	}

	public Text getMobile() {
		return mobile;
	}

	public void setMobile(Text mobile) {
		this.mobile = mobile;
	}

	public Text getEmail() {
		return email;
	}

	public void setEmail(Text email) {
		this.email = email;
	}

}
