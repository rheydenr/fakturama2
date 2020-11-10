package org.fakturama.imp.wizard.csv.products;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;
import org.fakturama.imp.ImportMessages;
import org.fakturama.imp.wizard.ImportOptions;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.bean.CsvToBean;
import com.sebulli.fakturama.dao.ProductCategoriesDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.util.ProductUtil;

public class GenericProductsCsvImporter {

    @Inject
    @Translation
    protected ImportMessages importMessages;

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private ProductsDAO productsDAO;

    @Inject
    private ProductCategoriesDAO productCategoriesDAO;

    @Inject
    private VatsDAO vatsDAO;

    @Inject
    protected ILogger log;

    @Inject
    protected IPreferenceStore defaultValuePrefs;

    /**
     * the model factory
     */
    private FakturamaModelFactory modelFactory;

    // The result string
    private String result = " ";

    // NewLine
    private String NL = System.lineSeparator();

    /**
     * The import procedure
     * 
     * @param fileName
     *            Name of the file to import
     * @param test
     *            if true, the dataset are not imported (currently not used)
     * @param importOptions
     *            Options for Import
     * 
     */
    public void importCSV(ImportOptions importOptions, boolean test) {

        // if true, also existing entries will be updated
        boolean updateExisting = importOptions.getUpdateExisting();

        // if true, also empty values will be updated
        boolean importEmptyValues = importOptions.getUpdateWithEmptyValues();
        String fileName = importOptions.getCsvFile();
        Path basePath = Paths.get(StringUtils.defaultString(importOptions.getBasePath()));
        char separator = StringUtils.defaultIfBlank(importOptions.getSeparator(), ";").charAt(0);
        char quoteChar = StringUtils.isNotBlank(importOptions.getQuoteChar()) ? importOptions.getQuoteChar().charAt(0) : '"';
        modelFactory = FakturamaModelPackage.MODELFACTORY;
        Date today = Calendar.getInstance().getTime();

        if (fileName == null) {
            log.error("No filename for import file given.");
            return;
        }

        // Result string
        // T: Importing + .. FILENAME
        result = String.format("%s %s", importMessages.wizardImportProgressinfo, fileName);

        // Count the imported products
        int importedProducts = 0;
        int updatedProducts = 0;

        // Count the line of the import file
//        int lineNr = 0;

        Path inputFile = Paths.get(fileName);

        // Open the existing file
        try (BufferedReader in = Files.newBufferedReader(inputFile)) {
            ICSVParser csvParser = new CSVParserBuilder().withSeparator(separator).withQuoteChar(quoteChar).build();
            CSVReader csvr = new CSVReaderBuilder(in).withCSVParser(csvParser).build();

            CustomHeaderColumnNameTranslateMappingStrategy<ProductBeanCSV> strat = new CustomHeaderColumnNameTranslateMappingStrategy<>();
            strat.setType(ProductBeanCSV.class);
            Map<String, String> columnMapping = new HashMap<String, String>(); 
            
            for (ProductImportMapping pm : importOptions.getMappings()) {
                // null values aren't allowed in streams, therefore we use the classical way
                columnMapping.put(pm.getLeftItem(), pm.getRightItem() != null ? pm.getRightItem().getKey() : null);
            }
            strat.setColumnMapping(columnMapping); // the fields to bind to in your bean

            CsvToBean<ProductBeanCSV> csv = new CsvToBean<>();
            csv.setCsvReader(csvr);
            csv.setMappingStrategy(strat);

            Iterator<ProductBeanCSV> productCsvIterator = csv.iterator();            
            
            // Read the existing file and store it in a buffer
            // with a fixed size. Only the newest lines are kept.

            // Read line by line
            while (productCsvIterator.hasNext()) {
//                lineNr++;

                //  Product product = modelFactory.createProduct();
                ProductBeanCSV productBean = productCsvIterator.next();
                Product product = modelFactory.createProduct();
                product.setItemNumber(productBean.getItemNumber());
                product.setName(productBean.getName());
                
                if (updateExisting) {
                    product = productsDAO.findOrCreate(product);
                }
                
                // update/copy values from CSV bean
                product = copyValues(product, productBean);
                
                if (productBean.getCategory() != null) {
                    ProductCategory category = productCategoriesDAO.getCategory(productBean.getCategory(), false);
                    if (category != null || importEmptyValues) {
                        product.setCategories(category);
                    }
                }
                //                setProductOptions(product, prop.getProperty("options"));

                if(productBean.getPictureName() != null && !basePath.toString().isEmpty() 
                        && (!productBean.getPictureName().isEmpty() || importEmptyValues)) {
                    byte[] picture = ProductUtil.readPicture(productBean.getPictureName(), basePath);
                    product.setPicture(picture);
                }

                // Only if both VAT name and value are given they are used.
                String vatName = productBean.getVatName();
                Double vatValue = productBean.getVat();
                VAT prodVat;
                if (vatName != null && vatValue != null) {
                    prodVat = modelFactory.createVAT();
                    prodVat.setName(vatName);
                    prodVat.setTaxValue(vatValue);
                    prodVat.setDescription(msg.getPurchaseTaxString());
                    prodVat = vatsDAO.findOrCreate(prodVat);

                } else {
                    // fallback: use default VAT
                    long vatId = defaultValuePrefs.getLong(Constants.DEFAULT_VAT);
                    prodVat = vatsDAO.findById(vatId);
                }
                product.setVat(prodVat);
                
                // Add the product to the data base
                if (DateUtils.isSameDay(product.getDateAdded(), today)) {
                    importedProducts++;
                } else if (updateExisting || DateUtils.isSameDay(product.getModified(), today)) {
                    // Update data
                    updatedProducts++;
                }
                // Update the modified product data
                productsDAO.update(product);
            }            

            // The result string
            // T: Message: xx Products HAVE BEEN IMPORTED
            result += NL + Integer.toString(importedProducts) + " " + importMessages.wizardImportInfoProductsimported;
            if (updatedProducts > 0)
                result += NL + Integer.toString(updatedProducts) + " " + importMessages.wizardImportInfoProductsupdated;
            
        } catch (IOException e) {
            // T: Error message
            result += NL + importMessages.wizardImportErrorOpenfile;
        } catch (FakturamaStoringException e) {
            log.error(e, "cant't store import data.");
        }

    }

