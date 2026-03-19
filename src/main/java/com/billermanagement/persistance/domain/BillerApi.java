package com.billermanagement.persistance.domain;

import com.billermanagement.vo.ui.BillerApiVO;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "BILLER_API")
@DynamicUpdate
@Data
public class BillerApi extends Base {
    @ManyToOne
    @JoinColumn(name = "BILLER_ID")
    private Biller biller;

    @ManyToOne
    @JoinColumn(name = "API_ID")
    private Api api;

    @Column(name = "TRANSFORM_ID", length = 64)
    private String transformId;

    @Column(name = "BILLER_CODE", length = 50)
    private String billerCode;

    @Column(name = "BILLER_PRICE", columnDefinition="INT")
    private int billerPrice;

    @Column(name = "PRIORITY", columnDefinition="TINYINT")
    private int priority;

    @Column(name = "DENOM", columnDefinition="INT")
    private int denom;

    @Column(name = "ADMIN_FEE", columnDefinition="INT")
    private int adminFee;

    @Column(name = "FEE_TYPE", length = 8)
    private String feeType;

    @Column(name = "STATUS", columnDefinition="BOOLEAN", nullable = false)
    private boolean status;

    /*public Integer getTransformId() {
        return transform.getId();
    }*/

    public BillerApi() { }

    public BillerApi(BillerApiVO vo){
        setValue(vo);
    }

    public void update(BillerApiVO vo){
        setValue(vo);
    }

    private void setValue(BillerApiVO vo) {
        transformId = vo.getTransformId();
        billerCode = vo.getBillerCode();
        billerPrice = vo.getBillerPrice();
        priority = vo.getPriority();
        denom = vo.getDenom();
        adminFee = vo.getAdminFee();
        feeType = vo.getFeeType();
    }

    @Override
    public void prePersist() {
        this.status = true;
        super.prePersist();
    }
}
