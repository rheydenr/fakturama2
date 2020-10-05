/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2020 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.exporter;

import com.sebulli.fakturama.model.Contact;

/**
 * Export interface for Contact's exporter.
 *
 */
public interface IContactExporter {

    /**
     * Export the complete information about a Contact.
     * @param currentContact
     * @return 
     */
    boolean writeDatasheet(Contact currentContact);

}
