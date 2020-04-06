/**
 * 
 */
package com.sebulli.fakturama.exporter;

import java.nio.file.Path;

import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.model.DocumentReceiver;

/**
 * Export service for various CSV files (outside a wizard)
 *
 */
@FunctionalInterface
public interface ICSVExporter {

	Path exportCSV4DP(Shell shell, DocumentReceiver receiverAddress);
}
