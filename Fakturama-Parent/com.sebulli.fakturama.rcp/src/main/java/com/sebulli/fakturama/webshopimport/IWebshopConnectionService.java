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

import org.eclipse.e4.core.contexts.IEclipseContext;

import com.sebulli.fakturama.handlers.WebshopCommand;

/**
 *
 */
public interface IWebshopConnectionService {

    /**
     * Returns a concrete webshop instance for a certain webshop.
     * The type of the webshop is determined by preference settings.
     * @param webshopConnection
     * @return
     */
    IWebshop getWebshop(WebShopConfig webshopConnection);

    /**
     * List of available web shops.
     * @return List of available web shops.
     */
    Webshop[] getAvailableWebshops();

    /**
     * Creates a static {@link IEclipseContext} which contains connection
     * information about the currently selected webshop.
     * 
     * @param selectedShopSystemId the webshop id which is currently in use
     * @param cmd (optional) command for which the the connector is prepared. Can be <code>null</code>.
     * @return {@link IEclipseContext}
     */
    IEclipseContext createWebshopContext(String selectedShopSystemId, WebshopCommand cmd);

}
