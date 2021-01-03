package org.fakturama.imp.wizard.csv.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
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
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.UserProperty;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.resources.core.ProgramImages;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

public class ImportCSVConfigTablePage extends WizardPage {
    
    
    public static final String PARAM_REQUIRED_HEADERS = "REQUIRED_HEADERS";
    public static final String PARAM_MAPPING_MESSAGE = "PARAM_MAPPING_MESSAGE";
    public static final String PARAM_SPEC_QUALIFIER = "PARAM_SPEC_QUALIFIER";
    public static final String PARAM_SPEC_NAME = "PARAM_SPEC_NAME";
    
    public static final String PRODUCT_SPEC_QUALIFIER = "PRODUCT_MAPPING";
    public static final String CONTACTS_SPEC_QUALIFIER = "CONTACTS_MAPPING";

    private static final char DEFAULT_QUOTE_CHAR = '"';
    private static final String DEFAULT_SEPARATOR = ";";
    private static final String MAPPING_FIELD_DELIMITER = ":";
    private static final String MAPPING_DELIMITER = "|";
    private static final String PAGE_NAME = "ImportCSVConfigTablePage";
    
    /**
     * Maximal length of the property value field in the database. Since the mapping is stored
     * in {@link UserProperty} table you can only use a limited length. May be improved later on.
     */
    private static final int MAX_PROPERTY_VALUE_LENGTH = 254;
    
    private static final String TEXT_CELL_LABEL = "Text_Cell_LABEL";
    private static final String ENTITYFIELD_CELL_LABEL = "EntityField_Cell_LABEL";
    private static final String DESCRIPTION_CELL_LABEL = "Description_Cell_LABEL";

    @Inject
    @Translation
    protected ImportMessages importMessages;

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    protected ILogger log;

    @Inject
    private PropertiesDAO propertiesDAO;

    private ImportOptions options;
    private IEclipseContext ctx;
    private EventList<ImportMapping> mappings;
    
    // Defines all columns that are used and imported
    private Map<String, Boolean> requiredHeaders = new HashMap<>();
    private EventList<String> csvHeaders = GlazedLists.eventList(new ArrayList<String>());
    
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();

    private CCombo comboSpecifications;
    private ComboViewer specComboViewer;
    private Button saveSpecButton;
    private Button deleteSpecButton;
    private NatTable natTable;
    private IComboBoxDataProvider dataProvider;
    private Function<String, String> i18nMappingFunction;
    
    public ImportCSVConfigTablePage(String title, String label, ProgramImages image) {
        super(PAGE_NAME);
        setTitle(title);
        setMessage(label);
    }

    /**
     * Default constructor. Used only for injection. <br />
     * WARNING: Use <b>only</b> with injection since some initial values are set
     * in initialize method.
     */
    public ImportCSVConfigTablePage() {
        super(PAGE_NAME);
    }

    @PostConstruct
    public void initialize(IEclipseContext ctx) {
        setTitle((String) ctx.get(ImportOptionPage.WIZARD_TITLE));
        this.ctx = ctx;

        String[] reqHdr = (String[]) ctx.get(PARAM_REQUIRED_HEADERS);
        Arrays.stream(reqHdr).forEach(h -> requiredHeaders.put(h, Boolean.FALSE));
    }

