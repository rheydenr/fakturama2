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
 
package com.sebulli.fakturama.webshopimport.shops;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.webshopimport.IWebshop;
import com.sebulli.fakturama.webshopimport.InterruptConnection;
import com.sebulli.fakturama.webshopimport.WebShopConfig;
import com.sebulli.fakturama.webshopimport.Webshop;
import com.sebulli.fakturama.webshopimport.type.ObjectFactory;
import com.sebulli.fakturama.webshopimport.type.Webshopexport;

/**
 * Webshop connector for all PHP based webshops (legacy interface)
 */
public class LegacyWebshopConnector implements IWebshop {

    private static final String PREFERENCE_LASTWEBSHOPIMPORT_DATE = "lastwebshopimport";
    
    private static final String WEBSHOP_IMPORT_LOGFILE = "WebShopImport.log";

    @Inject
    @Translation
    private Messages msg;
    
    @Inject 
    private ILogger log;

    @Inject
    private IPreferenceStore preferences;

    private WebShopConfig webshopConfig;
    private String generalWorkspace;

    @Inject
    public LegacyWebshopConnector(WebShopConfig webshopConnection) {
        this.webshopConfig = webshopConnection;
    }
    
    @Override
    public URLConnection connect() throws IOException {
        
        // ONLY FOR LOCAL CONNECTIONS!!!
        // (else we use HttpClient)
        URL url = new URL(webshopConfig.getScriptURL());
        URLConnection conn = url.openConnection();
        conn.setDoInput(true);
        conn.setConnectTimeout(4000);
        return conn;
    }
    
    @Override
    public Webshopexport changeState(Consumer<Integer> progressMonitor, IProgressMonitor localMonitor) {
        String scriptBaseUrl = webshopConfig.getScriptURL();
        ObjectFactory objectFactory = new ObjectFactory();
        generalWorkspace = preferences.getString(Constants.GENERAL_WORKSPACE);

        // create an export object so that we can transport an error (if any).
        // Will be overwritten if import is ok.
        Webshopexport webshopCallResult = objectFactory.createWebshopexport();

        // Check empty URL
        if (scriptBaseUrl.isEmpty()) {
            // T: Status message importing data from web shop
            webshopCallResult.setError(msg.importWebshopErrorUrlnotset);
            return webshopCallResult;
        }

        try {
            Map<String, Object> data = createRequestParams();

            progressMonitor.accept(20);

            JAXBContext jaxbContext = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] { ObjectFactory.class }, null);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            HttpClient httpClient = HttpClient.newHttpClient();

            progressMonitor.accept(30);

            HttpRequest request = createHttpRequest(scriptBaseUrl, data);
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            webshopCallResult = (Webshopexport) unmarshaller.unmarshal(response.body());

