/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   This code was copied with friendly permission from gnuaccounting.org. 
 *   - Jochen Staerk
 */
package org.fakturama.export.zugferd;

import org.apache.jempbox.impl.XMLUtil;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.w3c.dom.Element;

/**
 * Additionally to adding a RDF namespace with a indication which file
 * attachment if Zugferd, this namespace has to be described in a PDFA
 * Extension Schema.
 * 
 * Since there does not seem any documentation or explanation how to
 * properly use PDFBox' XMPBOX XMPMetadata (in particular how to write it to
 * a PDF file) I can't use PDFBox' PDFAExtensionSchema so this is my version
 * for PDFBox' jempbox XMPMetadata
 * 
 * @author jstaerk
 * 
 */
// TODO replace by PDFAExtensionSchema (XMPBox)
public class XMPSchemaPDFAExtensions extends XMPSchemaBasic {

	private void addProperty(Element listNode, String name, String type,
			String category, String description) {
		Element nameNode = schema.getOwnerDocument().createElement(
				"pdfaProperty:name"); //$NON-NLS-1$
		XMLUtil.setStringValue(nameNode, name);
		listNode.appendChild(nameNode);

		Element typeNode = schema.getOwnerDocument().createElement(
				"pdfaProperty:valueType"); //$NON-NLS-1$
		XMLUtil.setStringValue(typeNode, type);
		listNode.appendChild(typeNode);

		Element categoryNode = schema.getOwnerDocument().createElement(
				"pdfaProperty:category"); //$NON-NLS-1$
		XMLUtil.setStringValue(categoryNode, category);
		listNode.appendChild(categoryNode);

		Element descriptionNode = schema.getOwnerDocument().createElement(
				"pdfaProperty:description"); //$NON-NLS-1$
		XMLUtil.setStringValue(descriptionNode, description);
		listNode.appendChild(descriptionNode);

	}

	public XMPSchemaPDFAExtensions(org.apache.jempbox.xmp.XMPMetadata parent) {
		super(parent);

		// add some namespaces
		schema.setAttributeNS(NS_NAMESPACE, "xmlns:pdfaExtension", //$NON-NLS-1$
				"http://www.aiim.org/pdfa/ns/extension/"); //$NON-NLS-1$

		schema.setAttributeNS(NS_NAMESPACE, "xmlns:pdfaSchema", //$NON-NLS-1$
				"http://www.aiim.org/pdfa/ns/schema#"); //$NON-NLS-1$

		schema.setAttributeNS(NS_NAMESPACE, "xmlns:pdfaProperty", //$NON-NLS-1$
				"http://www.aiim.org/pdfa/ns/property#"); //$NON-NLS-1$

		// the superclass includes this two namespaces we don't need
		schema.removeAttributeNS(NS_NAMESPACE, "xapGImg"); //$NON-NLS-1$
		schema.removeAttributeNS(NS_NAMESPACE, "xmp"); //$NON-NLS-1$

		/*
		*What we attach is basically this:
		*pdfaExtension:schemas-node
		*+--bag
		*   +--rdf:li
		*      +--some text node (multiple)
		*      +--property node
		*         +--rdf:Seq
		*            +--rdf:li (multiple) attribute node
		*               +--some attribute property description text node (multiple)
		*/
		Element schemasNode = schema.getOwnerDocument().createElement(
				"pdfaExtension:schemas"); //$NON-NLS-1$

		Element bagNode = schema.getOwnerDocument()
				.createElement("rdf:Bag"); //$NON-NLS-1$

		Element bagListNode = schema.getOwnerDocument().createElement(
				"rdf:li"); //$NON-NLS-1$

		bagListNode.setAttribute("rdf:parseType", "Resource"); //$NON-NLS-1$ //$NON-NLS-2$

		Element schemaInfoNode = schema.getOwnerDocument().createElement(
				"pdfaSchema:schema"); //$NON-NLS-1$
		XMLUtil.setStringValue(schemaInfoNode,
				"ZUGFeRD PDFA Extension Schema"); //$NON-NLS-1$
		bagListNode.appendChild(schemaInfoNode);

		Element nsInfoNode = schema.getOwnerDocument().createElement(
				"pdfaSchema:namespaceURI"); //$NON-NLS-1$
		XMLUtil.setStringValue(nsInfoNode, "urn:ferd:pdfa:invoice:rc#"); //$NON-NLS-1$
		bagListNode.appendChild(nsInfoNode);

		Element prefixInfoNode = schema.getOwnerDocument().createElement(
				"pdfaSchema:prefix"); //$NON-NLS-1$
		XMLUtil.setStringValue(prefixInfoNode, "zf"); //$NON-NLS-1$
		bagListNode.appendChild(prefixInfoNode);

		Element propertyNode = schema.getOwnerDocument().createElement(
				"pdfaSchema:property"); //$NON-NLS-1$

		Element sequenceNode = schema.getOwnerDocument().createElement(
				"rdf:Seq"); //$NON-NLS-1$

		Element seqList1Node = schema.getOwnerDocument().createElement(
				"rdf:li"); //$NON-NLS-1$

		seqList1Node.setAttribute("rdf:parseType", "Resource"); //$NON-NLS-1$ //$NON-NLS-2$

		addProperty(seqList1Node, "DocumentFileName", "Text", "external", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"The name of the embedded Zugferd XML invoice file"); //$NON-NLS-1$

		sequenceNode.appendChild(seqList1Node);

		Element seqList2Node = schema.getOwnerDocument().createElement(
				"rdf:li"); //$NON-NLS-1$
		seqList2Node.setAttribute("rdf:parseType", "Resource"); //$NON-NLS-1$ //$NON-NLS-2$
		addProperty(seqList2Node, "DocumentType", "Text", "external", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"INVOICE"); //$NON-NLS-1$
		sequenceNode.appendChild(seqList2Node);

		Element seqList3Node = schema.getOwnerDocument().createElement(
				"rdf:li"); //$NON-NLS-1$
		seqList3Node.setAttribute("rdf:parseType", "Resource"); //$NON-NLS-1$ //$NON-NLS-2$
		addProperty(seqList3Node, "Version", "Text", "external", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"The version of the ZUGFeRD data"); //$NON-NLS-1$
		sequenceNode.appendChild(seqList3Node);

		Element seqList4Node = schema.getOwnerDocument().createElement(
				"rdf:li"); //$NON-NLS-1$
		seqList4Node.setAttribute("rdf:parseType", "Resource"); //$NON-NLS-1$ //$NON-NLS-2$
		addProperty(seqList4Node, "ConformanceLevel", "Text", "external", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"The conformance level of the ZUGFeRD data, i.e. BASIC or EXTENDED"); //$NON-NLS-1$
		sequenceNode.appendChild(seqList4Node);

		propertyNode.appendChild(sequenceNode);

		bagListNode.appendChild(propertyNode);

		bagNode.appendChild(bagListNode);
		schemasNode.appendChild(bagNode);

		schema.appendChild(schemasNode);

	}

}
