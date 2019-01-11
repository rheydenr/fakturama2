/**
 * 
 */
package com.sebulli.fakturama.exporter;

import java.nio.file.Path;

import com.sebulli.fakturama.model.Contact;

/**
 * Export service for various CSV files (outside a wizard)
 *
 */
public interface ICSVExporter {

	Path exportCSV4DP(Contact contact);
}
