package com.sebulli.fakturama.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.model.TextModule_;

@Creatable
public class TextsDAO extends AbstractDAO<TextModule> {

	protected Class<TextModule> getEntityClass() {
		return TextModule.class;
	}

	/**
	 * Gets the all visible properties of this VAT object.
	 * 
	 * @return String[] of visible VAT properties
	 */
	public String[] getVisibleProperties() {
		return new String[] { TextModule_.name.getName(), TextModule_.text.getName() };
	}
	
	
	/**
	 * Counts all entities with the given category.
	 * 
	 * @param cat count of entities which have the given category
	 */
	public long countByCategory(AbstractCategory cat) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
		Root<TextModule> root = criteria.from(getEntityClass());
		criteria.select(cb.count(root)).where(
						cb.equal(root.get(TextModule_.categories), cat)
				);
		return getEntityManager().createQuery(criteria).getSingleResult();
	}

}
