package com.billermanagement.persistance.domain;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TRANS_HISTORY")
@DynamicUpdate
@Data
//public class TransHistory implements Serializable {
public class TransHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trans_generator")
    @SequenceGenerator(name="trans_generator", sequenceName = "trans_seq")
    private Integer id;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_CREATED", updatable = false, nullable = false)
    private Date creationDate;

    @CreatedBy
    @Column(name = "CREATED_BY", length = 8, updatable = false)
    private String createdBy;

    @Column(name = "PARTNER_ID")
    private int partnerId;

    @Column(name = "BILLER_ID")
    private int billerId;

    @Column(name = "PARTNER_TID", columnDefinition="VARCHAR(64)")
    private String partnerTid;

    @Column(name = "BILLER_TID", columnDefinition="VARCHAR(45)")
    private String billerTid;

    @Column(name = "BM_TID", columnDefinition="VARCHAR(64)")
    private String bmTid;

    @Column(name = "PARTNER_CODE", columnDefinition="VARCHAR(16)")
    private String partnerCode;

    @Column(name = "BILLER_CODE", columnDefinition="VARCHAR(16)")
    private String billerCode;

    @Column(name = "PARTNER_PRICE", columnDefinition="INT")
    private int partnerPrice;

    @Column(name = "PARTNER_FEE", columnDefinition="INT")
    private int partnerFee;

    @Column(name = "BILLER_PRICE", columnDefinition="INT")
    private int billerPrice;

    //@Column(name = "DENOM", columnDefinition="INT")
    //private int denom;

    @Column(name = "ADMIN_FEE", columnDefinition="INT")
    private int adminFee;

    @Column(name = "STATUS", columnDefinition="VARCHAR(8)")
    private String status;

    @Column(name = "BM_STATUS", columnDefinition="VARCHAR(8)")
    private String bmStatus;

    @Column(name = "BILLER_STATUS", columnDefinition="VARCHAR(100)")
    private String billerStatus;

    @PrePersist
    public void prePersist() {
        this.creationDate = new Date();
    }

}
