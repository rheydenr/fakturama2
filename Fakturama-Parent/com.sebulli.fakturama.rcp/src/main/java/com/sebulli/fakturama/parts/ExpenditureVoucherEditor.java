/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.parts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.calculate.VoucherSummaryCalculator;
import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.dto.VoucherItemDTO;
import com.sebulli.fakturama.dto.VoucherSummary;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherItem;
import com.sebulli.fakturama.model.VoucherType;

public class ExpenditureVoucherEditor extends VoucherEditor {

    // Editor's ID
	public static final String ID = "com.sebulli.fakturama.editors.expenditureVoucherEditor";
	
	public static final String EDITOR_ID = "ExpenditureVoucherEditor";
    
    @Inject
    protected ExpendituresDAO expendituresDAO;
    
//    @Inject
//    private EHelpService helpService;

    /**
     * Saves the contents of this part
     * 
     * @param monitor
     *            Progress monitor
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Persist
    public void doSave(IProgressMonitor monitor) {
        /*
         * the following parameters are not saved: 
         * - id (constant)
         */

        // Always set the editor's data set to "undeleted"
        voucher.setDeleted(false);
        
        // Create a new voucher ID, if this is a new voucher
        if (newVoucher) {
            try {
                voucher = getModelRepository().save(voucher);
            } catch (FakturamaStoringException e) {
                log.error(e);
            }
        }

        // Set all the items
        List<VoucherItem> items = itemListTable.getVoucherItemsListData()
            .stream()
            .map(dto -> dto.getVoucherItem())
            .sorted(Comparator.comparing(VoucherItem::getId))
            .collect(Collectors.toList());
        voucher.setItems(new ArrayList<>(items));
        
        // delete removed items
        for (IEntity expenditureItem : itemListTable.getMarkedForDeletion()) {
            try {
                voucherItemsDAO.save((VoucherItem)expenditureItem);
            } catch (FakturamaStoringException e) {
                log.error(e);
            }
        }

//      for (DataSetVoucherItem itemDataset : itemDatasets) {
//
//          // Get the ID of this voucher item and
//          int id = itemDataset.getIntValueByKey("id");
//
//          DataSetVoucherItem item = null;
//
//          // Get an existing item, or use the temporary item
//          if (id >= 0) {
//              item = (DataSetVoucherItem) getVoucherItems().getDatasetById(id);
//
//              // Copy the values to the existing voucher item.
//              item.setStringValueByKey("name", itemDataset.getStringValueByKey("name"));
//              item.setStringValueByKey("category", itemDataset.getStringValueByKey("category"));
//              item.setDoubleValueByKey("price", itemDataset.getDoubleValueByKey("price"));
//              item.setIntValueByKey("vatid", itemDataset.getIntValueByKey("vatid"));
//          }
//          else
//              item = itemDataset;
//
//          // Updates the list of billing account
//          updateBillingAccount(item);
//          
//      }
//      // Set the string value
//      ...
//      // Set total and paid value
//     ...
//
//      // The the voucher was paid with a discount, use the paid value
        if (bPaidWithDiscount.getSelection()) {
            voucher.setPaidValue((Double) textPaidValue.getValue());
        }
        // else use the total value
        else {
            voucher.setPaidValue((Double) textTotalValue.getValue());
        }

      // The selection "book" is inverted
      voucher.setDoNotBook(!bBook.getSelection());
//
      // If it is a new voucher, add it to the voucher list and
      // to the data base
      if (newVoucher) {
          addVoucher(voucher);
          newVoucher = false;
      }
      // If it's not new, update at least the data base
      else {
          updateVoucher(voucher);
      }

      // Set the Editor's name to the voucher name.
      this.part.setLabel(voucher.getName());

      // Refresh the table view of all vouchers
      evtBroker.post(EDITOR_ID, "update");
      
      // reset dirty flag
      getMDirtyablePart().setDirty(false);
    }
    
    /**
	 * @return
	 */
	protected String getCustomerSupplierString() {
		return msg.expenditurevoucherFieldSupplier;
	}