    /**
     * Update values from CSV bean into Product bean.
     * 
     * @param product target
     * @param productBean source
     */
    private Product copyValues(Product product, ProductBeanCSV productBean) {
        Date today = Calendar.getInstance().getTime();
        product.setBlock1(productBean.getBlock1());
        product.setBlock2(productBean.getBlock2());
        product.setBlock3(productBean.getBlock3());
        product.setBlock4(productBean.getBlock4());
        product.setBlock5(productBean.getBlock5());

        if (productBean.getDateAdded() == null) {
            product.setDateAdded(today);
        } else {
            product.setDateAdded(productBean.getDateAdded());
            product.setModified(today);
        }
        
        // itemNumber an name were already set in caller method to check if product exists
        
        product.setSupplierItemNumber(productBean.getSupplierItemNumber());
        product.setPrice1(productBean.getPrice1());
        product.setPrice2(productBean.getPrice2());
        product.setPrice3(productBean.getPrice3());
        product.setPrice4(productBean.getPrice4());
        product.setPrice5(productBean.getPrice5());
        
        product.setQuantity(productBean.getQuantity());
        product.setQuantityUnit(productBean.getQuantityUnit());
        product.setSellingUnit(productBean.getSellingUnit());
        
        // VAT and category are set in caller method
        
        product.setWebshopId(productBean.getWebshopId());
        product.setWeight(productBean.getWeight());
        product.setGtin(productBean.getGtin());
        product.setCostPrice(productBean.getCostPrice());
        product.setAllowance(productBean.getAllowance());
        product.setCdf01(productBean.getCdf01());
        product.setCdf02(productBean.getCdf02());
        product.setCdf03(productBean.getCdf03());
        product.setNote(productBean.getNote());
        product.setDescription(productBean.getDescription());
        
        return product;
    }

    public String getResult() {
        return result;
    }

}
