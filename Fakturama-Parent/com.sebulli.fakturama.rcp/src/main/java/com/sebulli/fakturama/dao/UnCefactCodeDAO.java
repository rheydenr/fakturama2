/**
 * 
 */
package com.sebulli.fakturama.dao;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.CEFACTCode;

/**
 *
 */
@Creatable
public class UnCefactCodeDAO extends AbstractDAO<CEFACTCode> {

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.dao.AbstractDAO#getEntityClass()
	 */
	@Override
	protected Class<CEFACTCode> getEntityClass() {
		return CEFACTCode.class;
	}

}
