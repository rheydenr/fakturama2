package com.sebulli.fakturama.parts.voucheritems;

import java.util.Comparator;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.VoucherItemDTO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherItem;
import com.sebulli.fakturama.model.VoucherType;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;


/**
 * Builder for the {@link VoucherItem}s list.
 * 
 */
public class VoucherItemListBuilder {

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    private VatsDAO vatsDao;
    
    @Inject
    private IEclipseContext context;
    
    @Inject
    @Preference 
    protected IEclipsePreferences preferences;

    private Composite parent;
    private Voucher voucher;

    private boolean useGross;
    private int netgross = DocumentSummary.ROUND_NOTSPECIFIED;

    private FakturamaModelFactory modelFactory;

    private VoucherItemListTable itemListTable;

//    protected NatTable natTable;

    public VoucherItemListTable build() {
        modelFactory = FakturamaModelPackage.MODELFACTORY;
        // Container for the label and the add and delete button.
        Composite addButtonComposite = new Composite(parent, SWT.NONE | SWT.RIGHT);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(addButtonComposite);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addButtonComposite);

        // Items label
        Label labelItems = new Label(addButtonComposite, SWT.NONE | SWT.RIGHT);
        //T: VoucherEditor - Label Items
        labelItems.setText(msg.editorVoucherFieldItemsName);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(labelItems);

        // Item add button
        Label addButton = new Label(addButtonComposite, SWT.NONE);
            addButton.setImage(Icon.COMMAND_PLUS.getImage(IconSize.DefaultIconSize));
            addButton.setToolTipText(msg.editorVoucherFieldItemsNewposTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addButton);

        // Item delete button
        Label deleteButton = new Label(addButtonComposite, SWT.NONE);
        deleteButton.setImage(Icon.COMMAND_DELETE.getImage(IconSize.DefaultIconSize));
        deleteButton.setToolTipText(msg.editorVoucherFieldItemsDeleteposTooltip);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(deleteButton);
        
        itemListTable = ContextInjectionFactory.make(VoucherItemListTable.class, context);
        Control tableComposite = itemListTable.createPartControl(parent, voucher, useGross, netgross);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
        addButton.addMouseListener(new MouseAdapter() {

            // Add a new item with default properties
            public void mouseDown(MouseEvent e) {
                addNewItem();
            }
        });
        deleteButton.addMouseListener(new MouseAdapter() {

            // Delete the selected item
            public void mouseDown(MouseEvent e) {
                itemListTable.removeSelectedEntry();
            }
        });
        return itemListTable;

    }
    
    /**
     * Add a voucher item to the list of all voucher items.
     */
    private void addNewItem() {
        int defaultVatId = preferences.getInt(Constants.DEFAULT_VAT, 1);
        // Use the standard VAT value
        VAT defaultVat = vatsDao.findById(defaultVatId);
        VoucherItem item = createNewVoucherItem(msg.commonFieldName, "", Double.valueOf(0.0), defaultVat);
        Optional<VoucherItemDTO> maxPosItem = itemListTable.getVoucherItemsListData().stream().max(
                new Comparator<VoucherItemDTO>() {
            @Override
            public int compare(VoucherItemDTO o1, VoucherItemDTO o2) {
                return o1.getVoucherItem().getPosNr().compareTo(o2.getVoucherItem().getPosNr());
            }
        });
        
        Integer newPosNr = maxPosItem.isPresent() ? maxPosItem.get().getVoucherItem().getPosNr() + Integer.valueOf(1) : Integer.valueOf(1);
        item.setPosNr(newPosNr);
        VoucherItemDTO newItem = new VoucherItemDTO(item);
        itemListTable.addNewItem(newItem);
    }
    
    /**
     * Creates a new voucher item.
     *
     * @param name the name
     * @param category the category
     * @param price the price
     * @param vat the vat
     * @return the voucher item
     */
    private VoucherItem createNewVoucherItem(String name, String category, Double price, VAT vat) {
    	VoucherItem expenditureItem = modelFactory.createVoucherItem();
    	expenditureItem.setItemVoucherType(VoucherType.EXPENDITURE);
        expenditureItem.setName(name);
        expenditureItem.setPrice(price);
        expenditureItem.setVat(vat);
        return expenditureItem;
    }
    
  /**
   * Creates a new voucher item by a parent item
   * 
   * @param item
   *  The parent item
   * @return
   *  The created item
   */
    //   @Override
       protected VoucherItem createNewVoucherItem(VoucherItem item) {
           return null;
       }


    /**
     * Sets the parent container.
     *
     * @param parent the parent
     * @return the voucher item list builder
     */
    public VoucherItemListBuilder withParent(Composite parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Use gross or net
     *
     * @param useGross use gross
     * @return the voucher item list builder
     */
    public VoucherItemListBuilder withUseGross(boolean useGross) {
        this.useGross = useGross;
        return this;
    }

    public VoucherItemListBuilder withNetGross(int netgross) {
        this.netgross = netgross;
        return this;
    }

    public VoucherItemListBuilder withVoucher(Voucher voucher) {
        this.voucher = voucher;
        return this;
    }

}
