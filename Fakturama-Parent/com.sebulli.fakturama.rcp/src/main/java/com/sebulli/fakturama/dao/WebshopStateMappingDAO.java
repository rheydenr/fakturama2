package com.sebulli.fakturama.dao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
//import org.eclipse.emf.texo.server.store.EntityManagerProvider;
import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.queries.QueryByExamplePolicy;

import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.WebshopStateMapping;
import com.sebulli.fakturama.model.WebshopStateMapping_;

@Creatable
public class WebshopStateMappingDAO extends AbstractDAO<WebshopStateMapping> {

	List<String> excludedAttributes = Arrays.asList(WebshopStateMapping_.orderState.getName());

	@Inject
	@Translation
	protected Messages msg;

	@Inject
	@GeminiPersistenceContext(unitName = "unconfigured2", properties = {
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_DRIVER, valuePref = @Preference(value = PersistenceUnitProperties.JDBC_DRIVER) ),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_URL, valuePref = @Preference(PersistenceUnitProperties.JDBC_URL) ),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_USER, valuePref = @Preference(PersistenceUnitProperties.JDBC_USER) ),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_PASSWORD, valuePref = @Preference(PersistenceUnitProperties.JDBC_PASSWORD) ),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING, value = "false"),
			@GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING_INTERNAL, value = "false") })
	private EntityManager em;

	protected Class<WebshopStateMapping> getEntityClass() {
		return WebshopStateMapping.class;
	}

	@PostConstruct
	public void init() {
		FakturamaModelPackage.initialize(); // objectStore.get(WebshopStateMapping.class,
											// 1L);
	}

	public List<WebshopStateMapping> findAllForWebshop(long webshopId) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<WebshopStateMapping> criteria = cb.createQuery(WebshopStateMapping.class);
		Root<WebshopStateMapping> root = criteria.from(WebshopStateMapping.class);
		CriteriaQuery<WebshopStateMapping> cq = criteria
				.where(cb.equal(root.<WebshopStateMapping> get(WebshopStateMapping_.webshopId.getName()), webshopId));
		return getEntityManager().createQuery(cq).getResultList();
	}
	
	@Override
	protected Set<Predicate> getRestrictions(WebshopStateMapping object, CriteriaBuilder criteriaBuilder,
			Root<WebshopStateMapping> root) {
        Set<Predicate> restrictions = new HashSet<>();
        restrictions.add(criteriaBuilder.equal(root.get(WebshopStateMapping_.webshopId), object.getWebshopId()));
        restrictions.add(criteriaBuilder.equal(root.get(WebshopStateMapping_.webshopStateId), object.getWebshopStateId()));
        return restrictions;
	}
	
	@Override
	protected Map<Class<WebshopStateMapping>, Vector<String>> getAlwaysIncludeAttributes() {
	    Map<Class<WebshopStateMapping>, Vector<String>> map = new HashMap<>();
	    Vector<String> attribVector = new Vector<String>();
	    attribVector.addElement(WebshopStateMapping_.webshopId.getName());
	    attribVector.addElement(WebshopStateMapping_.webshopStateId.getName());
	    map.put(getEntityClass(), attribVector);
	    return map;
	}
	
	@Override
	protected QueryByExamplePolicy getQueryByExamplePolicy() {
		QueryByExamplePolicy policy = new WebshopStateQueryByExamplePolicy();
        policy.addSpecialOperation(String.class, "containsSubstring");
        policy.setAttributesToAlwaysInclude(getAlwaysIncludeAttributes());
        policy.setShouldUseEqualityForNulls(true);
		return policy;
	}
	
	
	/**
	 * Clear old mappings.
	 *
	 * @param webshopId the webshop id
	 * @throws FakturamaStoringException 
	 */
	public void clearOldMappings(long webshopId) throws FakturamaStoringException {
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaDelete<WebshopStateMapping> deleteQuery = criteriaBuilder.createCriteriaDelete(WebshopStateMapping.class);
		Root<WebshopStateMapping> root = deleteQuery.from(WebshopStateMapping.class);
		deleteQuery.where(criteriaBuilder.equal(root.get(WebshopStateMapping_.webshopId), webshopId));
		Query createdQuery = getEntityManager().createQuery(deleteQuery);
		
		try {
			checkConnection();
		EntityTransaction trx = getEntityManager().getTransaction();
		trx.begin();
		createdQuery.executeUpdate();
		trx.commit();
		} catch (SQLException e) {
			throw new FakturamaStoringException("Error cleaning web shop state mappings from database for your web shop (id="+webshopId+").", e);
		}
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
	 * @param em
	 *            the em to set
	 */
	protected void setEntityManager(EntityManager em) {
		this.em = em;
	}
	
	class WebshopStateQueryByExamplePolicy extends QueryByExamplePolicy {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -6551973233104289426L;

		@Override
		public boolean shouldIncludeInQuery(Class aClass, String attributeName, Object attributeValue) {
			boolean preCheckedValue = super.shouldIncludeInQuery(aClass, attributeName, attributeValue);
			// enumerate all attributes which don't have to come into the query
			boolean retval = !excludedAttributes.contains(attributeName);
			return retval && preCheckedValue;
		}
	}

}
