package org.fakturama.imp.wizard.csv.products;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportOptionPage;
import org.fakturama.imp.wizard.ImportOptions;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.sebulli.fakturama.dao.PropertiesDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.UserProperty;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.resources.core.ProgramImages;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

public class ImportCSVProductConfigTablePage extends WizardPage {

    private static final String MAPPING_FIELD_DELIMITER = ":";
    private static final String PRODUCT_SPEC_QUALIFIER = "PRODUCT_MAPPING";
    private static final String MAPPING_DELIMITER = "|";
    private static final String PAGE_NAME = "ImportCSVProductConfigPage";
    
    private static final String TEXT_CELL_LABEL = "Text_Cell_LABEL";
    private static final String PRODUCTFIELD_CELL_LABEL = "ProductField_Cell_LABEL";
    private static final String DESCRIPTION_CELL_LABEL = "Description_Cell_LABEL";

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
    private List<ProductImportMapping> mappings;
    
    // Defines all columns that are used and imported
    private Map<String, Boolean> requiredHeaders = new HashMap<>();
    private EventList<String> csvHeaders = GlazedLists.eventList(new ArrayList<String>());
    private EventList<ProductImportMapping> sortedList;
    
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();

    private CCombo comboSpecifications;
    private ComboViewer specComboViewer;
    private Button saveSpecButton;
    private Button deleteSpecButton;
    private NatTable natTable;
    
    public ImportCSVProductConfigTablePage(String title, String label, ProgramImages image) {
        super(PAGE_NAME);
        setTitle(title);
        setMessage(label);
    }

    /**
     * Default constructor. Used only for injection. <br />
     * WARNING: Use <b>only</b> with injection since some initial values are set
     * in initialize method.
     */
    public ImportCSVProductConfigTablePage() {
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
        setMessage(importMessages.wizardImportCsvProductsCreatemapping);
        
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

        createListTable(top);
    }

    
    private NatTable createListTable(Composite searchAndTableComposite) {
        mappings = new ArrayList<>();

        // get the visible properties to show in list view
        String[] propertyNames = new String[] {"leftItem", "rightItem"};
        final IColumnAccessor<ProductImportMapping> accessor = new ProductCsvColumnAccessor();
//
//        IRowIdAccessor<ProductImportMapping> rowIdAccessor = new IRowIdAccessor<ProductImportMapping>() {
//            @Override
//            public Serializable getRowId(ProductImportMapping rowObject) {
//                return rowObject.getId();
//            }
//        };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("leftItem", "import field");
        propertyToLabelMap.put("rightItem", "product attribute");

        sortedList = GlazedLists.eventList(csvHeaders.stream().map(c -> new ProductImportMapping(c, null)).collect(Collectors.toList()));

        IDataProvider bodyDataProvider = new ListDataProvider<>(sortedList, accessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        GlazedListsEventLayer<ProductImportMapping> eventLayer = new GlazedListsEventLayer<>(bodyDataLayer, sortedList);

        SelectionLayer selectionLayer = new SelectionLayer(eventLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(new DefaultColumnHeaderDataLayer(columnHeaderDataProvider), viewportLayer, selectionLayer);

        // build the row header layer
        IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(new DefaultRowHeaderDataLayer(rowHeaderDataProvider), viewportLayer, selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        ILayer cornerLayer = new CornerLayer(new DataLayer(cornerDataProvider), rowHeaderLayer, columnHeaderLayer);

        // build the grid layer
        GridLayer gridLayer = new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);

        // add default column labels to the label stack
        // need to be done on the column header data layer, otherwise the label
        // stack does not contain the necessary labels at the time the
        // comparator is searched
        ColumnOverrideLabelAccumulator cellLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
        cellLabelAccumulator.registerColumnOverrides(1, PRODUCTFIELD_CELL_LABEL);
        // Register label accumulator
        bodyDataLayer.setConfigLabelAccumulator(cellLabelAccumulator);

        natTable = new NatTable(searchAndTableComposite, gridLayer, false);
        natTable.setConfigRegistry(configRegistry);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration manually
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new ProductImportCsvMappingTableConfiguration());
//      natTable.setBackground(GUIHelper.COLOR_WHITE);
        
        natTable.configure();
        GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(natTable);

        return natTable;
    }
    /**
     * Registers the configuration for the product field column.
     * 
     * @param configRegistry the config registry
     * @param styleRightAligned a style attribute
     */
    private void registerProductFieldColumn(IConfigRegistry configRegistry, Style styleRightAligned) {
        //register a combobox editor for product field values 
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, PRODUCTFIELD_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, PRODUCTFIELD_CELL_LABEL); 
            configRegistry.registerConfigAttribute( 
                    CellConfigAttributes.CELL_PAINTER, 
                    new ComboBoxPainter(), 
                    DisplayMode.NORMAL, PRODUCTFIELD_CELL_LABEL);
            
            CsvFieldComboProvider dataProvider = new CsvFieldComboProvider(msg);
            
            ComboBoxCellEditor productFieldValueCombobox = new ComboBoxCellEditor(dataProvider);
            productFieldValueCombobox.setFreeEdit(false);
            configRegistry.registerConfigAttribute( 
                    EditConfigAttributes.CELL_EDITOR, 
                    productFieldValueCombobox, 
                    DisplayMode.NORMAL, PRODUCTFIELD_CELL_LABEL); 
            configRegistry.registerConfigAttribute( 
                    CellConfigAttributes.DISPLAY_CONVERTER, 
                    new ProductMappingDisplayConverter(), 
                    DisplayMode.NORMAL, PRODUCTFIELD_CELL_LABEL);
    }

