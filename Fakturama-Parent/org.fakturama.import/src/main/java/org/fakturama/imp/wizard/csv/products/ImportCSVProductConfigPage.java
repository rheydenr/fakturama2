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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.treemapper.ISemanticTreeMapperSupport;
import org.eclipse.nebula.widgets.treemapper.TreeMapper;
import org.eclipse.nebula.widgets.treemapper.TreeMapperUIConfigProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.parts.widget.contentprovider.SimpleTreeContentProvider;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 *
 */
public class ImportCSVProductConfigPage extends WizardPage {

    private static final String PAGE_NAME = "ImportOptionConfigPage";

    @Inject
    @Translation
    protected ImportMessages importMessages;

    @Inject
    @Translation
    protected Messages msg;

    private ImportOptions options;
    private IEclipseContext ctx;
    private List<ProductImportMapping> mappings;
    private TreeMapper<ProductImportMapping, String, Pair<String, String>> treeMapper;
    
    // Defines all columns that are used and imported
    private Map<String, Boolean> requiredHeaders = new HashMap<>();

    private CCombo comboSpecifications;

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
        setMessage(importMessages.wizardImportCsvProductsCreatemapping);
        
        Label importConfigName = new Label(top, SWT.NONE);
        importConfigName.setText("select specification");
        
        comboSpecifications = new CCombo(top, SWT.BORDER);
        fillSpecificationCombo();
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).grab(true, false).applyTo(comboSpecifications);
        
        Button saveSpec = new Button(top, SWT.PUSH);
        saveSpec.setText("save");
        
        Button deleteSpec = new Button(top, SWT.PUSH);
        deleteSpec.setText("delete");

        createTreeMapperWidget(top);
    }

    /**
     * creates the combo box for the stored specifications
     * @param parent 
     */
    private void fillSpecificationCombo() {
        // Collect all specification strings as a sorted Set
        final List<String> categories = new ArrayList<>();
//        categories.addAll(productCategoriesDAO.findAll());

        ComboViewer viewer = new ComboViewer(comboSpecifications);
        viewer.setContentProvider(new ArrayContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                return categories.toArray();
            }
        });
        
        // Add all categories to the combo
        viewer.setInput(categories);
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return element instanceof ProductCategory ? CommonConverter.getCategoryName((ProductCategory)element, "") : null;
            }
        });

//        UpdateValueStrategy<ProductCategory, String> productCatModel2Target = UpdateValueStrategy.create(new CategoryConverter<ProductCategory>(ProductCategory.class));
//        UpdateValueStrategy<String, ProductCategory> target2productCatModel = UpdateValueStrategy.create(new StringToCategoryConverter<ProductCategory>(categories, ProductCategory.class));
//        bindModelValue(editorProduct, comboCategory, Product_.categories.getName(), target2productCatModel, productCatModel2Target);
    }


    private void createTreeMapperWidget(Composite parent) {
        Display display = parent.getDisplay();

        Color gray = display.getSystemColor(SWT.COLOR_GRAY);
        Color blue = display.getSystemColor(SWT.COLOR_BLUE);
        TreeMapperUIConfigProvider uiConfig = new TreeMapperUIConfigProvider(gray, 1, blue, 3);
        mappings = new ArrayList<>();
        ISemanticTreeMapperSupport<ProductImportMapping, String, Pair<String, String>> semanticSupport = new ISemanticTreeMapperSupport<ProductImportMapping, String, Pair<String, String>>() {
            @Override
            public ProductImportMapping createSemanticMappingObject(String leftItem, Pair<String, String> rightItem) {
                // create only one mapping (for the left item); delete old mapping if it was created before!
                for (ProductImportMapping csvMappings : mappings) {
                    if (csvMappings.getLeftItem().equals(leftItem)) {
                        mappings.remove(csvMappings);
                        break;
                    }
                }
                return new ProductImportMapping(leftItem, rightItem);
            }

            @Override
            public String resolveLeftItem(ProductImportMapping semanticMappingObject) {
                return semanticMappingObject.getLeftItem();
            }

            @Override
            public Pair<String, String> resolveRightItem(ProductImportMapping semanticMappingObject) {
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
                            ProductImportMapping selectedMapping = (ProductImportMapping) treeMapper.getSelection().getFirstElement();
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
                boolean canFinish = false;
                for (String headerName : requiredHeaders.keySet()) {
                   if( mappings.stream()
                            .anyMatch(pm -> pm.getRightItem().getKey().equalsIgnoreCase(headerName))) {
                       requiredHeaders.put(headerName, Boolean.TRUE);
                   }
                }
                
                // can finish only if all required headers are set
                canFinish = !requiredHeaders.values().contains(Boolean.FALSE);
                
                if(canFinish) {
                    setErrorMessage(null);
                } else {
                    setErrorMessage(String.format(importMessages.wizardImportErrorMissingmappings, 
                            StringUtils.join(requiredHeaders.entrySet().stream().filter(e -> !e.getValue()).map(e -> e.getKey()).collect(Collectors.toList()))));
                }

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
     * List of {@link ProductImportMapping}s where <b>any</b> member of CSV file
     * is contained, but can be <code>null</code> if not assigned to a bean attribute.
     * This is necessary for later import, where the mapping have to be complete.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ProductImportMapping> getCompleteMappings() {
        // complete for missing assignments
        for (String availableColumn : ((HashMap<String, Integer>)treeMapper.getLeftTreeViewer().getInput()).keySet()) {
            if(!mappings.parallelStream().anyMatch(pm -> pm.getLeftItem().equalsIgnoreCase(availableColumn))) {
                mappings.add(new ProductImportMapping(availableColumn, null));
            }
        }
        return mappings;
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
            String retval = "";
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
