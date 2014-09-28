/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.webshopimport;

/**
 * @author rheydenr
 *
 */
public final class ExecutionResult {
    private String errorMessage;
    private int errorCode;
    private String runResult;
    /**
     * @param errorMessage
     * @param errorCode
     */
    public ExecutionResult(String errorMessage, int errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
    /**
     * @return the errorMessage
     */
    public final String getErrorMessage() {
        return errorMessage;
    }
    /**
     * @param errorMessage the errorMessage to set
     */
    public final void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    /**
     * @return the errorCode
     */
    public final int getErrorCode() {
        return errorCode;
    }
    /**
     * @param errorCode the errorCode to set
     */
    public final void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
    /**
     * @return the runResult
     */
    public final String getRunResult() {
        return runResult;
    }
    /**
     * @param runResult the runResult to set
     */
    public final void setRunResult(String runResult) {
        this.runResult = runResult;
    }
    
}
