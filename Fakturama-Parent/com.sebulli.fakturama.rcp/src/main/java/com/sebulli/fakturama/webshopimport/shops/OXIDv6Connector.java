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

package com.sebulli.fakturama.webshopimport.shops;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.nls.Translation;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.webshopimport.IWebshop;
import com.sebulli.fakturama.webshopimport.WebShopConfig;
import com.sebulli.fakturama.webshopimport.type.ObjectFactory;
import com.sebulli.fakturama.webshopimport.type.Webshopexport;

/**
 * OXID v6 connector
 *
 */
public class OXIDv6Connector implements IWebshop {

    @Inject
    @Translation
    private Messages msg;
    
    @Inject
    protected HttpClient cl;

    private WebShopConfig webshopConfig;
    
    // one instance, reuse
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public OXIDv6Connector(WebShopConfig webshopConnection) {
        this.webshopConfig = webshopConnection;
    }

    @Override
    public URLConnection connect() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Webshopexport synchronizeOrdersAndGetProducts(Consumer<Integer> progressMonitor, IProgressMonitor localMonitor) {
        if(!checkLogin()) {
            return null;
        }
        
        String scriptBaseUrl = webshopConfig.getScriptURL();
        ObjectFactory objectFactory = new ObjectFactory();

        // create an export object so that we can transport an error (if any).
        // Will be overwritten if import is ok.
        Webshopexport webshopCallResult = objectFactory.createWebshopexport();

        // Check empty URL
        if (scriptBaseUrl.isEmpty()) {
            // T: Status message importing data from web shop
            webshopCallResult.setError(msg.importWebshopErrorUrlnotset);
            return webshopCallResult;
        }

        synchronizeOrders();

        callGetProducts();

        return null;
    }

    private void callGetProducts() {
        // TODO Auto-generated method stub

    }

    private void synchronizeOrders() {
        

    }

    /**
     * Checks for a valid auth token and generates one if possible.
     */
    private boolean checkLogin() {
        if (webshopConfig.getAuthToken() == null) {
            String data = "{\"query\":\"query { token(username: \\\"rheydenr@justmail.de\\\", password:\\\"oxid620\\\") }\"}";

            HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(data))
                    .uri(URI.create("http://localhost/OXID630/source/graphql"))
                    .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                    .header("Content-Type", "application/json").build();

            HttpResponse<String> response = null;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            } catch (IOException | InterruptedException e) {
                System.err.println("ERROR retrieving authentication information. " + e.getMessage());
            }
            // Get the status of the response
            int status = response != null ? response.statusCode() : 0;
            if (status >= 200 && status < 300) {
                // print response body
                System.out.println(response.body());
                webshopConfig.setAuthToken(response.body());
                
            } else {
                System.err.println("ERROR retrieving authentication information.");
            }
        }
        return webshopConfig.getAuthToken() != null;
    }

    @Override
    public Webshopexport changeState(Consumer<Integer> progressMonitor, IProgressMonitor localMonitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Webshopexport getAvailableStates(IProgressMonitor localMonitor) {
        // TODO Auto-generated method stub
        return null;
    }

}
