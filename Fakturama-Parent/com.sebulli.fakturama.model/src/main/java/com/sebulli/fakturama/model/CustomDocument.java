/**
 * 
 */
package com.sebulli.fakturama.model;

import javax.persistence.Entity;
import javax.persistence.Table;


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
	private String shippingname;
	private String shippingdescription;
	private boolean shippingautovat;

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
		}
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
			return shippingname;
		}
	}

	/**
	 * @param shippingname the shippingname to set
	 */
	public void setShippingname(String shippingname) {
		setShipping(null);
		this.shippingname = shippingname;
	}

	/**
	 * @return the shippingdescription
	 */
	public String getShippingdescription() {
		if(getShipping() != null) {
			return getShipping().getDescription();
		} else {
			return shippingdescription;
		}
	}

	/**
	 * @param shippingdescription the shippingdescription to set
	 */
	public void setShippingdescription(String shippingdescription) {
		setShipping(null);
		this.shippingdescription = shippingdescription;
	}

	/**
	 * @return the shippingautovat
	 */
	public boolean isShippingautovat() {
		if(getShipping() != null) {
			return getShipping().getAutoVat();
		} else {
			return shippingautovat;
		}
	}

	/**
	 * @param shippingautovat the shippingautovat to set
	 */
	public void setShippingautovat(boolean shippingautovat) {
		setShipping(null);
		this.shippingautovat = shippingautovat;
	}
}
