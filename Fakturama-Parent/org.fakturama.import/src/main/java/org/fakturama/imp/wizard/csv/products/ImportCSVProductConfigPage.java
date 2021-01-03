/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2020 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package org.fakturama.imp.wizard.csv.products;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.treemapper.ISemanticTreeMapperSupport;
import org.eclipse.nebula.widgets.treemapper.TreeMapper;
import org.eclipse.nebula.widgets.treemapper.TreeMapperUIConfigProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportOptionPage;
import org.fakturama.imp.wizard.ImportOptions;
import org.fakturama.imp.wizard.csv.common.ImportMapping;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.sebulli.fakturama.dao.PropertiesDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.UserProperty;
import com.sebulli.fakturama.parts.widget.contentprovider.SimpleTreeContentProvider;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 * <p>This class is the displays a TreeMapper for mapping product attributes to CSV fields.</p>
 * 
 * <i>NOTE:</i> This class is not used, since the table config variant seems to be a better approach.
 * I've only left this class for educational reasons :-)
 */
public class ImportCSVProductConfigPage extends WizardPage {

    private static final String MAPPING_FIELD_DELIMITER = ":";
    private static final String PRODUCT_SPEC_QUALIFIER = "PRODUCT_MAPPING";
    private static final String MAPPING_DELIMITER = "|";
    private static final String PAGE_NAME = "ImportCSVProductConfigPage";

    @Inject
    @Translation
    protected ImportMessages importMessages;

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    private PropertiesDAO propertiesDAO;

    private ImportOptions options;
    private IEclipseContext ctx;
    private List<ImportMapping> mappings;
    private TreeMapper<ImportMapping, String, Pair<String, String>> treeMapper;
    
    // Defines all columns that are used and imported
    private Map<String, Boolean> requiredHeaders = new HashMap<>();

    private CCombo comboSpecifications;
    private ComboViewer specComboViewer;
    private Button saveSpecButton;
    private Button deleteSpecButton;
    
    public ImportCSVProductConfigPage(String title, String label, ProgramImages image) {
        super(PAGE_NAME);
        setTitle(title);
        setMessage(label);
    }

    /**
     * Default constructor. Used only for injection. <br />
     * WARNING: Use <b>only</b> with injection since some initial values are set
     * in initialize method.
     */
    public ImportCSVProductConfigPage() {
        super(PAGE_NAME);
    }

    @PostConstruct
    public void initialize(IEclipseContext ctx) {
        setTitle((String) ctx.get(ImportOptionPage.WIZARD_TITLE));
        this.ctx = ctx;
        
        String[] reqHdr = new String[] {"itemnumber", "name", "price1"};
        for (String string : reqHdr) {
            requiredHeaders.put(string, Boolean.FALSE);
        }
    }

    @Override
    public void createControl(Composite parent) {

        // Create the top composite
        Composite top = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(4).applyTo(top);
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(top);
        setControl(top);
        setMessage(importMessages.wizardImportCsvGenericCreatemapping);
        
        Label importConfigName = new Label(top, SWT.NONE);
        importConfigName.setText("select specification");
        
        comboSpecifications = new CCombo(top, SWT.BORDER);
        fillSpecificationCombo();
        final ISelectionChangedListener listener = event -> {
            if(!event.getStructuredSelection().isEmpty()) {
                applyMapping(event.getStructuredSelection());
            }
            deleteSpecButton.setEnabled(!event.getStructuredSelection().isEmpty());
            saveSpecButton.setEnabled(false);
        };
        specComboViewer.addSelectionChangedListener(listener);
        specComboViewer.getCCombo().addModifyListener(e -> {
            saveSpecButton.setEnabled(true);
        });
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).grab(true, false).applyTo(comboSpecifications);
        
