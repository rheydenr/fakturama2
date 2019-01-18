/**
 * 
 */
package com.sebulli.fakturama.exporter;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.bean.CsvBindByName;

/**
 * Bean for Address export to Deutsche Post (Online Frankierungsservice)
 *
 */
public class DPAddress {
	/**
	 * Absender Name1: z.B. Vor- und Nachname. Firmenname
	 */
	@CsvBindByName(column = "SEND_NAME1", required = true)
	private String senderName;

	/**
	 * Absender Name2: z.B. Adressdetail (zu Händen von Herrn Mustermann, Hinterhof,
	 * etc.)
	 */
	@CsvBindByName(column = "SEND_NAME2", required = false)
	private String additionalSenderName;

	@CsvBindByName(column = "SEND_STREET", required = true)
	private String senderStreet;

	@CsvBindByName(column = "SEND_HOUSENUMBER", required = true)
	private String senderHousenumber;

	@CsvBindByName(column = "SEND_PLZ", required = true)
	private String senderZipCode;

	@CsvBindByName(column = "SEND_CITY", required = true)
	private String senderCity;

	@CsvBindByName(column = "SEND_COUNTRY", required = true)
	private String senderISO3Country;

	@CsvBindByName(column = "RECV_NAME1", required = true)
	private String receiverName;

	@CsvBindByName(column = "RECV_NAME2", required = false)
	private String additionalReceiverName;

	@CsvBindByName(column = "RECV_STREET", required = true)
	private String receiverStreet;

	@CsvBindByName(column = "RECV_HOUSENUMBER", required = true)
	private String receiverHousenumber;

	@CsvBindByName(column = "RECV_PLZ", required = true)
	private String receiverZipCode;

	@CsvBindByName(column = "RECV_CITY", required = true)
	private String receiverCity;

	@CsvBindByName(column = "RECV_COUNTRY", required = false)
	private String receiverISO3Country;

	/**
	 * Es wird eine Produktkennzeichnung durch einen Schlüssel erwartet:
	Wenn kein Produkt gewählt wurde kann die Zeile nicht importiert werden.

	Produkte Deutschland:
	- Schlüssel: PAECKXS.DEU Produkt: Päckchen 1kg
	- Schlüssel: PAECK.DEU Produkt: Päckchen 2kg
	- Schlüssel: PAK02.DEU Produkt: Paket 2 kg
	- Schlüssel: PAK05.DEU Produkt: Paket 5 kg
	- Schlüssel: PAK10.DEU Produkt: Paket 10 kg
	- Schlüssel: PAK31.DEU Produkt: Paket 31,5 kg

	Produkte EU:
	- Schlüssel: PAECK.EU Produkt: Päckchen
	- Schlüssel: PAK05.EU Produkt: Paket  5 kg
	- Schlüssel: PAK10.EU Produkt: Paket 10 kg
	- Schlüssel: PAK20.EU Produkt: Paket 20 kg
	- Schlüssel: PAK315.EU Produkt: Paket 31,5 kg
	 */
	@CsvBindByName(column = "PRODUCT", required = true)
	private String product;

	@CsvBindByName(column = "COUPON", required = false)
	private String coupon;
	
	private String[] requiredFields = { senderName, senderStreet, senderHousenumber, senderZipCode, senderCity,
			senderISO3Country, receiverName, receiverStreet, receiverHousenumber, receiverZipCode, receiverCity,
			product };

	/**
	 * Checks if all required bean attributes are set.
	 * 
	 * @return <code>true</code> if all required bean attributes are set
	 */
	public boolean isValid() {
//		Set<String> emptyF = new HashSet<>();
//		List<String> emptyFields = Arrays.stream(requiredFields).filter(f -> f == null || f.isEmpty()).collect(Collectors.toList());
		return StringUtils.isNoneBlank(requiredFields);
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getAdditionalSenderName() {
		return additionalSenderName;
	}

	public void setAdditionalSenderName(String additionalSenderName) {
		this.additionalSenderName = additionalSenderName;
	}

	public String getSenderStreet() {
		return senderStreet;
	}

	public void setSenderStreet(String senderStreet) {
		this.senderStreet = senderStreet;
	}

	public String getSenderHousenumber() {
		return senderHousenumber;
	}

	public void setSenderHousenumber(String senderHousenumber) {
		this.senderHousenumber = senderHousenumber;
	}

	public String getSenderZipCode() {
		return senderZipCode;
	}

	public void setSenderZipCode(String senderZipCode) {
		this.senderZipCode = senderZipCode;
	}

	public String getSenderCity() {
		return senderCity;
	}

	public void setSenderCity(String senderCity) {
		this.senderCity = senderCity;
	}

	public String getSenderISO3Country() {
		return senderISO3Country;
	}

	public void setSenderISO3Country(String senderISO3Country) {
		this.senderISO3Country = senderISO3Country;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getAdditionalReceiverName() {
		return additionalReceiverName;
	}

	public void setAdditionalReceiverName(String additionalReceiverName) {
		this.additionalReceiverName = additionalReceiverName;
	}

	public String getReceiverStreet() {
		return receiverStreet;
	}

	public void setReceiverStreet(String receiverStreet) {
		this.receiverStreet = receiverStreet;
	}

	public String getReceiverHousenumber() {
		return receiverHousenumber;
	}

	public void setReceiverHousenumber(String receiverHousenumber) {
		this.receiverHousenumber = receiverHousenumber;
	}

	public String getReceiverZipCode() {
		return receiverZipCode;
	}

	public void setReceiverZipCode(String receiverZipCode) {
		this.receiverZipCode = receiverZipCode;
	}

	public String getReceiverCity() {
		return receiverCity;
	}

	public void setReceiverCity(String receiverCity) {
		this.receiverCity = receiverCity;
	}

	public String getReceiverISO3Country() {
		return receiverISO3Country;
	}

	public void setReceiverISO3Country(String receiverISO3Country) {
		this.receiverISO3Country = receiverISO3Country;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getCoupon() {
		return coupon;
	}

	public void setCoupon(String coupon) {
		this.coupon = coupon;
	}
}
