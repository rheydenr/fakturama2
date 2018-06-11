/**
 * 
 */
package com.sebulli.fakturama.exception;

/**
 * Exception for all things which went wrong while saving anything.
 *
 */
public class FakturamaStoringException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6058534777114190954L;
	
	/**
	 * a description, can be used in error dialogs
	 */
	private String description;
	
	/**
	 * The (re-thrown) exception
	 */
	private Throwable exception;
	
	/**
	 * the data which leads to the exception
	 */
	private Object data;
	
	/**
	 * @param description
	 * @param exception
	 */
	public FakturamaStoringException(String description, Throwable exception) {
		this.description = description;
		this.exception = exception;
	}
	
	/**
	 * @param description
	 * @param exception
	 * @param data
	 */
	public FakturamaStoringException(String description, Throwable exception, Object data) {
		this.description = description;
		this.exception = exception;
		this.data = data;
	}
	/**
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}
	
	public final String getMessage() {
		return description;
	}
	/**
	 * @return the exception
	 */
	public final Throwable getException() {
		return exception;
	}

	/**
	 * @return the data
	 */
	public final Object getData() {
		return data;
	}
	
	
}
