package com.sebulli.fakturama.dao;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.VoucherItem;

@Creatable
public class VoucherItemsDAO extends AbstractDAO<VoucherItem> {

    protected Class<VoucherItem> getEntityClass() {
    	return VoucherItem.class;
    }
        
//    /**
//    * Gets the all visible properties of this Voucher object.
//    * 
//    * @return String[] of visible Voucher properties
//    */
//    public String[] getVisibleProperties() {
//       return new String[] { Voucher_.doNotBook.getName(), 
//               Voucher_.voucherDate.getName(), 
//               Voucher_.voucherNumber.getName(), 
//               Voucher_.documentNumber.getName(),
//               Voucher_.name.getName(), 
//               Voucher_.paidValue.getName() 
//               };
//    }
}
