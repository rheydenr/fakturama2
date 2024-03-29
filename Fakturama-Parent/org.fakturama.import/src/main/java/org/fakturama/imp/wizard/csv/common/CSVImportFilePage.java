package org.fakturama.imp.wizard.csv.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportOptionPage;
import org.fakturama.imp.wizard.ImportOptions;
import org.fakturama.wizards.IFakturamaWizardService;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;

public class CSVImportFilePage extends WizardPage {

    @Inject
    @Translation
    protected ImportMessages importMessages;

    @Inject
    private ImportOptions options;

    @Inject
    @Preference(nodePath="/instance/com.sebulli.fakturama.rcp")
    private IEclipsePreferences eclipsePrefs;

    @Inject
    private ILogger log;

    private Text fileNameField;
    private Image previewImage = null;

    public CSVImportFilePage() {
        super("CSVImportFilePage");
    }

    @PostConstruct
    public void initialize(IEclipseContext ctx) {
        setTitle((String) ctx.get(ImportOptionPage.WIZARD_TITLE));
        this.previewImage = (Image) ctx.get(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE);
    }

    @Override
    public void createControl(Composite parent) {

        // Create the top composite
        Composite top = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(SWT.DEFAULT, 600).applyTo(top);
        setControl(top);

        // Preview image
        if (previewImage != null) {
            Label preview = new Label(top, SWT.BORDER);
            preview.setText(importMessages.wizardCommonPreviewLabel);
            GridDataFactory.swtDefaults().span(2, 1).align(SWT.BEGINNING, SWT.CENTER).applyTo(preview);
            try {
                preview.setImage(previewImage);
              //  parent.getShell().setSize(previewImage.getImageData().width, previewImage.getImageData().height);
            }
            catch (Exception e) {
                log.error(e, "Icon not found");
            }
        }

        // Create the label with the help text
        setMessage(importMessages.wizardImportCsvSelectfile);

        fileNameField = new Text(top, SWT.BORDER);
        UpdateValueStrategy<String, String> strat = new UpdateValueStrategy<>();
        strat.setBeforeSetValidator(filename -> {
            if (isFilenameValid(filename)) {
                setPageComplete(true);
                return ValidationStatus.ok();
            } else {
                options.setCsvFile(null);
                setPageComplete(false);
                return ValidationStatus.error(importMessages.wizardImportErrorWrongfilename);
            }
        });

        DataBindingContext bindingContext = new DataBindingContext();

        IObservableValue<String> model = PojoProperties.value(ImportOptions.class, "csvFile", String.class).observe(options);
        IObservableValue<String> uiWidget = WidgetProperties.text(SWT.FocusOut).observe(fileNameField);
        final Binding bindValue = bindingContext.bindValue(uiWidget, model, strat, null);
        ControlDecorationSupport.create(bindValue, SWT.LEFT | SWT.TOP);

        fileNameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                getContainer().updateButtons();
            }
        });
        
        fileNameField.addModifyListener(l -> {
             // reset analysis result since we could get a new CSV file
            if(options != null) {
                options.setAnalyzeCompleted(false);
            }
           
        });
        
        GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).applyTo(fileNameField);
        Button ellipsis = new Button(top, SWT.PUSH);
        ellipsis.setText(JFaceResources.getString("openBrowse"));
        ellipsis.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String startingDirectory;
                if (!fileNameField.getText().isEmpty()) {
                    startingDirectory = fileNameField.getText();
                } else {
                    startingDirectory = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null);
                }
                Path startDir = Paths.get(startingDirectory);
                options.setCsvFile(getFile(startDir));
                if (options.getCsvFile() != null) {
                    fileNameField.setText(options.getCsvFile());
                    bindValue.validateModelToTarget();
                    getContainer().updateButtons();
                }
            }
        });
    }
    
    private boolean isFilenameValid(String filename) {
        boolean retval = true;
        if(StringUtils.isNotBlank(filename)) {
            Path testFile = Paths.get(filename);
            retval = Files.exists(testFile);
        }
        return retval;
    }
    
    @Override
    public boolean isPageComplete() {
        return options.isMappingAvailable() && StringUtils.isNotBlank(options.getCsvFile());
    }

    @Override
    public boolean canFlipToNextPage() {
        return StringUtils.isNotBlank(options.getCsvFile());
    }

    /**
     * Helper to open the file chooser dialog.
     * 
     * @param startingDirectory
     *            the directory to open the dialog on.
     * @return File The File the user selected or null if they do not.
     */
    private String getFile(Path startingDirectory) {
        
        FileDialog fileDialog = new FileDialog(this.getShell());
        fileDialog.setFilterExtensions(new String[] { "*.csv" });

        // Start at the user's home or use the previously set filename
        if (startingDirectory != null) {
            fileDialog.setFilterPath(startingDirectory.toString());
         } else {
            startingDirectory = Paths.get(System.getProperty("user.home"));
       }
        fileDialog.setFilterPath(startingDirectory.toString());
        
        //T: CSV Import File Dialog Title
        fileDialog.setText(importMessages.wizardImportDialogSelectfile);

        //T: CSV Import File Filter
        fileDialog.setFilterNames(new String[] { importMessages.wizardImportCsvInfo+ " (*.csv)" });
        String file = fileDialog.open();
        
        if (file != null) {
            file = file.trim();
            if (!file.isEmpty()) {
                return file;
            }
        }

        return null;
    }

}