        saveSpecButton = new Button(top, SWT.PUSH);
        saveSpecButton.setEnabled(false); // enable only if valid mapping is available
        saveSpecButton.setImage(Icon.COMMAND_SAVE.getImage(IconSize.DefaultIconSize));
        saveSpecButton.setToolTipText("save specification");
        saveSpecButton.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                String newText = comboSpecifications.getText();
                UserProperty prop = createSpecFromMapping(newText);
                try {
                    prop = propertiesDAO.save(prop);
                } catch (FakturamaStoringException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                specComboViewer.removeSelectionChangedListener(listener);
                specComboViewer.add(prop);
                specComboViewer.setSelection(new StructuredSelection(prop));
                specComboViewer.addSelectionChangedListener(listener);
                deleteSpecButton.setEnabled(!specComboViewer.getStructuredSelection().isEmpty());
            }
        });
        
        deleteSpecButton = new Button(top, SWT.PUSH);
        deleteSpecButton.setImage(Icon.COMMAND_DELETE.getImage(IconSize.DefaultIconSize));
        deleteSpecButton.setToolTipText("delete specification");
        deleteSpecButton.setEnabled(!specComboViewer.getStructuredSelection().isEmpty());
        deleteSpecButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection structuredSelection = specComboViewer.getStructuredSelection();
                specComboViewer.removeSelectionChangedListener(listener);
                specComboViewer.remove(structuredSelection);
                propertiesDAO.delete((UserProperty) structuredSelection.getFirstElement());
                specComboViewer.addSelectionChangedListener(listener);
                specComboViewer.refresh();
                deleteSpecButton.setEnabled(!specComboViewer.getStructuredSelection().isEmpty());
                saveSpecButton.setEnabled(false);
            }
        });

        createTreeMapperWidget(top);
    }

    /**
     * Read the selected mapping and apply it to the current view.
     * 
     * @param mappingSelection current selection
     */
    private void applyMapping(IStructuredSelection mappingSelection) {
        if (!mappingSelection.isEmpty()) {
            try {

                List<ImportMapping> mappingsTemp = new ArrayList<>();
                UserProperty userProp = (UserProperty) mappingSelection.getFirstElement();
                String mapping = userProp.getValue();
                String[] splittedMapping = mapping.split(Pattern.quote(MAPPING_DELIMITER));

                for (int i = 0; i < splittedMapping.length; i++) {
                    String joinedString = splittedMapping[i];
                    String[] splittedString = joinedString.split(Pattern.quote(MAPPING_FIELD_DELIMITER));
                    if (splittedString.length == 2) {
                        String i18nIdentifier = ProductBeanCSV.getI18NIdentifier(splittedString[1]);
                        mappingsTemp.add(new ImportMapping(splittedString[0], Pair.of(splittedString[1], i18nIdentifier)));
                    }
                }

                mappings.clear();
                mappings.addAll(mappingsTemp);
                treeMapper.refresh();
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("is nich");
            }
        }
    }

    /**
     * Create a new specification (mapping) with the given name.
     * @param specName the name for the spec mapping
     */
    private UserProperty createSpecFromMapping(String specName) {
        UserProperty specMapping = null;
        if (validateMapping()) {
            // check if a mapping with same name exists
            UserProperty existingMapping = propertiesDAO.findByName(specName);
            if (existingMapping != null) {
                // warning dialog???
                specMapping = existingMapping;
            } else {
                specMapping = FakturamaModelPackage.MODELFACTORY.createUserProperty();
                specMapping.setName(specName);
                specMapping.setQualifier(PRODUCT_SPEC_QUALIFIER);
            }
            // mapping is stored in the form csv_field:product_attribute|csv_field:product_attribute
            List<String> collectedMappings = mappings.stream().map(m -> m.getLeftItem() + MAPPING_FIELD_DELIMITER + m.getRightItem().getKey())
                    .collect(Collectors.toList());
            specMapping.setValue(String.join(MAPPING_DELIMITER, collectedMappings));
        }
        return specMapping;
    }

    /**
     * creates the combo box for the stored specifications
     * 
     * @param parent
     */
    private void fillSpecificationCombo() {
        // Collect all specification strings as a sorted Set
        final List<UserProperty> categories = propertiesDAO.findMappingSpecs(PRODUCT_SPEC_QUALIFIER);

        specComboViewer = new ComboViewer(comboSpecifications);
        specComboViewer.setComparer(new IElementComparer() {
            
            @Override
            public int hashCode(Object element) {
                if(element instanceof UserProperty) return ((UserProperty)element).hashCode();
                return 0;
            }
            
            @Override
            public boolean equals(Object a, Object b) {
                // null checks are already done by caller
                //                if(a == null && b == null) return true;
                //                if(a == null && b != null || a != null && b == null) return false;
                if (a instanceof StructuredSelection && b instanceof Vector) {
                    UserProperty up1 = (UserProperty) ((StructuredSelection) a).getFirstElement();
                    @SuppressWarnings("unchecked")
                //  boolean result = ((Vector<UserProperty>) b).stream().anyMatch(p -> p.getId() == up1.getId());
                    UserProperty up2 = ((Vector<UserProperty>) b).firstElement();
                    return up1.getId() == up2.getId();
                } else if (a instanceof StructuredSelection && b instanceof UserProperty) {
                    UserProperty up1 = (UserProperty) ((StructuredSelection) a).getFirstElement();
                    return up1.getId() == ((UserProperty) b).getId();
                }
                return false;
            }
        });
        specComboViewer.setContentProvider(new ArrayContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                return categories.toArray();
            }
        });
        specComboViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return element instanceof UserProperty ? ((UserProperty) element).getName() : element.toString();
            }
        });

        // Add all categories to the combo
        specComboViewer.setInput(categories);

    }

    private void createTreeMapperWidget(Composite parent) {
        Display display = parent.getDisplay();

        Color gray = display.getSystemColor(SWT.COLOR_GRAY);
        Color blue = display.getSystemColor(SWT.COLOR_BLUE);
        TreeMapperUIConfigProvider uiConfig = new TreeMapperUIConfigProvider(gray, 1, blue, 3);
        mappings = new ArrayList<>();
        ISemanticTreeMapperSupport<ImportMapping, String, Pair<String, String>> semanticSupport = new ISemanticTreeMapperSupport<ImportMapping, String, Pair<String, String>>() {
            @Override
            public ImportMapping createSemanticMappingObject(String leftItem, Pair<String, String> rightItem) {
                // create only one mapping (for the left item); delete old mapping if it was created before!
                for (ImportMapping csvMappings : mappings) {
                    if (csvMappings.getLeftItem().equals(leftItem)) {
                        mappings.remove(csvMappings);
                        break;
                    }
                }
                return new ImportMapping(leftItem, rightItem);
            }

            @Override
            public String resolveLeftItem(ImportMapping semanticMappingObject) {
                return semanticMappingObject.getLeftItem();
            }

            @Override
            public Pair<String, String> resolveRightItem(ImportMapping semanticMappingObject) {
                return semanticMappingObject.getRightItem();
            }
        };
        treeMapper = new TreeMapper<>(parent, semanticSupport, uiConfig);

        treeMapper.setContentProviders(new ProductImportContentProvider(), new ProductsFieldContentProvider());
        treeMapper.setLabelProviders(new ViewLabelProvider(), new ViewLabelProvider());

        Canvas cv;
        Control[] controls = treeMapper.getControl().getChildren();
        // it's not possible to access the Canvas directly :-(
        for (Control control : controls) {
            if (control instanceof Canvas) {
                cv = (Canvas) control;
                cv.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        switch (e.keyCode) {
                        case SWT.DEL:
                            ImportMapping selectedMapping = (ImportMapping) treeMapper.getSelection().getFirstElement();
                            if (selectedMapping != null) {
                                mappings.remove(selectedMapping);
                                treeMapper.refresh();
                            }
                            break;

                        default:
                            break;
                        }
                        super.keyPressed(e);
                    }
                });
                break;
            }
        }

        treeMapper.getControl().setWeights(new int[] { 2, 1, 2 });
        treeMapper.addSelectionChangedListener(new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                boolean canFinish = validateMapping();
                
                if(canFinish) {
                    setErrorMessage(null);
                } else {
                    setErrorMessage(String.format(importMessages.wizardImportErrorMissingmappings, 
                            StringUtils.join(requiredHeaders.entrySet().stream().filter(e -> !e.getValue()).map(e -> e.getKey()).collect(Collectors.toList()))));
                }
                
                saveSpecButton.setEnabled(canFinish);
                deleteSpecButton.setEnabled(!specComboViewer.getStructuredSelection().isEmpty());
                options.setMappingAvailable(canFinish);
                setPageComplete(canFinish);
            }
        });
        GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 200).grab(true, true).span(4, 1).applyTo(treeMapper.getControl());

    }

    /**
     * This method is called by wizard to read in the CSV structure and fill the
     * Dual List widget with values.
     */
    public void analyzeCsvFile() {
        options = ctx.get(ImportOptions.class);
        if (options.getCsvFile() != null && !options.isAnalyzeCompleted()) {
            Path inputFile = Paths.get(options.getCsvFile());
            char separator = StringUtils.defaultIfBlank(options.getSeparator(), ";").charAt(0);
            char quoteChar = StringUtils.isNotBlank(options.getQuoteChar()) ? options.getQuoteChar().charAt(0) : '"';
            try (BufferedReader in = Files.newBufferedReader(inputFile)) {

                ICSVParser csvParser = new CSVParserBuilder().withIgnoreLeadingWhiteSpace(true).withSeparator(separator).withQuoteChar(quoteChar).build();
                CSVReader csvr = new CSVReaderBuilder(in).withCSVParser(csvParser).build();
                String[] headerLine = csvr.readNextSilently();
                if(headerLine != null && headerLine.length > 0) {
                    
                    Map<String, Integer> headerToPositions = new HashMap<>();
                    for (int i = 0; i < headerLine.length; i++) {
                        String entry = StringUtils.trim(headerLine[i]);
                        if(StringUtils.isNotBlank(entry)) {
                            headerToPositions.put(entry, i);
                        }
                    }

                    treeMapper.setInput(headerToPositions, "irrelevant", mappings);
                    options.setAnalyzeCompleted(true);
                }
            } catch (IOException e) {
                // T: Error message
                //                result += NL + importMessages.wizardImportErrorOpenfile;
            }
        }
    }

    /**
     * List of {@link ImportMapping}s where <b>any</b> member of CSV file
     * is contained, but can be <code>null</code> if not assigned to a bean attribute.
     * This is necessary for later import, where the mapping have to be complete.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ImportMapping> getCompleteMappings() {
        // complete for missing assignments
        for (String availableColumn : ((HashMap<String, Integer>)treeMapper.getLeftTreeViewer().getInput()).keySet()) {
            if(!mappings.parallelStream().anyMatch(pm -> pm.getLeftItem().equalsIgnoreCase(availableColumn))) {
                mappings.add(ImportMapping.ofNullValue(availableColumn));
            }
        }
        return mappings;
    }
    
    /**
     * Validates if the current mapping has at least all required headers mapped.
     * 
     * @return <code>true</code> if all required headers are mapped, <code>false</code> otherwise
     */
    private boolean validateMapping() {
        boolean canFinish;
        for (String headerName : requiredHeaders.keySet()) {
           if( mappings.stream()
                    .anyMatch(pm -> pm.getRightItem().getKey().equalsIgnoreCase(headerName))) {
               requiredHeaders.put(headerName, Boolean.TRUE);
           }
        }
        
        // can finish only if all required headers are set
        canFinish = !requiredHeaders.values().contains(Boolean.FALSE);
        return canFinish;
    }

    /**
     * Provides all available fields from CSV file.
     *
     */
    public class ProductImportContentProvider extends SimpleTreeContentProvider {
        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(Object inputElement) {
            return ((Map<String, Integer>) inputElement).keySet().toArray(new String[] {});
        }
    }

    class ViewLabelProvider extends LabelProvider {

        @SuppressWarnings("unchecked")
        @Override
        public String getText(Object element) {
            String retval;
            if (element instanceof Pair) {
                retval = ((Pair<String, String>)element).getValue();
            } else {
                retval = super.getText(element);
            }
            return retval;
        }

     }

    /**
     * Provides all selectable fields for product import.
     *
     */
    public class ProductsFieldContentProvider extends SimpleTreeContentProvider {
        private Object[] productAttributes;

        @Override
        public Object[] getElements(Object inputElement) {
            if (productAttributes == null) {
                List<Pair<String, String>> retList = ProductBeanCSV.createProductsAttributeMap(msg)
                        .entrySet()
                        .stream()
                        .map(Pair::of)
                        .sorted(Comparator.comparing(Pair::getValue))
                        .collect(Collectors.toList());
                productAttributes = retList.toArray();
            }
            return productAttributes;
        }
    }
}
