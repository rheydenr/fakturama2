/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.migration;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A (very) simple formatter for formatting the output of the migration log file.
 * It is used only for the output of migration info file. Only {@link Level#INFO}
 * messages are printed. 
 */
public class MigrationLogFormatter extends Formatter {
    
    /**
     * Holds a format string with only <i>one</i> placeholder (the message itself).
     */
    private final String format = "%1$s%n";

    @Override
    public synchronized String format(LogRecord record) {
        String message = formatMessage(record);
        return String.format(format, message);
    }

}
