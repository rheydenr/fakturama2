/**
 * 
 */
package com.sebulli.fakturama.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.sebulli.fakturama.oldmodel.OldDocuments;


/**
 * The {@link CustomDocument} class is the implementation of {@link Document}. This class
 * was introduced because we have to store "user managed values". I.e., if a user changes
 * the current address or the payment or shipping info, the manually edited values gets stored in 
 * this {@link CustomDocument}. The original reference to {@link Contact}, {@link Shipping} or
 * {@link Payment} (if any) is deleted (set to <code>null</code>). The getters are transparent in this case. E.g., if someone
 * wants to get the shippingvatdescription he only calls <i>one</i> getter. If a {@link Shipping}
 * is stored, then he gets the VAT description from this {@link Shipping}. If the description was
 * changed manually, he gets the shippingvatdescription stored in this concrete document.<br />
 * 
 * Only that attributes are contained which were also contained in {@link OldDocuments}.
 */
@Entity()
@Table(name = "FKT_CDOCUMENT")
public class CustomDocument extends Document {
	
	private VAT shippingvat;
	private String shippingvatdescription;	
	private String shippingName;
	private String shippingdescription;
	private ShippingVatType shippingAutoVat;
	private Double shippingValue;
	
	/**
	 * A manually edited address (which doesn't point to an existing Address entry).
	 */
	private String manualAddress = null;
		
	private String deliveryAddress = null;
//	private String paymentName = null;
//	private String paymentText = null;
//	private String paymentDescription = null;
//	private String noVatName = null;
//	private String noVatDescription = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 7361484324202876407L;

	/**
	 * @return the shippingvatdescription
	 */
	public String getShippingvatdescription() {
		if(getShipping() != null) {
			return getShipping().getShippingVat().getDescription();
		} else
			return shippingvatdescription;
	}

	/**
	 * @param shippingvatdescription the shippingvatdescription to set
	 */
	public void setShippingvatdescription(String shippingvatdescription) {
		// at first remove the relation to Shipping (like so in all other setters)
		setShipping(null);
		this.shippingvatdescription = shippingvatdescription;
	}

	/**
	 * @return the shippingvat
	 */
	public VAT getShippingvat() {
		if(getShipping() != null) {
			return getShipping().getShippingVat();
		} else {
			return shippingvat;
		}
	}

	/**
	 * @param shippingvat the shippingvat to set
	 */
	public void setShippingvat(VAT shippingvat) {
		setShipping(null);
		this.shippingvat = shippingvat;
	}

	/**
	 * @return the shippingname
	 */
	public String getShippingname() {
		if(getShipping() != null) {
			return getShipping().getName();
		} else {
			return shippingName;
		}
	}

	/**
	 * @param shippingname the shippingname to set
	 */
	public void setShippingname(String shippingname) {
		setShipping(null);
		this.shippingName = shippingname;
	}

	/**
	 * @return the shippingdescription
	 */
	public String getShippingDescription() {
		if(getShipping() != null) {
			return getShipping().getDescription();
		} else {
			return shippingdescription;
		}
	}

	/**
	 * @param shippingdescription the shippingdescription to set
	 */
	public void setShippingDescription(String shippingdescription) {
		setShipping(null);
		this.shippingdescription = shippingdescription;
	}

	/**
	 * @return the shippingautovat
	 */
	public ShippingVatType getShippingAutoVat() {
		if(getShipping() != null) {
			return getShipping().getAutoVat();
		} else {
			return shippingAutoVat;
		}
	}

	/**
	 * @param shippingautovat the shippingautovat to set
	 */
	public void setShippingAutoVat(ShippingVatType shippingautovat) {
		setShipping(null);
		this.shippingAutoVat = shippingautovat;
	}

	/**
	 * @return the shippingValue
	 */
	public Double getShippingValue() {
		if(getShipping() != null) {
			return Double.valueOf(getShipping().getShippingValue());		
		} else {
		return shippingValue;
		}
	}

	/**
	 * @param shippingValue the shippingValue to set
	 */
	public void setShippingValue(Double shippingValue) {
		setShipping(null);
		this.shippingValue = shippingValue;
	}

	/**
	 * @return the manualAddress
	 */
	public String getAddressAsString() {
		if(getContact() != null && getContact().getAddress() != null) {
			return createAddressString(getContact());
		} else {
			return manualAddress;
		}
	}

	private String createAddressString(Contact contact) {
		Address address = contact.getAddress();
		return String.format("", contact.getFirstName(), contact.getName());
	}

	/**
	 * @param manualAddress the manualAddress to set
	 */
	public void setAddress(String manualAddress) {
		setContact(null);
		this.manualAddress = manualAddress;
	}
	
	
	
/*
//
//	/**
//	 * A semantical compare method. This method compares the actual object
//	 * attribute by attribute to another object.
//	 * 
//	 * @generated
//	 */
//	public boolean isSameAs(IndividualDocumentInfo other) {
//		return other != null
//				&& id != null
//				&& id.equals(other.getId())
//				&& manualAddress != null
//				&& manualAddress.compareTo(other.getManualAddress()) == 0
//				&& deliveryAddress != null
//				&& deliveryAddress.compareTo(other.getDeliveryAddress()) == 0
//				&& paymentName != null
//				&& paymentName.compareTo(other.getPaymentName()) == 0
//				&& paymentText != null
//				&& paymentText.compareTo(other.getPaymentText()) == 0
//				&& paymentDescription != null
//				&& paymentDescription.compareTo(other.getPaymentDescription()) == 0
//				&& shippingName != null
//				&& shippingName.compareTo(other.getShippingName()) == 0
//				&& shippingValue != null
//				&& shippingValue.equals(other.getShippingValue())
//				&& shippingAutoVat != null
//				&& shippingAutoVat.compareTo(other.getShippingAutoVat()) == 0
//				&& shippingDescription != null
//				&& shippingDescription
//						.compareTo(other.getShippingDescription()) == 0
//				&& shippingVatDescription != null
//				&& shippingVatDescription.compareTo(other
//						.getShippingVatDescription()) == 0
//				&& shippingVat != null
//				&& shippingVat.equals(other.getShippingVat())
//				&& noVatName != null
//				&& noVatName.compareTo(other.getNoVatName()) == 0
//				&& noVatDescription != null
//				&& noVatDescription.compareTo(other.getNoVatDescription()) == 0
//				&& true /* and this is the last entry from the attributes */;
//	}
//
//
// */
}