            String error = "";//
            if (StringUtils.isNotEmpty(error)) {
                webshopCallResult.setError(error);
                return webshopCallResult;
            }
        } catch (Exception e) {
            createErrorMessage(scriptBaseUrl, webshopCallResult, e);
        }

        return webshopCallResult;
    }
   
    private  HttpRequest.BodyPublisher buildFormDataFromMap(Map<String, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    @Override
    public Webshopexport synchronizeOrdersAndGetProducts(Consumer<Integer> progressMonitor, IProgressMonitor localMonitor) {
        String scriptBaseUrl = webshopConfig.getScriptURL();
        ObjectFactory objectFactory = new ObjectFactory();
        generalWorkspace = preferences.getString(Constants.GENERAL_WORKSPACE);

        // create an export object so that we can transport an error (if any).
        // Will be overwritten if import is ok.
        Webshopexport webshopCallResult = objectFactory.createWebshopexport();

        // Check empty URL
        if (scriptBaseUrl.isEmpty()) {
            // T: Status message importing data from web shop
            webshopCallResult.setError(msg.importWebshopErrorUrlnotset);
            return webshopCallResult;
        }

        try {

            // 1. We need to create JAXBContext instance
            JAXBContext jaxbContext = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] { ObjectFactory.class }, null);
        
        /* if we have larger documents we have to use SAX.              */
        // 2. create a new XML parser
//            SAXParserFactory factory = SAXParserFactory.newInstance();
//            factory.setNamespaceAware(true);
//            XMLReader reader = factory.newSAXParser().getXMLReader();
        
        // 2. Use JAXBContext instance to create the Unmarshaller.
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    
            if (webshopConfig.getScriptURL().toLowerCase().startsWith("file://")) {
                // for local (file) connections
    
                // Start a connection in an extra thread
                URLConnection localConnection = connect();
                InterruptConnection interruptConnection = new InterruptConnection(localConnection);
                new Thread(interruptConnection).start();
                while (!localMonitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError());

                progressMonitor.accept(20);
   
                String error = checkErrors(interruptConnection, localConnection);
                if (StringUtils.isNotEmpty(error)) {
                    webshopCallResult.setError(error);
                    return webshopCallResult;
                }
    
                webshopCallResult = (Webshopexport) unmarshaller.unmarshal(interruptConnection.getInputStream());
            } else {
                 // form parameters
                Map<String, Object> data = createRequestParams();

                progressMonitor.accept(20);
    
                log.debug("POST-String: " + secureString(data.toString()));
   
                // Send user name, password and a list of unsynchronized orders to
                // the shop
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = createHttpRequest(scriptBaseUrl, data);
                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            // 3. Use the Unmarshaller to unmarshal the XML document to get
            // an instance of JAXBElement.
            // 4. Get the instance of the required JAXB Root Class from the
            // JAXBElement.
                webshopCallResult = (Webshopexport) unmarshaller.unmarshal(response.body());
            }
            
            progressMonitor.accept(30);
            
            //T: Status message importing data from web shop
            localMonitor.subTask(msg.importWebshopInfoLoading);

            // Do not save log files if there is no workspace set
            if (!generalWorkspace.isEmpty()) {

                // Create a sub folder "Log" if it does not exist yet.
                Path logFile = Paths.get(generalWorkspace, "Log", WEBSHOP_IMPORT_LOGFILE);
                if (!Files.isDirectory(logFile.getParent())) {
                    Files.createDirectories(logFile.getParent());
                }

                // Create a buffered writer to write the imported data to the file system
                // Write the web shop log file
                Marshaller marshaller = jaxbContext.createMarshaller(); 
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(webshopCallResult, Files.newOutputStream(logFile,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
            }
            
            progressMonitor.accept(40);
            
            // parse the XML stream
            if (!localMonitor.isCanceled()) {
                if(webshopCallResult.getWebshop() == null) {
                    //T: Status message importing data from web shop
                    webshopCallResult.setError(msg.importWebshopErrorNodata + "\n" + scriptBaseUrl);
                    return webshopCallResult;
                }

                // Clear the list of orders to sync, if the data was sent
                // NodeList ndList = document.getElementsByTagName("webshopexport");
                if (webshopCallResult.getOrders() != null) {
                    webshopConfig.setOrderstosynchronize(new Properties());
                } else {
                    webshopCallResult.setError("import NOT ok");
                }
               
                return webshopCallResult;
            }
        }
        catch (MarshalException mex) {
            //T: Status message importing data from web shop
            webshopCallResult.setError(msg.importWebshopErrorNodata + "\n" + scriptBaseUrl + "\n" + mex.getMessage());
        } catch(UnknownHostException uhe) {
            webshopCallResult.setError("Can't connect to webshop (unknown host): " + uhe.getMessage());
        }
        catch (Exception e) {
            createErrorMessage(scriptBaseUrl, webshopCallResult, e);
        }
    
        return webshopCallResult;
    }

    private void createErrorMessage(String scriptBaseUrl, Webshopexport webshopCallResult, Exception exc) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(msg.importWebshopErrorCantopen).append("\n")
                     .append(scriptBaseUrl).append("\n")
                     .append("Message: ").append(exc.getLocalizedMessage())
                     .append("\n");
        
        //T: Status message importing data from web shop
        if (exc.getStackTrace().length > 0)
            stringBuilder.append("Trace: ").append(exc.getStackTrace()[0].toString()).append("\n");
        
        if(exc.getCause() != null) {
            stringBuilder.append(exc.getCause().getMessage()).append("\n");
        }

        if (webshopCallResult != null) {
            stringBuilder.append(webshopCallResult.getError());
        }
        
        webshopCallResult.setError(stringBuilder.toString());
    }

    private HttpRequest createHttpRequest(String scriptBaseUrl, Map<String, Object> data) {
        Builder requestBuilder = HttpRequest.newBuilder()
                .POST(buildFormDataFromMap(data))
                .uri(URI.create(scriptBaseUrl))
                .setHeader("User-Agent", Webshop.LEGACY_WEBSHOP.getLabel())
                .header("Content-Type", "application/x-www-form-urlencoded");
        // Use password for password protected web shops
        if (webshopConfig.getUseAuthorization()) {
            String encodedPassword = Base64.getEncoder()
                    .encodeToString((webshopConfig.getAuthorizationUser() + ":" + webshopConfig.getAuthorizationPassword()).getBytes());
            requestBuilder.header("Authorization", "Basic " + encodedPassword);
        }
        return requestBuilder.build();
    }

    private Map<String, Object> createRequestParams() throws UnsupportedEncodingException {

        Integer maxProducts = preferences.getInt(Constants.PREFERENCES_WEBSHOP_MAX_PRODUCTS);
        Boolean onlyModifiedProducts = preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS);

        Map<String, Object> data = new HashMap<>();
        data.put("username", URLEncoder.encode(webshopConfig.getUser(), "UTF-8"));
        data.put("password", URLEncoder.encode(webshopConfig.getPassword(), "UTF-8"));
        data.put("ts", System.currentTimeMillis());

        String actionString = webshopConfig.getActionString();

        if (!actionString.isEmpty()) {
            data.put("action", actionString);
        }

        data.put("setstate", webshopConfig.getOrderstosynchronize().toString());
        if (maxProducts > 0) {
            data.put("maxproducts", maxProducts);
        }

        if (onlyModifiedProducts) {
            String lasttime = preferences.getString(PREFERENCE_LASTWEBSHOPIMPORT_DATE);
            if (!lasttime.isEmpty()) {
                data.put("lasttime", lasttime.toString());
            }
        }
        return data;
    }
    
    /**
     * Removes passwords from String for Log output
     * @param string String to test
     * @return secured String
     */
    private String secureString(String insecureString) {
        Pattern p = Pattern.compile("(.*?password=)(.*?)(&.*)", Pattern.DOTALL);
        return RegExUtils.replaceAll(insecureString, p, "$1*****$3");
    }

    private String checkErrors(InterruptConnection interruptConnection, URLConnection urlConnection) {

        // If the connection was interrupted and not finished: return
        if (!interruptConnection.isFinished()) {
            ((HttpURLConnection) urlConnection).disconnect();
            if (interruptConnection.isError()) {
                // T: Status error message importing data from web shop
                return msg.importWebshopErrorCantconnect;
            }
            return "";
        }

        // If there was an error, return with error message
        if (interruptConnection.isError()) {
            ((HttpURLConnection) urlConnection).disconnect();
            // T: Status message importing data from web shop
            return msg.importWebshopErrorCantread;
        }

        return "";
    }

    @Override
    public Webshopexport getAvailableStates(IProgressMonitor localMonitor) {
        String scriptBaseUrl = webshopConfig.getScriptURL();
        ObjectFactory objectFactory = new ObjectFactory();
        Webshopexport webshopCallResult = objectFactory.createWebshopexport();
       
        // Check empty URL
        if (scriptBaseUrl.isEmpty()) {
            //T: Status message importing data from web shop
            webshopCallResult.setError(msg.importWebshopErrorUrlnotset);
            return webshopCallResult;
        }

        try {
            URLConnection urlConnection = connect();
            if(urlConnection != null) {
                ((HttpURLConnection)urlConnection).setRequestMethod( "POST" );
                String postString = new StringBuilder("username=")
                                    .append(URLEncoder.encode(webshopConfig.getUser(), "UTF-8"))
                                    .append("&password=")
                                    .append(URLEncoder.encode(webshopConfig.getPassword(), "UTF-8"))
                                    .append("&action=status").toString();
                //this.webShopImportManager.log.debug("POST-String: " + postString);
                urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(postString.length()));
                
                OutputStream outputStream = urlConnection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);

                writer.write(postString);
                writer.flush();
                writer.close();
            }
            
            // Start a connection in an extra thread
            InterruptConnection interruptConnection = new InterruptConnection(urlConnection);
            new Thread(interruptConnection).start();
            while (!localMonitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError());
            
            String error = checkErrors(interruptConnection, urlConnection);
            if(StringUtils.isNotEmpty(error)) {
                webshopCallResult.setError(error);
                return webshopCallResult;
            }
            
            // 1. We need to create JAXBContext instance
            JAXBContext jaxbContext = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {ObjectFactory.class}, null);

            // 2. Use JAXBContext instance to create the Unmarshaller.
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            // 3. Use the Unmarshaller to unmarshal the XML document to get
            // an instance of JAXBElement.

            //T: Status message importing data from web shop
            localMonitor.subTask(msg.importWebshopInfoLoading);
            // 4. Get the instance of the required JAXB Root Class from the
            // JAXBElement.
            webshopCallResult = (Webshopexport) unmarshaller
                        .unmarshal(interruptConnection.getInputStream());
            // parse the XML stream
            if (!localMonitor.isCanceled()) {
                if(webshopCallResult.getWebshop() == null) {
                    //T: Status message importing data from web shop
                    webshopCallResult.setError(msg.importWebshopErrorNodata + "\n" + scriptBaseUrl);
                    return webshopCallResult;
                }
            }
           
        }
        catch (MarshalException mex) {
            //T: Status message importing data from web shop
            webshopCallResult.setError(msg.importWebshopErrorNodata + "\n" + scriptBaseUrl + "\n" + mex.getMessage());
        }
        catch (UnmarshalException e) {
            webshopCallResult.setError(msg.importWebshopErrorCantopen + "\n" + scriptBaseUrl + "\n"
                    + "Message: " + e.getCause() + "\n"+ e.getMessage());
        }
        catch (Exception e) {
            //T: Status message importing data from web shop
            webshopCallResult.setError(msg.importWebshopErrorCantopen + "\n" + scriptBaseUrl + "\n"
                        + "Message: " + e.getLocalizedMessage()+ "\n");
        } finally {
            localMonitor.done();
        }
        
        return webshopCallResult;
    }

}
