package com.sebulli.fakturama.webshopimport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

import java.util.Enumeration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.webshopimport.type.OrderType;
import com.sebulli.fakturama.webshopimport.type.ProductType;

/**
 * This object implements XMLFilter and monitors the incoming SAX events. Once
 * it hits a order element, it creates a new unmarshaller and unmarshals
 * one order.
 * 
 * <p>
 * Once finished unmarshalling it, we will process it, then move on to the next
 * order.
 */
public class Splitter extends XMLFilterImpl {

	/**
	 * Used to keep track of in-scope namespace bindings.
	 * 
	 * For JAXB unmarshaller to correctly unmarshal documents, it needs to know
	 * all the effective namespace declarations.
	 */
	private NamespaceSupport namespaces = new NamespaceSupport();

	/**
	 * Remembers the depth of the elements as we forward SAX events to a JAXB
	 * unmarshaller.
	 */
	private int depth;

	/**
	 * Reference to the unmarshaller which is unmarshalling an object.
	 */
	private UnmarshallerHandler unmarshallerHandler;

	/**
	 * Keeps a reference to the locator object so that we can later pass it to a
	 * JAXB unmarshaller.
	 */
	private Locator locator;
	protected IProgressMonitor localMonitor;
	protected Messages msg;
	
	/**
	 * Writer for creating a logfile.
	 */
	BufferedWriter logBuffer = null;

	public Splitter(JAXBContext context, IProgressMonitor localMonitor, Path logFile) {
		this.context = context;
		this.localMonitor = localMonitor;
		
        // Create a buffered writer to write the imported data to the file system
        try {
            logBuffer = Files.newBufferedWriter(logFile, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	
	/**
	 * We will create unmarshallers from this context.
	 */
	private final JAXBContext context;

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

	    if (!localMonitor.isCanceled()) {
    		if (depth != 0) {
    			// we are in the middle of forwarding events.
    			// continue to do so.
    			depth++;
    			super.startElement(namespaceURI, localName, qName, atts);
    			return;
    		}
    
    		if (namespaceURI.equals("http://www.fakturama.org") 
    				&& (localName.equals("product")
    				 || localName.equals("order"))) {
    			// start a new unmarshaller
    			Unmarshaller unmarshaller;
    			try {
    				unmarshaller = context.createUnmarshaller();
    			} catch (JAXBException e) {
    				// there's no way to recover from this error.
    				// we will abort the processing.
    				throw new SAXException(e);
    			}
    			unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
    
    			// set it as the content handler so that it will receive
    			// SAX events from now on.
    			setContentHandler(unmarshallerHandler);
    
    			// fire SAX events to emulate the start of a new document.
    			unmarshallerHandler.startDocument();
    			unmarshallerHandler.setDocumentLocator(locator);
    			
    			Enumeration<String> e = namespaces.getPrefixes();
    			while (e.hasMoreElements()) {
    				String prefix = e.nextElement();
    				String uri = namespaces.getURI(prefix);
    
    				unmarshallerHandler.startPrefixMapping(prefix, uri);
    			}
    			String defaultURI = namespaces.getURI("");
    			if (defaultURI != null)
    				unmarshallerHandler.startPrefixMapping("", defaultURI);
    
    			super.startElement(namespaceURI, localName, qName, atts);
    
    			// count the depth of elements and we will know when to stop.
    			depth = 1;
    		}
	    } else {
	        throw new SAXException("import cancelled");
	    }
	}

	// only used for logging / debugging
    @SuppressWarnings("unused")
    private void logXml(Object jaxbElement) {
        // Write the web shop log file
        if (logBuffer != null) {
            try {
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(jaxbElement, logBuffer);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
    }

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

		// forward this event
		super.endElement(namespaceURI, localName, qName);

		if (depth != 0) {
			depth--;
			if (depth == 0) {
				// just finished sending one chunk.

				// emulate the end of a document.
				Enumeration<String> e = namespaces.getPrefixes();
				while (e.hasMoreElements()) {
					String prefix = (String) e.nextElement();
					unmarshallerHandler.endPrefixMapping(prefix);
				}
				String defaultURI = namespaces.getURI("");
				if (defaultURI != null)
					unmarshallerHandler.endPrefixMapping("");
				unmarshallerHandler.endDocument();

				// stop forwarding events by setting a dummy handler.
				// XMLFilter doesn't accept null, so we have to give it
				// something,
				// hence a DefaultHandler, which does nothing.
				setContentHandler(new DefaultHandler());

				// then retrieve the fully unmarshalled object
				try {
					switch(localName) {
					case "product":
						JAXBElement<ProductType> result = (JAXBElement<ProductType>) unmarshallerHandler
						.getResult();
					process(result.getValue());
						break;
					case "order":
						JAXBElement<OrderType> resultOrder = (JAXBElement<OrderType>) unmarshallerHandler
						.getResult();
					process(resultOrder.getValue());
						break;
					default:
						result = null;
					}

					// process this new purchase order
				} catch (JAXBException je) {
					// error was found during the unmarshalling.
					// you can either abort the processing by throwing a
					// SAXException,
					// or you can continue processing by returning from this
					// method.
					System.err.println("unable to process an order at line " + locator.getLineNumber());
					return;
				}

				unmarshallerHandler = null;
			}
		}
	}

	private void process(OrderType value) {
        System.out.println("this order will be shipped at " + value.getDate());
	}

	private void process(ProductType order) {
		System.out.println(
				String.format("current location: [%d:%d]", locator.getLineNumber(), locator.getColumnNumber()));
		System.out.println("this order will be shipped to " + order.getCategory());
	}

	public void setDocumentLocator(Locator locator) {
		super.setDocumentLocator(locator);
		this.locator = locator;
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		namespaces.pushContext();
		namespaces.declarePrefix(prefix, uri);

		super.startPrefixMapping(prefix, uri);
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		namespaces.popContext();

		super.endPrefixMapping(prefix);
	}
}
