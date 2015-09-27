/**
 * 
 */
package com.sebulli.fakturama.parts.widget;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.nebula.widgets.picture.PictureControl;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * a {@link org.eclipse.nebula.widgets.picture.PictureControl} with some modifications.
 *
 */
public class FakturamaPictureControl extends PictureControl {
	protected Messages msg;
    protected IPreferenceStore defaultValuePrefs;

	public FakturamaPictureControl(Composite parent, IPreferenceStore defaultValuePrefs, Messages msg) {
		super(parent);
		this.msg = msg;
		this.defaultValuePrefs = defaultValuePrefs;
		setModifyImageLinkText(msg.editorProductButtonChoosepicName);
		setDeleteImageLinkText(msg.mainMenuEditDeleteName);
	}
	
	@Override
	public void setImageByteArray(byte[] imageByteArray) {
		super.setImageByteArray(imageByteArray);
		Menu menu = getPictureLabel().getMenu();
		menu.getItem(0).setText(msg.mainMenuEditDeleteName);
		menu.getItem(0).setImage(Icon.COMMAND_DELETE.getImage(IconSize.DefaultIconSize));
		menu.getItem(1).setText(msg.editorProductButtonChoosepicName);
		menu.getItem(1).setImage(Icon.COMMAND_ORDER_PROCESSING.getImage(IconSize.DefaultIconSize));
	}
	@Override
	protected void configure(FileDialog fd) {
		super.configure(fd);
		fd.setText(msg.editorProductButtonChoosepicName);
		fd.setFilterPath(defaultValuePrefs.getString(Constants.GENERAL_WORKSPACE));
	}
}
