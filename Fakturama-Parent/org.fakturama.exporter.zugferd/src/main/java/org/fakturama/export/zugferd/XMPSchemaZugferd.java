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

import org.apache.jempbox.impl.XMLUtil;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.w3c.dom.Element;

public class XMPSchemaZugferd extends XMPSchemaBasic {

    /**
     * This is what needs to be added to the RDF metadata - basically the name
     * of the embedded Zugferd file
     */
    public XMPSchemaZugferd(org.apache.jempbox.xmp.XMPMetadata parent, ConformanceLevel level) {
        super(parent);

        schema.setAttributeNS(NS_NAMESPACE, "xmlns:zf", //$NON-NLS-1$
                "urn:ferd:pdfa:invoice:rc#"); //$NON-NLS-1$
        // the superclass includes this two namespaces we don't need
        schema.removeAttributeNS(NS_NAMESPACE, "xapGImg"); //$NON-NLS-1$
        schema.removeAttributeNS(NS_NAMESPACE, "xmp"); //$NON-NLS-1$
        Element textNode = schema.getOwnerDocument().createElement("zf:DocumentType"); //$NON-NLS-1$
        XMLUtil.setStringValue(textNode, "INVOICE"); //$NON-NLS-1$
        schema.appendChild(textNode);

        textNode = schema.getOwnerDocument().createElement("zf:DocumentFileName"); //$NON-NLS-1$
        XMLUtil.setStringValue(textNode, "ZUGFeRD-invoice.xml"); //$NON-NLS-1$
        schema.appendChild(textNode);

        textNode = schema.getOwnerDocument().createElement("zf:Version"); //$NON-NLS-1$
        XMLUtil.setStringValue(textNode, "1.0"); //$NON-NLS-1$
        schema.appendChild(textNode);

        textNode = schema.getOwnerDocument().createElement("zf:ConformanceLevel"); //$NON-NLS-1$
        XMLUtil.setStringValue(textNode, level.name()); //$NON-NLS-1$
        schema.appendChild(textNode);

    }

}
