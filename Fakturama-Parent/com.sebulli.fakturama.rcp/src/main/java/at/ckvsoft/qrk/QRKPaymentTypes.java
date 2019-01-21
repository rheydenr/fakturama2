package at.ckvsoft.qrk;

public enum QRKPaymentTypes {
	// 0 = BAR, 1 = Bankomat, 2 = Kreditkarte
	
	BAR(0, "Bar"),
	BANKOMAT(1, "Bankomat"),
	KREDITKARTE(2, "Kreditkarte");
	
	private int id;
	private String name;
	
	private QRKPaymentTypes(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public final int getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}
	
}
