package com.sebulli.fakturama.model;

import java.time.Instant;
import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class EntityListener {
    public static ThreadLocal currentUser = new ThreadLocal();

    @PrePersist
    public void aboutToInsert(IEntity document) {
        document.setDateAdded(Date.from(Instant.now()));
        document.setModifiedBy((String) EntityListener.currentUser.get());
    }

    @PreUpdate
    public void aboutToUpdate(IEntity document) {
        document.setModified(Date.from(Instant.now()));
        document.setModifiedBy((String) EntityListener.currentUser.get());
    }

}