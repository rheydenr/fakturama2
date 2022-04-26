/**
 * 
 */
package com.sebulli.fakturama.views.datatable.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.MissingResourceException;

import javax.inject.Inject;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.sebulli.fakturama.resources.ITemplateResourceManager;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 * {@link ImagePainter} for all sorts of Images in a NatTable.
 */
public class CellImagePainter extends ImagePainter {
    
    public static final int MAX_IMAGE_PREVIEW_WIDTH = 250;
    
    private ITemplateResourceManager resourceManager;

    public CellImagePainter(ITemplateResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        Object dataValue = cell.getDataValue();
        Image retval = null;
        if (dataValue instanceof Icon) {
            retval = getImageFrom((Icon) dataValue);
        } else if (dataValue instanceof Image) {
            retval = getImageFrom((Image) dataValue);
        } else if(dataValue instanceof String) {
            retval = getImageFrom((String) dataValue);
        } else if(dataValue instanceof byte[]) {
			retval = getImagefromByteArray(dataValue);
        }
        return retval;
    }

	/**
	 * @param dataValue
	 * @return
	 */
    private Image getImagefromByteArray(Object dataValue) {
        Image image, retval = null;
        ByteArrayInputStream imgStream = new ByteArrayInputStream((byte[]) dataValue);
        try {
            image = new Image(Display.getCurrent(), imgStream);
        } catch (SWTException e) {
            image = resourceManager.getProgramImage(Display.getCurrent(), ProgramImages.NOT_FOUND_PICTURE);
        }

        // Get the pictures size
        int width = image.getBounds().width;
        int height = image.getBounds().height;

        // Scale the image to 64x48 Pixel
        if (width != 0 && height != 0) {

            // Picture is more width than height.
            if (width >= 64 * height / 48) {
                height = (height * 64) / width;
                width = 64;
            } else { // if (height > ((48*width)/64)) {
                width = (width * 48) / height;
                height = 48;
            }
        }

        retval = new Image(Display.getCurrent(), image.getImageData().scaledTo(width, height));
        return retval;
    }
    
    private Image getImageFrom(Icon dataValue) {
        Image retval = null;
            Icon icon = (Icon) dataValue;
            if (icon != null) {
                retval = icon.getImage(IconSize.DefaultIconSize);
            }
        return retval;
    }
    
    private Image getImageFrom(Image dataValue) {
        Image retval = null;
            retval = (Image)dataValue;
        return retval;
    }
    

        /**
         * Scale the given image to {@link CellImagePainter#MAX_IMAGE_PREVIEW_WIDTH}
         * px width. Copied from old ProductPictureDialog class.
         * 
         * @param pictureName
         * @return Image
         */
    private Image getImageFrom(String pictureName) {
            // The scaled image with width and height (used to resize the dialog)
            Image scaledImage = null;
            // Display the picture, if it is set.
            if (!pictureName.isEmpty()) {

                int width = 300;
                int height = 200;

                // Load the image, based on the picture name
                // but at first check if it exists
                if(Files.notExists(Paths.get(pictureName))) {
                    return null;
                }
                Image image = getImage(pictureName);

                // Get the pictures size
                width = image.getBounds().width;
                height = image.getBounds().height;
                
                // Scale the image to 64x48 Pixel
                if (width != 0 && height != 0) {
                    // Picture is wider than height.
                    if (width >= 64*height/48) {
                        height = height * 64 / width;
                        width = 64;
                    }
                    else { //if (height > ((48*width)/64)) {
                        width = width * 48 / height;
                        height = 48;
                    }
                }
                
                scaledImage = new Image(image.getDevice(), image.getImageData().scaledTo(width, height));
                
//                // Scale it to maximum 250px
//                int maxWidth = MAX_IMAGE_PREVIEW_WIDTH;
    //
//                // Maximum picture width 
//                if (width > maxWidth) {
//                    height = maxWidth * height / width;
//                    width = maxWidth;
    //
//                    // Rescale the picture to the maximum width
//                    scaledImage = new Image(image.getDevice(), image.getImageData().scaledTo(width, height));
//                }
//                else {
//                    scaledImage = image;
//                }
            }
            return scaledImage;
    }

    /**
     * Returns an image. Clients do not need to dispose the image, it will be
     * disposed automatically.
     * 
     * @return an {@link Image}
     */
    private Image getImage(String path) {
        Image image = JFaceResources.getImage(path);
        if (image == null) {
            image = addIconImageDescriptor(path);
        }
        return image;
    }

    /**
     * Add an image descriptor for a specific key to the
     * global {@link ImageRegistry}
     * 
     * @param name
     * @param is
     * @return <code>true</code> if successfully added, else <code>false</code>
     */
    private Image addIconImageDescriptor(String path) {
        try {
            URL fileLocation = new File(path).toURI().toURL();
            ImageDescriptor id = ImageDescriptor.createFromURL(fileLocation);
            JFaceResources.getImageRegistry().put(path, id);
        }
        catch (MissingResourceException | MalformedURLException | IllegalArgumentException e) {
            return null;
        }
        return JFaceResources.getImage(path);
    }

}
