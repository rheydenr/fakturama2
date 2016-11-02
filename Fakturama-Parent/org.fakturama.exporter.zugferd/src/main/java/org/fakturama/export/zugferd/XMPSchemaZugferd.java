/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: This code was copied with friendly permission from
 * gnuaccounting.org. - Jochen Staerk
 */
package org.fakturama.export.zugferd;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAExtensionSchema;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.Types;

@StructuredType(preferedPrefix = "zf", namespace = "urn:ferd:pdfa:CrossIndustryDocument:invoice:1p0#")
public class XMPSchemaZugferd extends PDFAExtensionSchema {

	/**
     * 
     */
	public static final String ZUGFERD_XML_DEFAULT_NAME = "ZUGFeRD-invoice.xml";

	/** The Constant CONFORMANCE_LEVEL. */
	@PropertyType(type = Types.Text, card = Cardinality.Simple)
	public static final String CONFORMANCE_LEVEL = "ConformanceLevel";

	/** The Constant DOCUMENT_FILE_NAME. */
	@PropertyType(type = Types.Text, card = Cardinality.Simple)
	public static final String DOCUMENT_FILE_NAME = "DocumentFileName";

	/** The Constant DOCUMENT_TYPE. */
	@PropertyType(type = Types.Text, card = Cardinality.Simple)
	public static final String DOCUMENT_TYPE = "DocumentType";

	/** The Constant VERSION. */
	@PropertyType(type = Types.Text, card = Cardinality.Simple)
	public static final String VERSION = "Version";

	@PropertyType(type = Types.PDFAType, card = Cardinality.Seq)
	public static final String VALUE_TYPE = "valueType";
	
	public XMPSchemaZugferd(XMPMetadata parent, ConformanceLevel level) {
		this(parent, ZUGFERD_XML_DEFAULT_NAME, "INVOICE", "1.0", level);
	}
	
	public XMPSchemaZugferd(XMPMetadata parent, ConformanceLevel level, String version) {
		this(parent, ZUGFERD_XML_DEFAULT_NAME, "INVOICE", version, level);
	}
	
	public XMPSchemaZugferd(XMPMetadata parent, String documentFileName, String documentType, String version, ConformanceLevel conformanceLevel) {
		super(parent);
		setDocumentFileName(documentFileName);
		setDocumentType("INVOICE");
		setVersion(version);
		setConformanceLevel(conformanceLevel.name());
	}

	/**
	 * This is what needs to be added to the RDF metadata - basically the name
	 * of the embedded Zugferd file
	 * */
	public XMPSchemaZugferd(XMPMetadata parent) {
		this(parent, ZUGFERD_XML_DEFAULT_NAME, "INVOICE", "1.0", ConformanceLevel.BASIC);
	}

	public void setConformanceLevel(String conformanceLevel) {
		setTextPropertyValue(CONFORMANCE_LEVEL, conformanceLevel);
	}

	public void setDocumentFileName(String documentFileName) {
		setTextPropertyValue(DOCUMENT_FILE_NAME, documentFileName);
	}

	public void setDocumentType(String documentType) {
		setTextPropertyValue(DOCUMENT_TYPE, documentType);
	}

	public void setVersion(String version) {
		setTextPropertyValue(VERSION, version);
	}

}