    @Override
    public void createControl(Composite parent) {

        // Create the top composite
        Composite top = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(4).applyTo(top);
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(top);
        setControl(top);
        setMessage(MessageFormat.format(importMessages.wizardImportCsvGenericCreatemapping, (String)ctx.get(PARAM_SPEC_NAME)));
        
        Label importConfigName = new Label(top, SWT.NONE);
        importConfigName.setText(importMessages.wizardImportCsvSpecSelect);
        
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
            saveSpecButton.setEnabled(StringUtils.isNotBlank(comboSpecifications.getText()));
        });
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).grab(true, false).applyTo(comboSpecifications);
        
        saveSpecButton = new Button(top, SWT.PUSH);
        saveSpecButton.setEnabled(false); // enable only if valid mapping is available
        saveSpecButton.setImage(Icon.COMMAND_SAVE.getImage(IconSize.DefaultIconSize));
        saveSpecButton.setToolTipText(importMessages.wizardImportCsvSpecSaveTooltip);
        saveSpecButton.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                String newText = comboSpecifications.getText();
                UserProperty prop = createSpecFromMapping(newText);
                boolean isUpdated = true;
                try {
                    // later on we can increase the database field
                    if(prop.getValue().length() >= MAX_PROPERTY_VALUE_LENGTH) {
                        MessageDialog.openError(getShell(), msg.dialogMessageboxTitleError, 
                                "Mapping can't be stored because there are too many mappings "
                                + "(database field is too small for it). Please reduce the count "
                                + "of your mappings and try again. Sorry for the inconveniences.");
                        isUpdated = false;
                    } else {
                        prop = propertiesDAO.save(prop);
                    }
                } catch (FakturamaStoringException e1) {
                    log.error(e1);
                }
                if(isUpdated) {
                    specComboViewer.removeSelectionChangedListener(listener);
                    specComboViewer.add(prop);
                    specComboViewer.setSelection(new StructuredSelection(prop));
                    specComboViewer.addSelectionChangedListener(listener);
                    deleteSpecButton.setEnabled(!specComboViewer.getStructuredSelection().isEmpty());
                }
            }
        });
        
        deleteSpecButton = new Button(top, SWT.PUSH);
        deleteSpecButton.setImage(Icon.COMMAND_DELETE.getImage(IconSize.DefaultIconSize));
        deleteSpecButton.setToolTipText(importMessages.wizardImportCsvSpecDeleteTooltip);
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
        // get the visible properties to show in list view
        String[] propertyNames = new String[] {"leftItem", "rightItem"};
        final IColumnAccessor<ImportMapping> accessor = new BeanCsvColumnAccessor();

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put(propertyNames[0], importMessages.wizardImportCsvGenericSource);
        propertyToLabelMap.put(propertyNames[1], MessageFormat.format(importMessages.wizardImportCsvGenericTarget, (String)ctx.get(PARAM_SPEC_NAME)));

        // initialize mappings, set each field to "unassigned"
        mappings = GlazedLists.eventList(csvHeaders.stream().map(c -> new ImportMapping(c, null)).collect(Collectors.toList()));

        IDataProvider bodyDataProvider = new ListDataProvider<>(mappings, accessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        GlazedListsEventLayer<ImportMapping> eventLayer = new GlazedListsEventLayer<>(bodyDataLayer, mappings);

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
        // stack does not contain the necessary labels at the time the comparator is searched
        bodyDataLayer.setColumnWidthByPosition(0, 150); // use fixed size for the moment
        bodyDataLayer.setColumnWidthByPosition(1, 150);
        ColumnOverrideLabelAccumulator cellLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
        cellLabelAccumulator.registerColumnOverrides(1, ENTITYFIELD_CELL_LABEL);
        // Register label accumulator
        bodyDataLayer.setConfigLabelAccumulator(cellLabelAccumulator);

        natTable = new NatTable(searchAndTableComposite, gridLayer, false);
        natTable.setConfigRegistry(configRegistry);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration manually
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new BeanImportCsvMappingTableConfiguration());
        
        natTable.configure();
        GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(natTable);

        return natTable;
    }

    /**
     * Read the selected mapping and apply it to the current view.
     * 
     * @param mappingSelection current selection
     */
    private void applyMapping(IStructuredSelection mappingSelection) {
        if (!mappingSelection.isEmpty()) {
            try {
                // at first clear old mappings
                mappings.forEach(p -> p.setRightItem(null));
                requiredHeaders = requiredHeaders.keySet().stream().collect(Collectors.toMap(Function.identity(), k -> Boolean.FALSE));

                UserProperty userProp = (UserProperty) mappingSelection.getFirstElement();
                String mapping = userProp.getValue();
                String[] splittedMapping = mapping.split(Pattern.quote(MAPPING_DELIMITER));

                for (int i = 0; i < splittedMapping.length; i++) {
                    String joinedString = splittedMapping[i];
                    String[] splittedString = joinedString.split(Pattern.quote(MAPPING_FIELD_DELIMITER));
                    if (splittedString.length == 2) {
                        String i18nIdentifier = getI18NMappingFunction().apply(splittedString[1]);
                        Optional<ImportMapping> existingBeanMappingEntry = mappings.stream().filter(p -> p.getLeftItem().contentEquals(splittedString[0]))
                                .findAny();
                        existingBeanMappingEntry.ifPresent(p -> p.setRightItem(Pair.of(splittedString[1], i18nIdentifier)));
                    }
                }

                checkCompleteness();

                natTable.refresh();
            } catch (ArrayIndexOutOfBoundsException e) {
                log.error("mapping can't be applied! " + e.getMessage());
            }
        }
    }

    /**
     * Registers the configuration for the bean field column.
     * 
     * @param configRegistry the config registry
     * @param styleRightAligned a style attribute
     */
    private void registerEntityFieldColumn(IConfigRegistry configRegistry, Style styleRightAligned) {
        //register a combobox editor for bean field values 
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, ENTITYFIELD_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, ENTITYFIELD_CELL_LABEL); 
            configRegistry.registerConfigAttribute( 
                    CellConfigAttributes.CELL_PAINTER, 
                    new ComboBoxPainter(), 
                    DisplayMode.NORMAL, ENTITYFIELD_CELL_LABEL);
            
            ComboBoxCellEditor beanFieldValueCombobox = new ComboBoxCellEditor(getDataProvider(), 10);
            beanFieldValueCombobox.setFreeEdit(false);
            beanFieldValueCombobox.setShowDropdownFilter(true);
            configRegistry.registerConfigAttribute( 
                    EditConfigAttributes.CELL_EDITOR, 
                    beanFieldValueCombobox, 
                    DisplayMode.NORMAL, ENTITYFIELD_CELL_LABEL); 
            configRegistry.registerConfigAttribute( 
                    CellConfigAttributes.DISPLAY_CONVERTER, 
                    new MappingDisplayConverter(), 
                    DisplayMode.NORMAL, ENTITYFIELD_CELL_LABEL);
    }

    protected IComboBoxDataProvider getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(IComboBoxDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
     * Create a new specification (mapping) with the given name.
     * @param specName the name for the spec mapping
     */
    private UserProperty createSpecFromMapping(String specName) {
        UserProperty specMapping = null;
        // check if a mapping with same name exists
        UserProperty existingMapping = propertiesDAO.findByName(specName);
        if (existingMapping != null) {
            // warning dialog???
            specMapping = existingMapping;
        } else {
            specMapping = FakturamaModelPackage.MODELFACTORY.createUserProperty();
            specMapping.setName(specName);
            specMapping.setQualifier((String)ctx.get(PARAM_SPEC_QUALIFIER));
        }
        // mapping is stored in the form csv_field:bean_attribute|csv_field:bean_attribute
        List<String> collectedMappings = mappings.stream()
                // filter out empty entries
                .filter(m -> m.getRightItem() != null && !BeanCsvFieldComboProvider.EMPTY_ENTRY.contentEquals(m.getLeftItem()))
                .map(m -> String.format("%s%s%s", m.getLeftItem(), MAPPING_FIELD_DELIMITER, m.getRightItem().getKey())).collect(Collectors.toList());
        specMapping.setValue(String.join(MAPPING_DELIMITER, collectedMappings));
        return specMapping;
    }

    /**
     * creates the combo box for the stored specifications
     * 
     * @param parent
     */
    private void fillSpecificationCombo() {
        // Collect all specification strings as a sorted Set
        final List<UserProperty> categories = propertiesDAO.findMappingSpecs((String) ctx.get(PARAM_SPEC_QUALIFIER));

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
            char separator = StringUtils.defaultIfBlank(options.getSeparator(), DEFAULT_SEPARATOR).charAt(0);
            char quoteChar = StringUtils.isNotBlank(options.getQuoteChar()) ? options.getQuoteChar().charAt(0) : DEFAULT_QUOTE_CHAR;
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
                    mappings.clear();
                    mappings.addAll(
                            GlazedLists.eventList(csvHeaders.stream().map(c -> new ImportMapping(c, null)).collect(Collectors.toList())));

                    options.setAnalyzeCompleted(true);
                    comboSpecifications.clearSelection();
                    comboSpecifications.setText("");
                }
            } catch (IOException e) {
                // T: Error message
                log.error(e, importMessages.wizardImportErrorOpenfile);
            }
        }
    }
    
    private Function<String, String> getI18NMappingFunction() {
        return i18nMappingFunction;
    }

    /**
     * Sets the mapping function which determines the given field name in the current localization.
     * 
     * @param i18nMappingFunction
     */
    public void setI18nMappingFunction(Function<String, String> i18nMappingFunction) {
        this.i18nMappingFunction = i18nMappingFunction;
    }

    /**
     * List of {@link ImportMapping}s where <b>any</b> member of CSV file
     * is contained, but can be <code>null</code> if not assigned to a bean attribute.
     * This is necessary for later import, where the mapping have to be complete.
     * 
     * @return
     */
    public List<ImportMapping> getCompleteMappings() {
        // complete for missing assignments
        for (String availableColumn : csvHeaders) {
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
        for (String headerName : requiredHeaders.keySet()) {
           if(mappings.stream()
                    .anyMatch(pm -> pm.getRightItem() != null && pm.getRightItem().getKey().equalsIgnoreCase(headerName))) {
               requiredHeaders.put(headerName, Boolean.TRUE);
           }
        }
        
        // can finish only if all required headers are set
        return !requiredHeaders.values().contains(Boolean.FALSE);
    }
    
    class BeanCsvColumnAccessor implements IColumnAccessor<ImportMapping> {


        @Override
        public Object getDataValue(ImportMapping rowObject, int columnIndex) {
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
        public void setDataValue(ImportMapping rowObject, int columnIndex, Object newValue) {
            switch (columnIndex) {
            case 0:
                // unsettable! 
                break;
            case 1:
                // create only one mapping (per left item); delete old mapping if it was created before!
                @SuppressWarnings("rawtypes")
                Optional<ImportMapping> oldMappingEntry = mappings.stream()
                        .filter(p -> p.getRightItem() != null && p.getRightItem().getKey().equals(((ImmutablePair) newValue).getLeft())).findAny();
                oldMappingEntry.ifPresent(p -> p.setRightItem(null));
                ImmutablePair<String, String> newMappingEntry = (ImmutablePair<String, String>) newValue;
                if (BeanCsvFieldComboProvider.EMPTY_ENTRY.contentEquals(newMappingEntry.getLeft())) {
                    rowObject.setRightItem(null);
                } else {
                    rowObject.setRightItem(newMappingEntry);
                }
                checkCompleteness();
                break;
            }
        }

        @Override
        public int getColumnCount() {
            return 2;
        }
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
                    StringUtils.join(requiredHeaders.entrySet().stream()
                            .filter(e -> !e.getValue())
                            .map(e -> getI18NMappingFunction().apply(e.getKey()))
                            .collect(Collectors.toList()))));
        }
        
        saveSpecButton.setEnabled(canFinish && StringUtils.isNotBlank(comboSpecifications.getText()));
        deleteSpecButton.setEnabled(!specComboViewer.getStructuredSelection().isEmpty());
        options.setMappingAvailable(canFinish);
        setPageComplete(canFinish);
    }

    class BeanImportCsvMappingTableConfiguration extends AbstractRegistryConfiguration {

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

            registerEntityFieldColumn(configRegistry, styleRightAligned); 
        }
    }
}
