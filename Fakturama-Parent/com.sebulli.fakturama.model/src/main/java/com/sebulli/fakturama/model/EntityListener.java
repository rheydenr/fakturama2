package com.sebulli.fakturama.model;

import java.time.Instant;
import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class EntityListener {
//    public static ThreadLocal currentUser = new ThreadLocal();
    final String currentUser = System.getProperty("user.name", "(unknown)");

    @PrePersist
    public void aboutToInsert(IEntity document) {
        // sometimes we have to overtake an old date (esp. while migrating old data),
        // therefore we have to check if dateAdded is set before
        if(document.getDateAdded() == null) {
            document.setDateAdded(Date.from(Instant.now()));
        }
//        document.setModifiedBy((String) EntityListener.currentUser.get());
        document.setModifiedBy(currentUser);
    }

    @PreUpdate
    public void aboutToUpdate(IEntity document) {
        document.setModified(Date.from(Instant.now()));
//        document.setModifiedBy((String) EntityListener.currentUser.get());
        document.setModifiedBy(currentUser);
    }

}
