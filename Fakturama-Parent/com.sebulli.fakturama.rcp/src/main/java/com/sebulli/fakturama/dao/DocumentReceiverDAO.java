/**
 * 
 */
package com.sebulli.fakturama.dao;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.DocumentReceiver;

/**
 *
 */
@Creatable
public class DocumentReceiverDAO extends AbstractDAO<DocumentReceiver> {

	@Override
	protected Class<DocumentReceiver> getEntityClass() {
		return DocumentReceiver.class;
	}
}
