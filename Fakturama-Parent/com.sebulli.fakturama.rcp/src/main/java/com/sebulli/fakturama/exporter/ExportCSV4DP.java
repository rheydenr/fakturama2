
package com.sebulli.fakturama.exporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.ibm.icu.util.ULocale;
import com.opencsv.CSVWriter;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * Exporter for a single address of a document (invoice, offer etc). Only used
 * for Deutsche Post AG.
 * 
 * @see <a href=
 *      "https://shop.deutschepost.de/shop/pop/popcsvupload.jsp">Deutsche Post
 *      AG</a>
 *
 */
public class ExportCSV4DP implements ICSVExporter {

	@Inject
	@Translation
	protected Messages msg;

	@Inject
	@Optional
	private IPreferenceStore preferences;

	@Inject
	private EPartService partService;

	@Inject
	private ILocaleService localeUtil;
    
    @Inject
    private IEclipseContext context;
    
	@Inject
	private IDocumentAddressManager addressManager;

	private ContactUtil contactUtil;

	@Execute
	public void execute(@Optional @Named(CallEditor.PARAM_CALLING_DOC) String callingDoc,
			final MApplication application, Shell shell) throws ExecutionException {
		MPart activePart = partService.getActivePart();
		DocumentEditor activeEditor = (DocumentEditor) activePart.getObject();
    	contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);
		DocumentReceiver billingContact = addressManager.getBillingAdress(activeEditor.getDocument());
		exportCSV4DP(shell, billingContact);
	}

	@Override
	public Path exportCSV4DP(Shell shell, DocumentReceiver receiver) {
		// Create a File object in workspace
		Path csvFile = Paths.get(preferences.getString(Constants.GENERAL_WORKSPACE), "dp-addressimport-" + receiver.getCustomerNumber() + ".csv");

		// Create a new file
		try (BufferedWriter bos = Files.newBufferedWriter(csvFile, Charset.forName("CP1252"), StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);) {
			HeaderColumnNameMappingStrategy<DPAddress> strategy = new HeaderColumnNameMappingStrategy<>();
			strategy.setColumnOrderOnWrite(new Comparator<String>() {
				public int compare(String o1, String o2) {
					return Integer.compare(DPAddress.HeaderPositions.valueOf(o1).pos, DPAddress.HeaderPositions.valueOf(o2).pos);
				};
			});
			strategy.setType(DPAddress.class);
			StatefulBeanToCsv<DPAddress> beanToCsv = new StatefulBeanToCsvBuilder<DPAddress>(bos)
					.withMappingStrategy(strategy).withLineEnd("\r\n").withSeparator(';').withQuotechar(CSVWriter.NO_QUOTE_CHARACTER).build();
			
			DPAddress dpAddressBean = createDPBean(receiver);
			if(dpAddressBean.isValid()) {
				List<DPAddress> beans = new ArrayList<>();
				beans.add(dpAddressBean);
				beanToCsv.write(beans);
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), msg.dialogMessageboxTitleInfo, MessageFormat.format(msg.commandDocumentsExportAddresscsv4dpExportfinished, csvFile.toString()));
			} else {
				MessageDialog.openError(shell, msg.dialogMessageboxTitleError, msg.commandDocumentsExportAddresscsv4dpEmptyfields);
			}
		} catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | IllegalStateException e) {
			e.printStackTrace();
		}
		return csvFile;
	}

	private DPAddress createDPBean(DocumentReceiver receiverAddress) {
		DPAddress dpAddressBean = new DPAddress();
		dpAddressBean.setSenderName(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_NAME));
		dpAddressBean.setAdditionalSenderName(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_OWNER));
		dpAddressBean.setSenderStreet(contactUtil.getStreetName(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_STREET)));
		dpAddressBean.setSenderHousenumber(contactUtil.getStreetNo(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_STREET)));
		dpAddressBean.setSenderZipCode(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_ZIP));
		dpAddressBean.setSenderCity(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CITY));
        java.util.Optional<ULocale> locale = localeUtil.findLocaleByDisplayCountry(preferences.getString(Constants.PREFERENCES_YOURCOMPANY_CITY));
		dpAddressBean.setSenderISO3Country(locale.orElse(ULocale.getDefault()).getISO3Country());
		
		dpAddressBean.setReceiverName(contactUtil.getCompanyOrLastname(receiverAddress));
		dpAddressBean.setAdditionalReceiverName(contactUtil.getFirstAndLastName(receiverAddress));
		dpAddressBean.setReceiverStreet(contactUtil.getStreetName(receiverAddress.getStreet()));
		dpAddressBean.setReceiverHousenumber(contactUtil.getStreetNo(receiverAddress.getStreet()));
		dpAddressBean.setReceiverZipCode(receiverAddress.getZip());
		dpAddressBean.setReceiverCity(receiverAddress.getCity());
		dpAddressBean.setReceiverISO3Country(localeUtil.findLocaleByDisplayCountry(receiverAddress.getCountryCode()).orElse(ULocale.getDefault()).getISO3Country());
		
		// for the first iteration we use a hard coded product
		dpAddressBean.setProduct("PAECKXS.DEU");
		return dpAddressBean;
	}
//
//	public void createFile() {
//		// Create a "SAVE AS" file dialog
//		FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
//
//		fileDialog.setFilterExtensions(new String[] { "*.csv" });
//
//		// T: Text in a file name dialog
//		fileDialog.setFilterNames(new String[] { msg.exporterFilenameTypeCsv + " (*.csv)" });
//		// T: Text in a file name dialog
//		fileDialog.setText(msg.exporterFilename);
//		fileDialog.setFileName("");
//		fileDialog.setOverwrite(true);
//		String selectedFile = fileDialog.open();
//		if (selectedFile != null) {
//		}
//	}

	@CanExecute
	public boolean canExecute() {
		// can only execute if document is not dirty
		return partService.getActivePart() != null && !partService.getActivePart().isDirty();
	}
}