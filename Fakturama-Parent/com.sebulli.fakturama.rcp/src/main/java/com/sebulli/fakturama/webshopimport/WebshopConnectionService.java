/* Fakturama - Free Invoicing Software - https://www.fakturama.info
 * 
 * Copyright (C) 2021 www.fakturama.info
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation */

package com.sebulli.fakturama.webshopimport;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.opcoach.e4.preferences.IPreferenceStoreProvider;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.webshopimport.shops.LegacyWebshopConnector;
import com.sebulli.fakturama.webshopimport.shops.ShopwareV5Connector;

/**
 *
 */
@Component(name = "webshopfactory" )
public class WebshopConnectionService implements IWebshopConnectionService {

    @Inject
    private IEclipseContext ctx;
    
    private IPreferenceStore preferenceStore;
    
    @Reference(unbind = "unbindPreferenceStoreProvider")
    public void bindPreferenceStoreProvider(IPreferenceStoreProvider preferenceStoreProvider) {
        this.preferenceStore = preferenceStoreProvider.getPreferenceStore();
    }
    
    public void unbindPreferenceStoreProvider(IPreferenceStoreProvider preferenceStoreProvider) {
        this.preferenceStore = null;
    }

    @Override
    public IWebshop getWebshop(WebShopConfig webshopConnection) {
        if (webshopConnection != null) {
            Webshop selectedWebshop = webshopConnection.getSelectedWebshop();
            
            if (selectedWebshop == null) {
                // determine from preferences
                String shopType = preferenceStore.getString(Constants.PREFERENCES_WEBSHOP_SHOPTYPE);
                if (shopType != null) {
                    selectedWebshop = Webshop.valueOf(shopType);
                }
            }
            
            IEclipseContext localCtx = EclipseContextFactory.create("webshop-creation");
            localCtx.set(WebShopConfig.class, webshopConnection);
            switch (selectedWebshop) {
            case SHOPWARE_V5:
                return new ShopwareV5Connector(webshopConnection);
            case LEGACY_WEBSHOP:
                IWebshop ws = ContextInjectionFactory.make(LegacyWebshopConnector.class, ctx, localCtx);
                return ws;
            default:
                break;
            }
        } 
        return null;
    }

    @Override
    public Webshop[] getAvailableWebshops() {
        return Webshop.values();
    }
}
