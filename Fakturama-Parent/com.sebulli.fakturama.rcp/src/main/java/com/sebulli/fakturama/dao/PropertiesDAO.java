package com.sebulli.fakturama.dao;

import java.util.Optional;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;

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
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserProperty> criteria = cb.createQuery(getEntityClass());
        Root<UserProperty> root = criteria.from(UserProperty.class);
        criteria.where(cb.equal(root.get(UserProperty_.name), name));
        Optional<String> retval = Optional.empty();
		try {
			UserProperty result = getEntityManager().createQuery(criteria).getSingleResult();
			retval = Optional.ofNullable(result.getValue());
		} catch (NoResultException e) {
			// ignore, retval is an empty Optional
		}
        return retval;
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
                prop = new UserProperty();
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
