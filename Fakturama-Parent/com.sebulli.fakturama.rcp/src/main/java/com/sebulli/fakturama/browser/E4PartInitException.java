/**
 * 
 */
package com.sebulli.fakturama.browser;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author G527032
 *
 */
public class E4PartInitException extends CoreException {

   /**
     * 
     */
    private static final long serialVersionUID = 7314709608792332149L;

/**
     * Creates a new exception with the given message.
     *
     * @param message the message
     */
    public E4PartInitException(String message) {
    	this(new Status(IStatus.ERROR, "PlatformUI.PLUGIN_ID", 0, message, null));
    }

    /**
     * Creates a new exception with the given message.
     *
     * @param message the message
     * @param nestedException a exception to be wrapped by this PartInitException
     */
    public E4PartInitException(String message, Throwable nestedException) {
    	this(new Status(IStatus.ERROR, "PlatformUI.PLUGIN_ID", 0, message,
                nestedException));
    }

    /**
     * Creates a new exception with the given status object.  The message
     * of the given status is used as the exception message.
     *
     * @param status the status object to be associated with this exception
     */
    public E4PartInitException(IStatus status) {
        super(status);
    }

}
