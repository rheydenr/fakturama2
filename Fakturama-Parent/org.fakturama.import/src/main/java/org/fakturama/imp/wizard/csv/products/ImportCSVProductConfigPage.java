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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.treemapper.ISemanticTreeMapperSupport;
import org.eclipse.nebula.widgets.treemapper.TreeMapper;
import org.eclipse.nebula.widgets.treemapper.TreeMapperUIConfigProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
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
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.parts.widget.contentprovider.SimpleTreeContentProvider;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 *
 */
public class ImportCSVProductConfigPage extends WizardPage {

    @Inject
    @Translation
    protected ImportMessages importMessages;

    private ImportOptions options;
    private IEclipseContext ctx;
    private List<ProductImportMapping> mappings;
    private TreeMapper<ProductImportMapping, String, EStructuralFeature> treeMapper;

    public ImportCSVProductConfigPage(String title, String label, ProgramImages image) {
        super("ImportOptionConfigPage");
        setTitle(title);
        setMessage(label);
    }

    /**
     * Default constructor. Used only for injection. <br />
     * WARNING: Use <b>only</b> with injection since some initial values are set
     * in initialize method.
     */
    public ImportCSVProductConfigPage() {
        super("ImportOptionConfigPage");
    }

    @PostConstruct
    public void initialize(IEclipseContext ctx) {
        setTitle((String) ctx.get(ImportOptionPage.WIZARD_TITLE));
        this.ctx = ctx;
    }

    @Override
    public void createControl(Composite parent) {

        // Create the top composite
        Composite top = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(3).applyTo(top);
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(top);
        setControl(top);

        // Create the label with the help text
        Label labelDescription = new Label(top, SWT.NONE);

        labelDescription.setText("Please map the fields from CSV file to product attributes.");
        GridDataFactory.swtDefaults().span(3, 1).align(SWT.BEGINNING, SWT.CENTER).indent(0, 10).applyTo(labelDescription);

        createTreeMapperWidget(top);

    }

    private void createTreeMapperWidget(Composite parent) {
        Display display = parent.getDisplay();

        Color gray = display.getSystemColor(SWT.COLOR_GRAY);
        Color blue = display.getSystemColor(SWT.COLOR_BLUE);
        TreeMapperUIConfigProvider uiConfig = new TreeMapperUIConfigProvider(gray, 1, blue, 3);
        mappings = new ArrayList<>();
        ISemanticTreeMapperSupport<ProductImportMapping, String, EStructuralFeature> semanticSupport = new ISemanticTreeMapperSupport<ProductImportMapping, String, EStructuralFeature>() {
            @Override
            public ProductImportMapping createSemanticMappingObject(String leftItem, EStructuralFeature rightItem) {
                // create only one mapping (for the left item); delete old mapping if it was created before!
                for (ProductImportMapping orderStatesMapping : mappings) {
                    if (orderStatesMapping.getLeftItem().equals(leftItem)) {
                        mappings.remove(orderStatesMapping);
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
            public EStructuralFeature resolveRightItem(ProductImportMapping semanticMappingObject) {
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
                boolean canFinish = !mappings.isEmpty();
                options.setMappingAvailable(canFinish);
                setPageComplete(canFinish);
            }
        });
        GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 200).grab(true, true).span(2, 1).applyTo(treeMapper.getControl());

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
                    List<String> sortedHeaderList = Arrays.stream(headerLine).map(e -> StringUtils.trim(e)).filter(e -> StringUtils.isNotBlank(e)).sorted().collect(Collectors.toList());
                    treeMapper.setInput(sortedHeaderList, "irrelevant", mappings);
                    options.setAnalyzeCompleted(true);
                }
            } catch (IOException e) {
                // T: Error message
                //                result += NL + importMessages.wizardImportErrorOpenfile;
            }
        }
    }

    /**
     * Provides all available fields from CSV file.
     *
     */
    public class ProductImportContentProvider extends SimpleTreeContentProvider {
        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(Object inputElement) {
            return ((List<String>) inputElement).toArray(new String[] {});
        }
    }

    /**
     * Provides all selectable fields for product import.
     *
     */
    public class ProductsFieldContentProvider extends SimpleTreeContentProvider {
        private List<EStructuralFeature> productAttributes;

        @Override
        public Object[] getElements(Object inputElement) {
            if (productAttributes == null) {
                productAttributes = ((EClass) FakturamaModelPackage.INSTANCE.getEPackage().getEClassifiers().get(FakturamaModelPackage.PRODUCT_CLASSIFIER_ID)) //
                        .getEStructuralFeatures().stream() //
                        .filter(f -> !f.isMany()) //
                        .sorted(Comparator.comparing(EStructuralFeature::getName)).collect(Collectors.toList());
            }
            return productAttributes.toArray();
        }
    }

    class ViewLabelProvider extends LabelProvider {
        private ResourceManager resourceManager;

        @Override
        public String getText(Object element) {
            String retval = "";
            if (element instanceof EStructuralFeature) {
                retval = ((EStructuralFeature)element).getName();
            } else {
                retval = super.getText(element);
            }
            return retval;
        }

        @Override
        public void dispose() {
            // garbage collect system resources
            if (resourceManager != null) {
                resourceManager.dispose();
                resourceManager = null;
            }
        }

        protected ResourceManager getResourceManager() {
            if (resourceManager == null) {
                resourceManager = new LocalResourceManager(JFaceResources.getResources());
            }
            return resourceManager;
        }
    }

}
