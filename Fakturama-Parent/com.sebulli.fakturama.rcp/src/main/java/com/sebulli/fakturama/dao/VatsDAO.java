package com.sebulli.fakturama.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VAT_;
import com.sebulli.fakturama.oldmodel.OldVats;

@Creatable
public class VatsDAO extends AbstractDAO<VAT> {
    
    @Inject
    @Translation
    protected Messages msg;

	@Inject
	@GeminiPersistenceContext(unitName = "unconfigured2", properties = {
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_DRIVER, valuePref = @Preference(value = PersistenceUnitProperties.JDBC_DRIVER)),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_URL, valuePref = @Preference(PersistenceUnitProperties.JDBC_URL)),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_USER, valuePref = @Preference(PersistenceUnitProperties.JDBC_USER)),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_PASSWORD, valuePref = @Preference(PersistenceUnitProperties.JDBC_PASSWORD)),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING, value = "false"),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING_INTERNAL, value = "false") })
	private EntityManager em;

	protected Class<VAT> getEntityClass() {
		return VAT.class;
	}
	
	public void init() {
	    // do nothing
	}

	@PreDestroy
	public void destroy() {
		if (getEntityManager() != null && getEntityManager().isOpen()) {
			getEntityManager().close();
		}
	}

	/**
	 * @return the em
	 */
	protected EntityManager getEntityManager() {
		return em;
	}
	
	/**
	 * Deletes the whole table data. Use with caution!
	 */
	public boolean truncate() {
//	    getEntityManager().getTransaction().begin();
//	    getEntityManager().createQuery("delete from VAT").executeUpdate();
//	    getEntityManager().getTransaction().commit();
	    return true;
	}

	/**
	 * @param em
	 *            the em to set
	 */
	protected void setEntityManager(EntityManager em) {
		this.em = em;
	}
	
	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.dao.AbstractDAO#getAlwaysIncludeAttributes()
	 */
	@Override
	protected Map<Class<VAT>, Vector<String>> getAlwaysIncludeAttributes() {
	    Map<Class<VAT>, Vector<String>> map = new HashMap<>();
	    Vector<String> attribVector = new Vector<String>();
	    attribVector.addElement(VAT_.taxValue.getName());
	    map.put(getEntityClass(), attribVector);
	    return map;
	}

	/**
	 * Finds a {@link VAT} by a given {@link OldVats}.
	 * 
	 * @param oldVat
	 * @return
	 */
	public VAT findByOldVat(OldVats oldVat) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<VAT> criteria = cb.createQuery(VAT.class);
		Root<VAT> root = criteria.from(VAT.class);
		CriteriaQuery<VAT> cq = criteria.where(cb.and(cb.equal(root.<String> get(VAT_.description), oldVat.getDescription()),
				cb.equal(root.<String> get(VAT_.name), oldVat.getName())));
		return getEntityManager().createQuery(cq).getSingleResult();
	}
	     
	/**
	 * Gets the all visible properties of this VAT object.
	 * 
	 * @return String[] of visible VAT properties
	 */
	public String[] getVisibleProperties() {
		String[] retval = new String[] { VAT_.name.getName(), VAT_.description.getName(), VAT_.taxValue.getName()};
		return retval;
	}
}