//
//	/**
//	 * Get all vouchers
//	 * 
//	 * @return
//	 * 	All vouchers
//	 */
//	public DataSetArray<?> getVouchers() {
//		return Data.INSTANCE.getExpenditureVouchers();
//	}
//	
//	/**
//	 * Add a voucher item to the list of all voucher items
//	 * 
//	 * @param item
//	 * 	The new item to add
//	 * @return
//	 *  A Reference to the added item
//	 */
//	public DataSetVoucherItem addVoucherItem(DataSetVoucherItem item) {
//		return Data.INSTANCE.getExpenditureVoucherItems().addNewDataSet((DataSetExpenditureVoucherItem) item);
//	}
	
	/**
	 * @return
	 */
	protected String getEditorTitle() {
		return msg.expenditurevoucherEditorTitle;
	}

	/**
	 * @return
	 */
	protected VoucherType getVoucherType() {
		return VoucherType.EXPENDITURE;
	}

//	/**
//	 * Gets the temporary voucher items
//	 * 
//	 * @return
//	 * 	The temporary items
//	 */
//	public DataSetArray<?> getMyVoucherItems() {
//		return voucherItems;
//	}
//
//	/**
//	 * Creates the SWT controls for this workbench part
//	 * 
//	 * @param the
//	 *            parent control
//	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
//	 */
//	@SuppressWarnings("unchecked")
//	public void createPartControl(Composite parent) {
//		super.createPartControl(parent, ContextHelpConstants.VOUCHER_EDITOR);
//		// Fill the table with the items
//		tableViewerItems.setInput((DataSetArray<DataSetExpenditureVoucherItem>) getMyVoucherItems());
//	}
    

    /**
     * Calculate the total sum of all voucher items
     */
    private void calculateTotal() {
        
        VoucherSummaryCalculator voucherSummaryCalculator = ContextInjectionFactory.make(VoucherSummaryCalculator.class, context);
        // unwrap VoucherItemDTOs at first
        List<VoucherItem> docItems = new ArrayList<>();
        if(itemListTable != null) {
            // don't use Lambdas because the List isn't initialized yet.
            for (VoucherItemDTO item : itemListTable.getVoucherItemsListData()) {
                docItems.add(item.getVoucherItem());
            }
        } else {
            docItems.addAll(voucher.getItems());
        }
        // Do the calculation
        VoucherSummary voucherSummary = voucherSummaryCalculator.calculate(null, docItems, false, 
                paidValue, totalValue, false);

        // Get the total result
        totalValue = voucherSummary.getTotalGross();

        // Update the text widget
        textTotalValue.setValue(totalValue);
        voucher.setTotalValue((Double) textTotalValue.getValue());
    }

    /**
     * This method is for setting the dirty state to <code>true</code>. This
     * happens if e.g. the items list has changed. (could be sent from
     * DocumentListTable)
     */
    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void handleItemChanged(@UIEventTopic(EDITOR_ID + "/itemChanged") Event event) {
        if (event != null) {
            // the event has already all given params in it since we created them as Map
            String targetDocumentName = (String) event.getProperty(DocumentEditor.DOCUMENT_ID);
            // at first we have to check if the message is for us
            String voucherTempId =  part.getProperties().get(PART_ID); 
            if (!StringUtils.equals(targetDocumentName, voucherTempId)) {
                // if not, silently ignore this event
                return;
            }
            // (re)calculate summary
            // TODO check if this has to be done in a synchronous or asynchronous call
            // within UISynchronize
            if ((Boolean) event.getProperty(DocumentEditor.DOCUMENT_RECALCULATE)) {
                calculateTotal();
            }
            getMDirtyablePart().setDirty(true);
        }
    }    

    /**
     * If an entity is deleted via list view we have to close a possibly open
     * editor window. Since this is triggered by a UIEvent we named this method
     * "handle*".
     */
    @Inject
    @Optional
    public void handleForceClose(@UIEventTopic(ExpenditureVoucherEditor.EDITOR_ID + "/forceClose") Event event) {
        //      sync.syncExec(() -> top.setRedraw(false));
        // the event has already all given params in it since we created them as Map
        String targetDocumentName = (String) event.getProperty(DocumentEditor.DOCUMENT_ID);
        // at first we have to check if the message is for us
        String voucherTempId =  part.getProperties().get(PART_ID); 
        if (!StringUtils.equals(targetDocumentName, voucherTempId)) {
            // if not, silently ignore this event
            return;
        }
        partService.hidePart(part, true);
        //  sync.syncExec(() -> top.setRedraw(true));
    }

    @Override
    protected ExpendituresDAO getModelRepository() {
        return expendituresDAO;
    }
	
    
    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }

    @Override
    protected Class<Voucher> getModelClass() {
        return Voucher.class;
    }

}
