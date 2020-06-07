/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014, 2020 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Ralf Heydenreich - initial API and implementation
 */
package org.fakturama.export.einvoice;

/**
 * Abbildung der Levels für die einzelnen ZUGFeRD-Ausbaustufen
 * 
 * 
 */
public enum ConformanceLevel {
    
	ZUGFERD_V1_BASIC("urn:ferd:invoice:1.0:basic"), 
	ZUGFERD_V1_COMFORT("urn:ferd:invoice:1.0:comfort"), 
	ZUGFERD_V1_EXTENDED("urn:ferd:invoice:1.0:extended"),
	
	ZUGFERD_V2_MINIMUM("urn:zugferd.de:2p0:minimum"),
	ZUGFERD_V2_BASICWL("urnzugferd.de:2p0:basicwl"), 
    ZUGFERD_V2_BASIC("urn:cen.eu:en16931:2017#compliant#urn:zugferd.de:2p0:basic"), 
    ZUGFERD_V2_COMFORT("urn:cen.eu:en16931:2017"), 
    ZUGFERD_V2_EN16931("urn:cen.eu:en16931:2017"), 
    ZUGFERD_V2_EXTENDED("urn:cen.eu:en16931:2017#conformant#urn:zugferd.de:2p0:extended"),

    FACTURX_MINIMUM("urn:factur-x.eu:1p0:minimum"),
    FACTURX_BASICWL("urn:factur-x.eu:1p0:basicwl"),
    FACTURX_BASIC("urn:cen.eu:en16931:2017#compliant#urn:factur-x.eu:1p0:basic"),
    FACTURX_COMFORT("urn:cen.eu:en16931:2017"),
    FACTURX_EN16931("urn:cen.eu:en16931:2017"),
    FACTURX_EXTENDED("urn:cen.eu:en16931:2017#conformant#urn:factur-x.eu:1p0:extended")
    ;

	private String urn;

	/**
	 * @param urn
	 */
	private ConformanceLevel(String urn) {
		this.urn = urn;
	}

	/**
	 * @return the urn
	 */
	public final String getUrn() {
		return urn;
	}

}
