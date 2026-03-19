package com.billermanagement.persistance.domain;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "BM_LOG")
@DynamicUpdate
@Data
public class BmLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmlog_generator")
    @SequenceGenerator(name="bmlog_generator", sequenceName = "bmlog_seq")
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
        this.createdBy = "System";
    }

    @Column(name = "REQUEST_METHOD", columnDefinition="VARCHAR(16)")
    private String method;

    @Column(name = "PARTNER_TID", columnDefinition="VARCHAR(64)")
    private String partnerTid;

    @Column(name = "BILLER_TID", columnDefinition="VARCHAR(45)")
    private String billerTid;

    @Column(name = "BM_TID", columnDefinition="VARCHAR(64)")
    private String bmTid;

    @Column(name = "REQ_FE", columnDefinition="TEXT")
    private String reqFe;

    @Column(name = "RES_FE", columnDefinition="TEXT")
    private String resFe;

    @Column(name = "CB_FE", columnDefinition="TEXT")
    private String cbFe;

    @Column(name = "REQ_BE", columnDefinition="TEXT")
    private String reqBe;

    @Column(name = "RES_BE", columnDefinition="TEXT")
    private String resBe;

    @Column(name = "CB_BE", columnDefinition="TEXT")
    private String cbBe;

    public BmLog() { }
}
