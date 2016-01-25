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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.webshopimport.type.ObjectFactory;
import com.sebulli.fakturama.webshopimport.type.Webshopexport;

/**
 * Importer for the different WebShop states.
 */
public class WebShopStatusImport extends AbstractWebshopImporter implements IRunnableWithProgress {
IWebshopConnection webshopManager;
	public WebShopStatusImport(IWebshopConnection webshopManager) {
		super(webshopManager.getPreferences(), webshopManager.getMsg());
		this.webshopManager = webshopManager;
	}

	@Override
	public void run(IProgressMonitor pMonitor) throws InvocationTargetException, InterruptedException {
        localMonitor = pMonitor;
		Webshopexport webshopexport = null;

        // Check empty URL
        if (address.isEmpty()) {
            //T: Status message importing data from web shop
        	webshopManager.setRunResult(msg.importWebshopErrorUrlnotset);
            return;
        }
        
        // Add "http://" if no protocol is given
        address = StringUtils.prependIfMissingIgnoreCase(address, "http://", "https://", "file://");
        // Connect to web shop
        //T: Status message importing data from web shop
        localMonitor.beginTask(msg.importWebshopInfoConnection, 100);
        //T: Status message importing data from web shop
        localMonitor.subTask(msg.importWebshopInfoConnected + " " + address);
        setProgress(10);

        try {
        // Send user name, password and a list of unsynchronized orders to
        // the shop
	        URLConnection conn = createConnection(address, useAuthorization, authorizationUser, authorizationPassword);
	        if(conn != null) {
	        	OutputStream outputStream = conn.getOutputStream();
	            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
	            setProgress(20);
	            String postString = "username=" + URLEncoder.encode(user, "UTF-8") + "&password=" +URLEncoder.encode(password, "UTF-8") ;
	            String actionString = "&action=get_statusvalues";
	            postString += actionString;
                //this.webShopImportManager.log.debug("POST-String: " + postString);
                writer.write(postString);
                writer.flush();
                writer.close();

            }
            setProgress(30);
            // Start a connection in an extra thread
            InterruptConnection interruptConnection = new InterruptConnection(conn);
            new Thread(interruptConnection).start();
            while (!localMonitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError());
            
            // If the connection was interrupted and not finished: return
            if (!interruptConnection.isFinished()) {
                ((HttpURLConnection)conn).disconnect();
                if (interruptConnection.isError()) {
                    //T: Status error message importing data from web shop
                	webshopManager.setRunResult(msg.importWebshopErrorCantconnect);
                }
                return;
            }
            
            // If there was an error, return with error message
            if (interruptConnection.isError()) {
                ((HttpURLConnection)conn).disconnect();
                //T: Status message importing data from web shop
                webshopManager.setRunResult(msg.importWebshopErrorCantread);
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
    		setProgress(50);
            // parse the XML stream
            if (!localMonitor.isCanceled()) {
            	if(webshopexport.getWebshop() == null) {
                    //T: Status message importing data from web shop
            		webshopManager.setRunResult(msg.importWebshopErrorNodata + "\n" + address);
                    return;
                }

                // Get the error elements and add them to the run result list
                //ndList = document.getElementsByTagName("error");
                if (StringUtils.isNotEmpty(webshopexport.getError()) ) {
                	webshopManager.setRunResult(webshopexport.getError());
                }
            }
            // else cancel the download process

            // Interpret the imported data (and load the product images)
            if (webshopManager.getRunResult().isEmpty()) {
                // If there is no error - interpret the data.
                interpretWebShopStates(localMonitor, webshopexport);
            }
            
            localMonitor.done();
        }
        catch (MarshalException mex) {
            //T: Status message importing data from web shop
        	webshopManager.setRunResult(msg.importWebshopErrorNodata + "\n" + address + "\n" + mex.getMessage());
		}
        catch (Exception e) {
            //T: Status message importing data from web shop
        	webshopManager.setRunResult(msg.importWebshopErrorCantopen + "\n" + address + "\n");
        	webshopManager.setRunResult(webshopManager.getRunResult() + "Message: " + e.getLocalizedMessage()+ "\n");
            if (e.getStackTrace().length > 0)
            	webshopManager.setRunResult(webshopManager.getRunResult()+ "Trace: " + e.getStackTrace()[0].toString()+ "\n");

            if (webshopexport != null)
            	webshopManager.setRunResult(webshopManager.getRunResult() + "\n\n" + webshopexport);
            }
        }

	private void interpretWebShopStates(IProgressMonitor localMonitor, Webshopexport webshopexport) {
		webshopManager.setData(webshopexport);
	}
}
