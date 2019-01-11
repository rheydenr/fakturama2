 
package com.sebulli.fakturama.exporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.parts.DocumentEditor;

/**
 * Exporter for a single address of a document (invoice, offer etc). Only used for 
 * Deutsche Post AG.
 * 
 * @see <a href="https://shop.deutschepost.de/shop/pop/popcsvupload.jsp">Deutsche Post AG</a>
 *
 */
public class ExportCSV4DP implements ICSVExporter {

    @Inject
    @Translation
    protected Messages msg;

    @Inject @Optional
    private IPreferenceStore preferences;

	@Inject
	private EPartService partService;
	
	@Execute
	public void execute(@Optional @Named(CallEditor.PARAM_CALLING_DOC) String callingDoc,
			final MApplication application) throws ExecutionException {
		MPart activePart = partService.getActivePart();
		DocumentEditor activeEditor = (DocumentEditor)activePart.getObject();
		Contact billingContact = activeEditor.getDocument().getBillingContact();
		exportCSV4DP(billingContact);
	}	
	

	@Override
	public Path exportCSV4DP(Contact contact) {
		// Create a File object
		Path csvFile = Paths.get("");

		String NEW_LINE = System.lineSeparator();

		// Create a new file
		try (BufferedWriter bos = Files.newBufferedWriter(csvFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);){
			StringBuffer stringBuffer = new StringBuffer();
			
			bos.write(
					"\"id\";"+ 
					"\"category\";"+
					
					"\"gender\";"+
					"\"title\";"+
					"\"firstname\";"+
					"\"name\";"+
					"\"company\";"+
					"\"street\";"+
					"\"zip\";"+
					"\"city\";"+
					"\"country\";"+

					"\"delivery_gender\";"+
					"\"delivery_title\";"+
					"\"delivery_firstname\";"+
					"\"delivery_name\";"+
					"\"delivery_company\";"+
					"\"delivery_street\";"+
					"\"delivery_zip\";"+
					"\"delivery_city\";"+
					"\"delivery_country\";"+
					
					"\"account_holder\";"+
					"\"account\";"+
					"\"bank_code\";"+
					"\"bank_name\";"+
					"\"iban\";"+
					"\"bic\";"+
					
					"\"nr\";"+

					"\"note\";"+
					"\"date_added\";"+
					"\"payment\";"+
					"\"reliability\";"+
					"\"phone\";"+
					"\"fax\";"+
					"\"mobile\";"+
					"\"email\";"+
					"\"website\";"+
					"\"vatnr\";"+
					"\"vatnrvalid\";"+
					"\"discount\";"+
					"\"birthday\""+
					NEW_LINE);
			stringBuffer.append(";");

			bos.write(stringBuffer.toString() + NEW_LINE);
	}
	catch (IOException e) {
	}
		return csvFile;
	}
	
	
	public void createFile() {
		// Create a "SAVE AS" file dialog
		FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		
		fileDialog.setFilterExtensions(new String[] { "*.csv" });

		//T: Text in a file name dialog
		fileDialog.setFilterNames(new String[] { msg.exporterFilenameTypeCsv + " (*.csv)" });
		//T: Text in a file name dialog
		fileDialog.setText(msg.exporterFilename);
		fileDialog.setFileName("");
		fileDialog.setOverwrite(true);
		String selectedFile = fileDialog.open();
		if (selectedFile != null) {
		}
	}

	@CanExecute
	public boolean canExecute() {
		// can only execute if document is not dirty
		return partService.getActivePart() != null && !partService.getActivePart().isDirty(); 
	}
}