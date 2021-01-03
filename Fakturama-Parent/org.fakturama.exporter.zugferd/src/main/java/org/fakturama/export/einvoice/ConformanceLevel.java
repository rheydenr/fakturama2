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
    
	ZUGFERD_V1_BASIC("urn:ferd:CrossIndustryDocument:invoice:1p0:basic", "BASIC"), 
	ZUGFERD_V1_COMFORT("urn:ferd:CrossIndustryDocument:invoice:1p0:comfort", "COMFORT"), 
	ZUGFERD_V1_EXTENDED("urn:ferd:CrossIndustryDocument:invoice:1p0:extended", "EXTENDED"),
	
	ZUGFERD_V2_MINIMUM("urn:zugferd.de:2p0:minimum", "MINIMUM"),
	ZUGFERD_V2_BASICWL("urnzugferd.de:2p0:basicwl", "BASIC"), 
    ZUGFERD_V2_BASIC("urn:cen.eu:en16931:2017#compliant#urn:zugferd.de:2p0:basic", "BASIC"), 
    ZUGFERD_V2_COMFORT("urn:cen.eu:en16931:2017", "COMFORT"), 
    ZUGFERD_V2_EN16931("urn:cen.eu:en16931:2017", "COMFORT"), 
    
    XRECHNUNG("urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_1.2", "COMFORT"), 
    
    ZUGFERD_V2_EXTENDED("urn:cen.eu:en16931:2017#conformant#urn:zugferd.de:2p0:extended", "EXTENDED"),

    FACTURX_MINIMUM("urn:factur-x.eu:1p0:minimum", "MINIMUM"),
    FACTURX_BASICWL("urn:factur-x.eu:1p0:basicwl", "BASIC"),
    FACTURX_BASIC("urn:cen.eu:en16931:2017#compliant#urn:factur-x.eu:1p0:basic", "BASIC"),
    FACTURX_COMFORT("urn:cen.eu:en16931:2017", "COMFORT"),
    FACTURX_EN16931("urn:cen.eu:en16931:2017", "COMFORT"),
    FACTURX_EXTENDED("urn:cen.eu:en16931:2017#conformant#urn:factur-x.eu:1p0:extended", "EXTENDED")
    ;

	private String urn, descriptor;

	/**
	 * @param urn
	 */
	private ConformanceLevel(String urn, String descriptor) {
		this.urn = urn;
		this.descriptor = descriptor;
	}

	/**
	 * @return the urn
	 */
	public final String getUrn() {
		return urn;
	}

    public String getDescriptor() {
        return descriptor;
    }

}
