package com.tmukimi.hospital_management.config;

import com.tmukimi.hospital_management.entities.CustomRevisionEntity;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            customRevisionEntity.setModifiedBy(authentication.getName());
        } else {
            customRevisionEntity.setModifiedBy("SYSTEM_GENERATED");
        }
    }
}