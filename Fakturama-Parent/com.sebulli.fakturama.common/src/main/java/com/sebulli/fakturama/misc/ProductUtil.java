/**
 * 
 */
package com.sebulli.fakturama.misc;

/**
 * Some helpful methods for product handling
 *
 */
public final class ProductUtil {

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
        if (!name.equals(itemNr))
            pictureName += "_" + name;

        // Remove all illegal characters that are not allowed as file name.
        final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', ' ', '.' };
        for (char c : ILLEGAL_CHARACTERS)
            pictureName = pictureName.replace(c, '_');

        // Add the .*jpg extension
        pictureName += ".jpg";

        return pictureName;
    }

}
