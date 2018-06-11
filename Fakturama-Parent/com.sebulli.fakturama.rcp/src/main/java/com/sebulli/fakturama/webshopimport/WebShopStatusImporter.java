/**
 * 
 */
package com.sebulli.fakturama.webshopimport;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.webshopimport.type.ObjectFactory;
import com.sebulli.fakturama.webshopimport.type.Webshopexport;

/**
 * Importer for the different WebShop states.
 */
public class WebShopStatusImporter implements IRunnableWithProgress {
	
	@Inject
	@Translation
	private Messages msg;

//	private String productImagePath = "";
	private int worked = 0;
	
	private WebShopConnector connector;
	private String runResult = "";
	
	private IProgressMonitor localMonitor;
	private Webshopexport webshopexport = null;

	@Override
	public void run(IProgressMonitor pMonitor) throws InvocationTargetException, InterruptedException {
        localMonitor = pMonitor;
        
        if(connector == null) {
	        	runResult = "no connection information provided";
	        	return;
        }
        String shopURL = connector.getScriptURL();
        
        // Check empty URL http://shop.fakturama.info/admin/fakturama2_connector.php
        if (shopURL.isEmpty()) {
            //T: Status message importing data from web shop
        	setRunResult(msg.importWebshopErrorUrlnotset);
            return;
        }
        
        // Connect to web shop
        //T: Status message importing data from web shop
        localMonitor.beginTask(msg.importWebshopInfoConnection, 100);
        //T: Status message importing data from web shop
        localMonitor.subTask(msg.importWebshopInfoConnected + " " + shopURL);
        setProgress(10);

        try {
        // Send user name, password and a list of unsynchronized orders to
        // the shop
	        URLConnection connection = connector.createConnection();
	        if(connection != null) {
	        	((HttpURLConnection)connection).setRequestMethod( "POST" );
	        	String postString = new StringBuilder("username=")
	            					.append(URLEncoder.encode(connector.getUser(), "UTF-8"))
									.append("&password=")
									.append(URLEncoder.encode(connector.getPassword(), "UTF-8"))
									.append("&action=status").toString();
                //this.webShopImportManager.log.debug("POST-String: " + postString);
	            connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
	            connection.setRequestProperty("Content-Length", String.valueOf(postString.length()));
	        	
	        	OutputStream outputStream = connection.getOutputStream();
	            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
	            setProgress(20);

                writer.write(postString);
                writer.flush();
                writer.close();

            }
	        
            setProgress(30);
            // Start a connection in an extra thread
            InterruptConnection interruptConnection = new InterruptConnection(connection);
            new Thread(interruptConnection).start();
            while (!localMonitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError());
            
            // If the connection was interrupted and not finished: return
            if (!interruptConnection.isFinished()) {
                ((HttpURLConnection)connection).disconnect();
                if (interruptConnection.isError()) {
                    //T: Status error message importing data from web shop
                	setRunResult(msg.importWebshopErrorCantconnect);
                }
                return;
            }
            
            // If there was an error, return with error message
            if (interruptConnection.isError()) {
                ((HttpURLConnection)connection).disconnect();
                //T: Status message importing data from web shop
                setRunResult(msg.importWebshopErrorCantread);
                return;
            }
            
    		// 1. We need to create JAXBContext instance
    		JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

    		// 2. Use JAXBContext instance to create the Unmarshaller.
    		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			// 3. Use the Unmarshaller to unmarshal the XML document to get
			// an instance of JAXBElement.

            //T: Status message importing data from web shop
            localMonitor.subTask(msg.importWebshopInfoLoading);
			// 4. Get the instance of the required JAXB Root Class from the
			// JAXBElement.
			webshopexport = (Webshopexport) unmarshaller
    					.unmarshal(interruptConnection.getInputStream());
    		setProgress(40);
            // parse the XML stream
            if (!localMonitor.isCanceled()) {
            	if(webshopexport.getWebshop() == null) {
                    //T: Status message importing data from web shop
            		setRunResult(msg.importWebshopErrorNodata + "\n" + shopURL);
                    return;
                }

                // Get the error elements and add them to the run result list
                //ndList = document.getElementsByTagName("error");
                if (StringUtils.isNotEmpty(webshopexport.getError()) ) {
                	setRunResult(webshopexport.getError());
                }
            }
            // else cancel the download process

//            // Interpret the imported data (and load the product images)
//            if (getRunResult().isEmpty()) {
//                // If there is no error - interpret the data.
//            	setWebshopexport(webshopexport);
//            }
            
            localMonitor.done();
        }
        catch (MarshalException mex) {
            //T: Status message importing data from web shop
        	setRunResult(msg.importWebshopErrorNodata + "\n" + shopURL + "\n" + mex.getMessage());
		}
        catch (Exception e) {
            //T: Status message importing data from web shop
        	setRunResult(msg.importWebshopErrorCantopen + "\n" + shopURL + "\n");
        	setRunResult(getRunResult() + "\nMessage: " + e.getLocalizedMessage()+ "\n");
            if (e.getStackTrace().length > 0)
            	setRunResult(getRunResult()+ "\nTrace: " + e.getStackTrace()[0].toString()+ "\n");

            if (webshopexport != null)
            	setRunResult(getRunResult() + "\n\n" + webshopexport);
            }
        }
	
	/**
	 * Sets the progress of the job in percent
	 * 
	 * @param percent
	 */
	protected void setProgress(int percent) {
	    if (percent > worked) {
	        localMonitor.worked(percent - worked);
	        worked = percent;
	    }
	}

	/**
	 * @return the runResult
	 */
	public String getRunResult() {
		return runResult;
	}

	/**
	 * @param runResult the runResult to set
	 */
	public void setRunResult(String runResult) {
		this.runResult = runResult;
	}

	/**
	 * @return the webshopexport
	 */
	public Webshopexport getWebshopexport() {
		return webshopexport;
	}

	/**
	 * @param webshopexport the webshopexport to set
	 */
	public void setWebshopexport(Webshopexport webshopexport) {
		this.webshopexport = webshopexport;
	}

	/**
	 * @return the connector
	 */
	public WebShopConnector getConnector() {
		return connector;
	}

	/**
	 * @param connector the connector to set
	 */
	public void setConnector(WebShopConnector connector) {
		this.connector = connector;
	}

}
