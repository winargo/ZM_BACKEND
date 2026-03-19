package com.billermanagement.persistance.domain;

import com.billermanagement.vo.backend.TransTmpVO;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "TRANS_TMP")
@DynamicUpdate
@Data
//public class TransTmp implements Serializable {
public class TransTmp {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transTmp_generator")
    @SequenceGenerator(name="transTmp_generator", sequenceName = "transTmp_seq")
    private Integer id;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_CREATED", updatable = false, nullable = false)
    private Date creationDate;

    @CreatedBy
    @Column(name = "CREATED_BY", length = 8, updatable = false)
    private String createdBy;

    @PrePersist
    public void prePersist() {
        this.creationDate = new Date();
    }

    @Column(name = "PARTNER_CODE", columnDefinition="VARCHAR(16)")
    private String partnerCode;

    @Column(name = "PARTNER_ID")
    private int partnerId;

    @Column(name = "BILLER_ID")
    private int billerId;

    @Column(name = "PARTNER_NAME", columnDefinition="VARCHAR(16)")
    private String partnerName;

    @Column(name = "PARTNER_TID", columnDefinition="VARCHAR(64)")
    private String partnerTid;

    @Column(name = "PARTNER_PRICE", columnDefinition="INT")
    private int partnerPrice;

    @Column(name = "PARTNER_FEE", columnDefinition="INT")
    private int partnerFee;

    @Column(name = "BILLER_API_ID")
    private int billerApiId;

    @Column(name = "BILLER_TID", columnDefinition="VARCHAR(45)")
    private String billerTid;

    @Column(name = "BILLER_PRICE", columnDefinition="INT")
    private int billerPrice;

    @Column(name = "ADMIN_FEE", columnDefinition="INT")
    private int adminFee;

    @Column(name = "BM_TID", columnDefinition="VARCHAR(64)")
    private String bmTid;

    @Column(name = "REFF_ID", columnDefinition="VARCHAR(32)")
    private String reffId;

    @Column(name = "REQUEST_METHOD", columnDefinition="VARCHAR(16)")
    private String method;

    @Column(name = "TRANSFORM_ID", columnDefinition="VARCHAR(64)")
    private String transformId;

    @Column(name = "REQUEST", columnDefinition="TEXT")
    private String request;

    @Column(name = "RESPONSE", columnDefinition="TEXT")
    private String response;

    @Column(name = "URL")
    private String url;

    @Column(name = "PARTNER_URL")
    private String partnerUrl;

    @Column(name = "STATUS", columnDefinition="TINYINT")
    private int status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SLA_TIME",nullable = true)
    private Date slaTime;

    public TransTmp() { }

    /*public TransTmp(TransTmpVO vo) {
        setValues(vo);
    }

    public void update(TransTmpVO vo){
        setValue(vo);
    }

    private void setValue(TransTmpVO vo) {
        partnerTid = vo.getPartnerTid();
        billerTid = vo.getBillerTid();
        partnerCode = vo.getPartnerCode();
        transformId = vo.getTransformId();
        status = vo.getStatus();
    }*/

}
