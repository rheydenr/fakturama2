package com.sebulli.fakturama.model;

import java.util.Calendar;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class EntityListener {
    public static ThreadLocal currentUser = new ThreadLocal();

    @PrePersist
    public void aboutToInsert(IEntity document) {
        document.setDateAdded(Calendar.getInstance().getTime());
        document.setModifiedBy((String) EntityListener.currentUser.get());
    }

    @PreUpdate
    public void aboutToUpdate(IEntity document) {
        document.setModified(Calendar.getInstance().getTime());
        document.setModifiedBy((String) EntityListener.currentUser.get());
    }

}
