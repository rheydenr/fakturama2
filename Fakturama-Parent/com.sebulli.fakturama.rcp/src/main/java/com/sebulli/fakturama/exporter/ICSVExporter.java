/**
 * 
 */
package com.sebulli.fakturama.exporter;

import java.nio.file.Path;

import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.model.Contact;

/**
 * Export service for various CSV files (outside a wizard)
 *
 */
public interface ICSVExporter {

	Path exportCSV4DP(Shell shell, Contact contact);
}
