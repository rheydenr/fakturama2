package com.sebulli.fakturama.webshopimport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * Runs the reading of a http stream in an extra thread.
 * So it can be interrupted by clicking the cancel button. 
 */
public class InterruptConnection implements Runnable {
    
	// The connection 
	private URLConnection conn;
	
	// Reference to the input stream data
    private InputStream inputStream = null;
    
    // true, if the reading was successful
    private boolean isFinished = false;

    // true, if there was an error
    private boolean isError = false;
    
    
    /**
     * Constructor. Creates a new connection to use it in an extra thread
     * 
     * @param conn
     * 			The connection
     */
    public InterruptConnection(URLConnection conn) {
        this.conn = conn;
    }

    /**
     * Return whether the reading was successful
     * 
     * @return
     * 		True, if the stream was read completely
     */
    public boolean isFinished() {
    	return isFinished;
    }

    /**
     * Return whether the was an error
     * 
     * @return
     * 		True, if there was an error
     */
    public boolean isError() {
    	return isError;
    }
    	    
    /**
     * Returns a reference to the input stream
     * 
     * @return
     * 		Reference to the input stream
     */
    public InputStream getInputStream() {
    	return inputStream;
    }
    
    /**
     * Start reading the input stream 
     */
    public void run() {
        try {
        	inputStream = conn.getInputStream();
        	isFinished = true;
        } catch (IOException e) {
        	isError = true;
		}
    }
}