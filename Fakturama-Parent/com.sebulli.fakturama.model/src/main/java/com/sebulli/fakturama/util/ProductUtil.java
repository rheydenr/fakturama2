/**
 * 
 */
package com.sebulli.fakturama.util;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;

import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Product;

/**
 * Some helpful methods for product handling
 *
 */
@Singleton
public final class ProductUtil {
       
    @Inject
    @Preference//(nodePath="/instance/com.sebulli.fakturama.rcp")
    private IEclipsePreferences eclipsePrefs;
    
    /**
     * Create the picture name based on the product's item number Remove illegal
     * characters and add an ".jpg"
     * 
     * @param name
     *            The name of the product
     * @param itemNr
     *            The item number of the product
     * @return Picture name as String
     */
    public static String createPictureName(String name, String itemNr) {

        String pictureName;

        // Get the product's item number
        pictureName = itemNr;

        // If the product name is different to the item number,
        // add also the product name to the pictures name
        if (!name.equals(itemNr)) {
            pictureName += "_" + name;
        }

        // Remove all illegal characters that are not allowed as file name.
        final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', ' ', '.' };
        for (char c : ILLEGAL_CHARACTERS) {
            pictureName = pictureName.replace(c, '_');
        }
        
        // Add the .*jpg extension
        pictureName += ".jpg";

        return pictureName;
    }
    

    /**
     * Get the products price. Because the products price can be a graduated
     * price, it is necessary to compare all blocks.
     * 
     * @param quantity
     *            Quantity to search for
     * @return The price for this quantity
     */
    public Double getPriceByQuantity(Product product, Double quantity) {

        // Start with first block
        Double price = product.getPrice1();
        int blockQuantity = Integer.valueOf(0);
        int newQuantity;
        int scaledPrices;
        scaledPrices = eclipsePrefs.getInt(Constants.PREFERENCES_PRODUCT_SCALED_PRICES, Integer.valueOf(1));

        // search all used blocks
        // no reflection used because of unwanted side effects...
        // maybe later on we use a list of "blocks"
        Integer[] blocks = new Integer[] {product.getBlock1(), product.getBlock2(), product.getBlock3(), product.getBlock4(), product.getBlock5()};
        Double[] prices = new Double[] {product.getPrice1(), product.getPrice2(), product.getPrice3(), product.getPrice4(), product.getPrice5()};
        for (int i = 0; i < scaledPrices; i++) {
            newQuantity = blocks[i];
            if (newQuantity > blockQuantity && quantity >= newQuantity - 0.0001) {
                blockQuantity = newQuantity;
                price = prices[i];
            }
        }
        return price;
    }

}
