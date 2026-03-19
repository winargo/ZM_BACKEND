package com.billermanagement.persistance.domain;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by yukibuwana on 1/24/17.
 */

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@Data
public abstract class Base implements Serializable {

    private static final long serialVersionUID = -7369920601847524273L;

    @Id
    @GeneratedValue
    protected Integer id;

    /**
     * Random id for security reason
     */
    @Column(name = "SECURE_ID", unique = true, length = 36, nullable = false)
    private String secureId;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_CREATED", updatable = false, nullable = false)
    private Date creationDate;

    @CreatedBy
    @Column(name = "CREATED_BY", length = 8, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_MODIFIED")
    private Date modificationDate;

    @LastModifiedBy
    @Column(name = "MODIFIED_BY", length = 8)
    private String modifiedBy;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version = 0;

    @PrePersist
    public void prePersist() {
        this.secureId = UUID.randomUUID().toString();
        this.creationDate = new Date();
    }

    @PreUpdate
    public void preUpdate(){
        this.modificationDate = new Date();
    }
}
