package com.sebulli.fakturama.dao;

import java.util.Optional;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.persistence.config.QueryHints;

import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.UserProperty;
import com.sebulli.fakturama.model.UserProperty_;
import com.sebulli.fakturama.oldmodel.OldProperties;

@Creatable
public class PropertiesDAO extends AbstractDAO<UserProperty> {

    protected Class<UserProperty> getEntityClass() {
        return UserProperty.class;
    }

    /**
     * Finds a {@link UserProperty} by a given {@link OldProperties}.
     * 
     * @param oldVat
     * @return
     */
    public OldProperties findByOldProperty(OldProperties oldProperties) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<OldProperties> criteria = cb.createQuery(OldProperties.class);
        Root<OldProperties> root = criteria.from(OldProperties.class);
        CriteriaQuery<OldProperties> cq = criteria.where(cb.and(cb.equal(root.<String> get("description"), oldProperties.getName()),
                cb.equal(root.<String> get("name"), oldProperties.getName())));
        return getEntityManager().createQuery(cq).getSingleResult();
    }
    
    /**
     * Finds the value of an user specific property.
     * 
     * @param name property
     * @return value of that property
     */
    public Optional<String> findPropertyValue(String name) {
        return findPropertyValue(name, false);
    }
    
    /**
     * Finds the value of an user specific property.
     * 
     * @param name property
     * @param force forces read of database
     * @return value of that property
     */
    public Optional<String> findPropertyValue(String name, boolean force) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserProperty> criteria = cb.createQuery(getEntityClass());
        Root<UserProperty> root = criteria.from(UserProperty.class);
        criteria.where(cb.equal(root.get(UserProperty_.name), name)).orderBy(cb.desc(root.get(UserProperty_.dateAdded)));
        TypedQuery<UserProperty> query = null;
        UserProperty result = null;
		try {
            query = getEntityManager().createQuery(criteria);
			query.setHint(QueryHints.CACHE_STORE_MODE, "REFRESH");
            result = query.getSingleResult();
		} catch (NoResultException e) {
			// ignore, retval is an empty Optional
		} catch (NonUniqueResultException nuex) {
			log.warn("non-unique result found for property " + name);
			result = query.getResultList().get(0);
		}
        return result != null ? Optional.ofNullable(result.getValue()) : Optional.empty();
    }

    /**
     * Updates a user property. If the given property isn't available then it
     * will be created.
     * 
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        try {
            UserProperty prop = findByName(key);
            if (prop != null) {
                prop.setValue(value);
                update(prop);
            }
            else {
            	prop = modelFactory.createUserProperty();
                prop.setName(key);
                prop.setValue(value);
                prop.setUser(System.getProperty("user.name", "(unknown)"));
                save(prop);
            }
        }
        catch (FakturamaStoringException e) {
            getLog().error(e);
        }
    }
}
