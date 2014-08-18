package com.sebulli.fakturama.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A representation of the model object '<em><b>TextModule</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity()
@Table(name = "FKT_TEXTMODULE")
public class TextModule extends ModelObject implements Serializable {
    /**
     * A common serial ID.
     * 
     * @generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Long id = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "NAME")
    private String name = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "DELETED")
    private Boolean deleted = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "T_TEXT")
    private String text = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(joinColumns = { @JoinColumn(name = "TEXTMODULE_CATEGORIES") }, inverseJoinColumns = { @JoinColumn(name = "CATEGORIES_TEXTCATEGORY") }, name = "FKT_TEXTMODULE_CATEGORIES")
    private List<TextCategory> categories = new ArrayList<TextCategory>();

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Basic()
    @Column(name = "MODIFIED")
    @Temporal(TemporalType.DATE)
    private Date modified = null;

    /**
     * Returns the value of '<em><b>id</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>id</b></em>' feature
     * @generated
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the '{@link TextModule#getId() <em>id</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newId
     *            the new value of the '{@link TextModule#getId() id}' feature.
     * @generated
     */
    public void setId(Long newId) {
        id = newId;
        firePropertyChange("id", this.id, newId);
    }

    /**
     * Returns the value of '<em><b>name</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>name</b></em>' feature
     * @generated
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the '{@link TextModule#getName() <em>name</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newName
     *            the new value of the '{@link TextModule#getName() name}'
     *            feature.
     * @generated
     */
    public void setName(String newName) {
        name = newName;
        firePropertyChange("name", this.name, newName);
    }

    /**
     * Returns the value of '<em><b>deleted</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>deleted</b></em>' feature
     * @generated
     */
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * Sets the '{@link TextModule#getDeleted() <em>deleted</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newDeleted
     *            the new value of the '{@link TextModule#getDeleted() deleted}'
     *            feature.
     * @generated
     */
    public void setDeleted(Boolean newDeleted) {
        deleted = newDeleted;
        firePropertyChange("deleted", this.deleted, newDeleted);
    }

    /**
     * Returns the value of '<em><b>text</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>text</b></em>' feature
     * @generated
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the '{@link TextModule#getText() <em>text</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newText
     *            the new value of the '{@link TextModule#getText() text}'
     *            feature.
     * @generated
     */
    public void setText(String newText) {
        text = newText;
        firePropertyChange("text", this.text, newText);
    }

    /**
     * Returns the value of '<em><b>categories</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>categories</b></em>' feature
     * @generated
     */
    public List<TextCategory> getCategories() {
        return categories;
    }

    /**
     * Adds to the <em>categories</em> feature.
     *
     * @param categoriesValue
     *            value to add
     *
     * @generated
     */
    public boolean addToCategories(TextCategory categoriesValue) {
        if (!categories.contains(categoriesValue)) {
            categories.add(categoriesValue);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Removes from the <em>categories</em> feature.
     *
     * @param categoriesValue
     *            value to remove
     *
     * @generated
     */
    public boolean removeFromCategories(TextCategory categoriesValue) {
        if (categories.contains(categoriesValue)) {
            categories.remove(categoriesValue);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Clears the <em>categories</em> feature.
     * 
     * @generated
     */
    public void clearCategories() {
        while (!categories.isEmpty()) {
            removeFromCategories(categories.iterator().next());
        }
    }

    /**
     * Sets the '{@link TextModule#getCategories() <em>categories</em>}'
     * feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newCategories
     *            the new value of the '{@link TextModule#getCategories()
     *            categories}' feature.
     * @generated
     */
    public void setCategories(List<TextCategory> newCategories) {
        categories = newCategories;
        firePropertyChange("categories", this.categories, newCategories);
    }

    /**
     * Returns the value of '<em><b>modified</b></em>' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of '<em><b>modified</b></em>' feature
     * @generated
     */
    public Date getModified() {
        return modified;
    }

    /**
     * Sets the '{@link TextModule#getModified() <em>modified</em>}' feature.
     *
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param newModified
     *            the new value of the '{@link TextModule#getModified()
     *            modified}' feature.
     * @generated
     */
    public void setModified(Date newModified) {
        modified = newModified;
        firePropertyChange("modified", this.modified, newModified);
    }

    /**
     * A toString method which prints the values of all EAttributes of this
     * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        return "TextModule " + " [id: " + getId() + "]" + " [name: " + getName() + "]" + " [deleted: " + getDeleted() + "]" + " [text: " + getText() + "]"
                + " [modified: " + getModified() + "]";
    }
}
