/* 
 * Fakturama - Free Invoicing Software - https://www.fakturama.info
 * 
 * Copyright (C) 2021 www.fakturama.info
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.webshopimport;

import java.io.IOException;
import java.net.URLConnection;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;

import com.sebulli.fakturama.webshopimport.type.Webshopexport;

/**
 * Common interface for all webhop connections. 
 *
 */
public interface IWebshop {

    URLConnection connect() throws IOException;

    Webshopexport synchronizeOrdersAndGetProducts(Consumer<Integer> progressMonitor, IProgressMonitor localMonitor);

}
