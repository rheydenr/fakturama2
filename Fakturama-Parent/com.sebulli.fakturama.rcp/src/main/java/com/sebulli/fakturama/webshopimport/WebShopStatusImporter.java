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
import javax.xml.bind.UnmarshalException;
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
 * @deprecated Use {@link WebShopController}
 */
public class WebShopStatusImporter implements IRunnableWithProgress {
	
	@Inject
	@Translation
	private Messages msg;

    @Inject
    private IWebshopConnectionService svc;

//	private String productImagePath = "";
	private int worked = 0;
	
	private WebShopConfig connector;
	private ExecutionResult runResult;
	
	private IProgressMonitor localMonitor;
	private Webshopexport webshopexport = null;

	@Override
	public void run(IProgressMonitor pMonitor) throws InvocationTargetException, InterruptedException {
        localMonitor = pMonitor;
        
        if(connector == null) {
	        	setRunResult("no connection information provided");
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
            IWebshop webshop = svc.getWebshop(connector);
            URLConnection urlConnection = webshop.connect();
	        if(urlConnection != null) {
	        	((HttpURLConnection)urlConnection).setRequestMethod( "POST" );
	        	String postString = new StringBuilder("username=")
	            					.append(URLEncoder.encode(connector.getUser(), "UTF-8"))
									.append("&password=")
									.append(URLEncoder.encode(connector.getPassword(), "UTF-8"))
									.append("&action=get_status").toString();
                //this.webShopImportManager.log.debug("POST-String: " + postString);
	        	urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
	        	urlConnection.setRequestProperty("Content-Length", String.valueOf(postString.length()));
	        	
	        	OutputStream outputStream = urlConnection.getOutputStream();
	            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
	            setProgress(20);

                writer.write(postString);
                writer.flush();
                writer.close();

            }
	        
            setProgress(30);
            // Start a connection in an extra thread
            InterruptConnection interruptConnection = new InterruptConnection(urlConnection);
            new Thread(interruptConnection).start();
            while (!localMonitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError());
            
            // If the connection was interrupted and not finished: return
            if (!interruptConnection.isFinished()) {
                ((HttpURLConnection)urlConnection).disconnect();
                if (interruptConnection.isError()) {
                    //T: Status error message importing data from web shop
                	setRunResult(msg.importWebshopErrorCantconnect);
                }
                return;
            }
            
            // If there was an error, return with error message
            if (interruptConnection.isError()) {
                ((HttpURLConnection)urlConnection).disconnect();
                //T: Status message importing data from web shop
                setRunResult(msg.importWebshopErrorCantread);
                return;
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
        catch (UnmarshalException e) {
        	setRunResult(msg.importWebshopErrorCantopen + "\n" + shopURL + "\n"
    				+ "Message: " + e.getCause() + "\n", e);

        if (webshopexport != null)
        	setRunResult(getRunResult().getErrorMessage() + "\n\n" + webshopexport, e);
        }
    	catch (Exception e) {
            //T: Status message importing data from web shop
        	setRunResult(msg.importWebshopErrorCantopen + "\n" + shopURL + "\n"
        				+ "Message: " + e.getLocalizedMessage()+ "\n", e);

            if (webshopexport != null)
            	setRunResult(getRunResult().getErrorMessage() + "\n\n" + webshopexport, e);
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
	public ExecutionResult getRunResult() {
		return runResult;
	}
	
	private void setRunResult(String runResult) {
		setRunResult(runResult, null);
	}
	
	private void setRunResult(String runResult, Exception error) {
		this.runResult = new ExecutionResult(runResult, 1);
		if(error != null) {
			this.runResult.setException(error);
		}
	}

	/**
	 * @return the webshopexport
	 */
	public Webshopexport getWebshopexport() {
		return webshopexport;
	}

	/**
	 * @return the connector
	 */
	public WebShopConfig getConnector() {
		return connector;
	}

	/**
	 * @param connector the connector to set
	 */
	public void setConnector(WebShopConfig connector) {
		this.connector = connector;
	}

}
