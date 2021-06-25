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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Flow.Subscriber;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.webshopimport.IWebshop;
import com.sebulli.fakturama.webshopimport.InterruptConnection;
import com.sebulli.fakturama.webshopimport.WebShopConfig;
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
        URLConnection conn = null;
        URL url = new URL(webshopConfig.getScriptURL());
        conn = url.openConnection();
        conn.setDoInput(true);
        conn.setConnectTimeout(4000);
        if (!webshopConfig.getScriptURL().toLowerCase().startsWith("file://")) {
            conn.setDoOutput(true);
    
            // Use password for password protected web shops
            if (webshopConfig.getUseAuthorization()) {
                String encodedPassword = Base64.getEncoder().encodeToString((webshopConfig.getAuthorizationUser() 
                        + ":" + webshopConfig.getAuthorizationPassword()).getBytes());
                conn.setRequestProperty("Authorization", "Basic " + encodedPassword );
            }
        }
        return conn;
    }
    
    @Override
    public Webshopexport changeState(Consumer<Integer> progressMonitor, IProgressMonitor localMonitor) {
        String scriptBaseUrl = webshopConfig.getScriptURL();
        ObjectFactory objectFactory = new ObjectFactory();
        generalWorkspace = preferences.getString(Constants.GENERAL_WORKSPACE);

        Integer maxProducts = preferences.getInt(Constants.PREFERENCES_WEBSHOP_MAX_PRODUCTS);
        Boolean onlyModifiedProducts = preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS);

        // create an export object so that we can transport an error (if any).
        // Will be overwritten if import is ok.
        Webshopexport webshopCallResult = objectFactory.createWebshopexport();
        
        // Check empty URL
        if (scriptBaseUrl.isEmpty()) {
            //T: Status message importing data from web shop
            webshopCallResult.setError(msg.importWebshopErrorUrlnotset);
            return webshopCallResult;
        }

        BufferedWriter logBuffer = null;

        try {
            URLConnection urlConnection = connect();

            // Send user name, password and a list of unsynchronized orders to
            // the shop
            if(urlConnection != null && urlConnection.getDoOutput()) {
                OutputStream outputStream = urlConnection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                progressMonitor.accept(20);
                
                StringBuilder postStringSb = new StringBuilder("username=")
                        .append(URLEncoder.encode(webshopConfig.getUser(), "UTF-8"))
                        .append("&password=")
                        .append(URLEncoder.encode(webshopConfig.getPassword(), "UTF-8"));


                postStringSb.append("&setstate=").append(webshopConfig.getOrderstosynchronize().toString());

                log.debug("POST-String: " + secureString(postStringSb.toString()));
                writer.write(postStringSb.toString());
                writer.flush();
                writer.close();
            }
            progressMonitor.accept(30);
            
            // Start a connection in an extra thread
            InterruptConnection interruptConnection = new InterruptConnection(urlConnection);
            new Thread(interruptConnection).start();
            while (!localMonitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError());
            
            String error = checkErrors(interruptConnection, urlConnection);
            if(StringUtils.isNotEmpty(error)) {
                webshopCallResult.setError(error);
                return webshopCallResult;
            }
        } catch  (Exception e) {
            
        }
        
        return webshopCallResult;
            
        // for legacy connectors it's the same
       // return synchronizeOrdersAndGetProducts(progressMonitor, localMonitor);
    }
   
    private  HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        System.out.println(builder.toString());
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    @Override
    public Webshopexport synchronizeOrdersAndGetProducts(Consumer<Integer> progressMonitor, IProgressMonitor localMonitor) {
        String scriptBaseUrl = webshopConfig.getScriptURL();
        ObjectFactory objectFactory = new ObjectFactory();
        generalWorkspace = preferences.getString(Constants.GENERAL_WORKSPACE);

        Integer maxProducts = preferences.getInt(Constants.PREFERENCES_WEBSHOP_MAX_PRODUCTS);
        Boolean onlyModifiedProducts = preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_ONLY_MODIFIED_PRODUCTS);

        // create an export object so that we can transport an error (if any).
        // Will be overwritten if import is ok.
        Webshopexport webshopCallResult = objectFactory.createWebshopexport();
        
        // Check empty URL
        if (scriptBaseUrl.isEmpty()) {
            //T: Status message importing data from web shop
            webshopCallResult.setError(msg.importWebshopErrorUrlnotset);
            return webshopCallResult;
        }

        BufferedWriter logBuffer = null;

        try {
            URLConnection urlConnection = connect();

//            HttpClient httpClient = HttpClient.newHttpClient();
//            // form parameters
//            Map<Object, Object> data = new HashMap<>();
//            data.put("username", "admin");
//            data.put("password", "admin");
////            data.put("custom", "secret");
//            data.put("ts", System.currentTimeMillis());
//            HttpRequest request = HttpRequest.newBuilder()
//                    .POST(buildFormDataFromMap(data))
//                    .uri(URI.create(webshopConfig.getScriptURL()))
//                    .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
//                    .header("Content-Type", "application/x-www-form-urlencoded")
//                    .build();
//
////            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            
            // 1. We need to create JAXBContext instance
//            Map<String, Object> properties = new HashMap<String, Object>();
//            properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, response.body());
//            JAXBContext jaxbContext1 = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {ObjectFactory.class, Webshopexport.class}, properties);
//            Unmarshaller unmarshaller1 = jaxbContext1.createUnmarshaller();
//
//
////            StreamSource json = new StreamSource(response.body(), response.uri().toString());
//            Webshopexport md = (Webshopexport) unmarshaller1.unmarshal(response.body())/*, Webshopexport.class)*//*.getValue()*/;
//           
//            
//            
//               HttpResponse<Webshopexport> response = httpClient.send(request, new BodyHandler<Webshopexport>() {
//
//                @Override
//                public BodySubscriber<Webshopexport> apply(ResponseInfo responseInfo) {
//                    return HttpResponse.BodySubscribers.mapping(
//                            HttpResponse.BodySubscribers.ofInputStream(), 
//                            inputStream -> {
//                                Webshopexport md = null;
//                                try {
////                                    StreamSource json = new StreamSource(inputStream);
////                                    System.out.print(inputStream.read());
//                                    
//                                    // Hier steht das konkrete Objekt, das aus dem Shop geladen wird. Das implementiert aber ein allgmeines
//                                    // Interface (und vor allem IAdaptable), damit das dann in ein gültiges Webshopexport-Objekt umgewandelt werden kann.
//                                    md = (Webshopexport) unmarshaller1.unmarshal(inputStream/*, Webshopexport.class*/)/*.getValue()*/;
//                                    System.out.println(md);
//                                } catch (javax.xml.bind.JAXBException e) {
//                                    // TODO Auto-generated catch block
//                                    e.printStackTrace();
//                                }
//                                return md;
//                            });
//                        }
//            });
//               

//            // print status code
//            System.out.println(response.statusCode());
//
//            // print response body
//            System.out.println(response.body());


            
            
            // Send user name, password and a list of unsynchronized orders to
            // the shop
            if(urlConnection != null && urlConnection.getDoOutput()) {
                OutputStream outputStream = urlConnection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                progressMonitor.accept(20);
                
                StringBuilder postStringSb = new StringBuilder("username=")
                        .append(URLEncoder.encode(webshopConfig.getUser(), "UTF-8"))
                        .append("&password=")
                        .append(URLEncoder.encode(webshopConfig.getPassword(), "UTF-8"));

                String actionString = "";
                if (webshopConfig.isGetProducts())
                    actionString += "_products";
                if (webshopConfig.isGetOrders())
                    actionString += "_orders";
                if (!actionString.isEmpty())
                    actionString = "&action=get" + actionString;

                postStringSb.append(actionString)
                    .append("&setstate=").append(webshopConfig.getOrderstosynchronize().toString());
                if (maxProducts > 0) {
                    postStringSb.append("&maxproducts=").append(maxProducts.toString());
                }

                if (onlyModifiedProducts) {
                    String lasttime = preferences.getString(PREFERENCE_LASTWEBSHOPIMPORT_DATE);
                    if (! lasttime.isEmpty()) {
                        postStringSb.append("&lasttime=").append(lasttime.toString());
                    }
                }
            
                log.debug("POST-String: " + secureString(postStringSb.toString()));
                writer.write(postStringSb.toString());
                writer.flush();
                writer.close();
            }
            progressMonitor.accept(30);
            
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
            
            /* if we have larger documents we have to use SAX.              */
            // 2. create a new XML parser
//                SAXParserFactory factory = SAXParserFactory.newInstance();
//                factory.setNamespaceAware(true);
//                XMLReader reader = factory.newSAXParser().getXMLReader();
            
            // 2. Use JAXBContext instance to create the Unmarshaller.
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            //T: Status message importing data from web shop
            localMonitor.subTask(msg.importWebshopInfoLoading);

            // Get the directory of the workspace
            Path logFile = null;

            // Do not save log files if there is no workspace set
            if (!generalWorkspace.isEmpty()) {

                // Create a sub folder "Log" if it does not exist yet.
                // Name of the log file
                // Create a File object
                logFile = Paths.get(generalWorkspace, "Log", WEBSHOP_IMPORT_LOGFILE);
                if (!Files.isDirectory(logFile.getParent())) {
                    Files.createDirectories(logFile.getParent());
                }

                // Create a new file
//                    Files.deleteIfExists(logFile);
//                    Files.createFile(logFile);
//    
                // Create a buffered writer to write the imported data to the file system
                logBuffer = Files.newBufferedWriter(logFile, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            
            // 3. Use the Unmarshaller to unmarshal the XML document to get
            // an instance of JAXBElement.
            // 4. Get the instance of the required JAXB Root Class from the
            // JAXBElement.
            webshopCallResult = (Webshopexport) unmarshaller
                        .unmarshal(interruptConnection.getInputStream());
            
            // alternatively (for large responses)
            // prepare a Splitter
//                Splitter splitter = new Splitter(jaxbContext);

            // connect two components
//                reader.setContentHandler(splitter);
            
            // TODO surround with try-catch! This is the main part for reading the stream.
            // note that XMLReader expects an URL, not a file name.
            // so we need conversion.
//                reader.parse(new InputSource(interruptConnection.getInputStream()));
            
            progressMonitor.accept(40);
            
            // Write the web shop log file
            if (logBuffer != null) {
                Marshaller marshaller = jaxbContext.createMarshaller(); 
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(webshopCallResult, logBuffer);
                logBuffer.close();
            }
            
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
            //T: Status message importing data from web shop
            String error = msg.importWebshopErrorCantopen + "\n" + scriptBaseUrl + "\n"
            +"Message: " + e.getLocalizedMessage()+ "\n";
            if (e.getStackTrace().length > 0)
                error += "\nTrace: " + e.getStackTrace()[0].toString()+ "\n";
            
            if(e.getCause() != null) {
                error += e.getCause().getMessage() + "\n";
            }

            if (webshopCallResult != null)
                error += "\n\n" + webshopCallResult;
            webshopCallResult.setError(error);
        }
        finally {
            if(logBuffer != null) {
                try {
                    logBuffer.close();
                } catch (IOException e) {
                    log.error(e, String.format("couldn't close output stream for logfile '%s'.", WEBSHOP_IMPORT_LOGFILE));
                }
            }
        }
    
        return webshopCallResult;
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
            ((HttpURLConnection)urlConnection).disconnect();
            if (interruptConnection.isError()) {
                //T: Status error message importing data from web shop
                return msg.importWebshopErrorCantconnect;
            }
            return "";
        }

        // If there was an error, return with error message
        if (interruptConnection.isError()) {
            ((HttpURLConnection)urlConnection).disconnect();
            //T: Status message importing data from web shop
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