    /**
     * Read the selected mapping and apply it to the current view.
     * 
     * @param mappingSelection current selection
     */
    private void applyMapping(IStructuredSelection mappingSelection) {
        if (!mappingSelection.isEmpty()) {
            try {

                List<ProductImportMapping> mappingsTemp = new ArrayList<>();
                UserProperty userProp = (UserProperty) mappingSelection.getFirstElement();
                String mapping = userProp.getValue();
                String[] splittedMapping = mapping.split(Pattern.quote(MAPPING_DELIMITER));

                for (int i = 0; i < splittedMapping.length; i++) {
                    String joinedString = splittedMapping[i];
                    String[] splittedString = joinedString.split(Pattern.quote(MAPPING_FIELD_DELIMITER));
                    if (splittedString.length == 2) {
                        String i18nIdentifier = ProductBeanCSV.getI18NIdentifier(splittedString[1]);
                        mappingsTemp.add(new ProductImportMapping(splittedString[0], Pair.of(splittedString[1], i18nIdentifier)));
                    }
                }

                mappings.clear();
                mappings.addAll(mappingsTemp);
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
                if (element instanceof UserProperty)
                    return ((UserProperty) element).hashCode();
                return 0;
            }

            @Override
            public boolean equals(Object a, Object b) {
                if (a instanceof StructuredSelection && b instanceof Vector) {
                    UserProperty up1 = (UserProperty) ((StructuredSelection) a).getFirstElement();
                    @SuppressWarnings("unchecked")
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

                    csvHeaders = GlazedLists.eventList(headerToPositions.keySet());
                    sortedList.clear();
                    sortedList.addAll(
                            GlazedLists.eventList(csvHeaders.stream().map(c -> new ProductImportMapping(c, null)).collect(Collectors.toList())));
//                    natTable.refresh();
                    options.setAnalyzeCompleted(true);
                }
            } catch (IOException e) {
                // T: Error message
                //                result += NL + importMessages.wizardImportErrorOpenfile;
            }
        }
    }

    /**
     * List of {@link ProductImportMapping}s where <b>any</b> member of CSV file
     * is contained, but can be <code>null</code> if not assigned to a bean attribute.
     * This is necessary for later import, where the mapping have to be complete.
     * 
     * @return
     */
    public List<ProductImportMapping> getCompleteMappings() {
        // complete for missing assignments
        for (String availableColumn : csvHeaders) {
            if(!mappings.parallelStream().anyMatch(pm -> pm.getLeftItem().equalsIgnoreCase(availableColumn))) {
                mappings.add(ProductImportMapping.ofNullValue(availableColumn));
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
           if(mappings.stream()
                    .anyMatch(pm -> pm.getRightItem().getKey().equalsIgnoreCase(headerName))) {
               requiredHeaders.put(headerName, Boolean.TRUE);
           }
        }
        
        // can finish only if all required headers are set
        canFinish = !requiredHeaders.values().contains(Boolean.FALSE);
        return canFinish;
    }
    
    class ProductCsvColumnAccessor implements IColumnAccessor<ProductImportMapping> {

        @Override
        public Object getDataValue(ProductImportMapping rowObject, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return rowObject.getLeftItem();
                case 1:
                    return rowObject.getRightItem();
            }
            return "";
        }

        @SuppressWarnings("unchecked")
        @Override
        public void setDataValue(ProductImportMapping rowObject, int columnIndex, Object newValue) {
            switch (columnIndex) {
                case 0:
                    // unsettable! 
                    break;
                case 1:
                    rowObject.setRightItem((ImmutablePair<String, String>) newValue);
                    createMappingEntry(rowObject);
                    checkCompleteness();
                    break;
            }
        }

        @Override
        public int getColumnCount() {
            return 2;
        }
    }

    private void createMappingEntry(ProductImportMapping rowObject) {
        // create only one mapping (for the left item); delete old mapping if it was created before!
        for (ProductImportMapping csvMapping : mappings) {
            if (csvMapping.getLeftItem().equals(rowObject.getLeftItem())) {
                mappings.remove(csvMapping);
                break;
            }
        }
        mappings.add(rowObject);

    }

    /**
     * Checks if the mapping contains all necessary headers. Sets the button status if mapping is complete.
     */
    private void checkCompleteness() {
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

    class ProductImportCsvMappingTableConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            Style styleLeftAligned = new Style();
            styleLeftAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
            Style styleRightAligned = new Style();
            styleRightAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
            Style styleCentered = new Style();
            styleCentered.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
            
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
                                                   styleLeftAligned,                // value of the attribute
                                                   DisplayMode.NORMAL,              // apply during normal rendering i.e not during selection or edit
                                                   GridRegion.BODY.toString());     // apply the above for all cells with this label
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, TEXT_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, DESCRIPTION_CELL_LABEL);
            
            // configure to open the adjacent editor after commit
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.OPEN_ADJACENT_EDITOR,
                    Boolean.TRUE);

            registerProductFieldColumn(configRegistry, styleRightAligned); 
        }
    }
}
