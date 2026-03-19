package com.billermanagement.persistance.domain;

import com.billermanagement.vo.ui.PartnerApiVO;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "PARTNER_API")
@DynamicUpdate
@Data
public class PartnerApi extends Base {
    @ManyToOne
    @JoinColumn(name = "PARTNER_ID")
    private Partner partner;

    @ManyToOne
    @JoinColumn(name = "API_ID")
    private Api api;

    @Column(name = "PARTNER_PRICE", columnDefinition="INT")
    private int partnerPrice;

    @Column(name = "PARTNER_FEE", columnDefinition="INT")
    private int partnerFee;

    @Column(name = "FEE_TYPE", length = 8)
    private String feeType;

    @Column(name = "URL", length = 128)
    private String url;

    @OneToMany
    //@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "PARTNER_API_ID")
    private List<BillerList> billerApiId;
    //@Fetch(value= FetchMode.SELECT)
    //private List<BillerList> billerApiId;
    //private List<BillerAndApiResult> billerAndApiResults;

    @Column(name = "STATUS", columnDefinition="BOOLEAN", nullable = false)
    private boolean status;

    public PartnerApi() { }

    public PartnerApi(PartnerApiVO vo){
        setValue(vo);
    }

    public void update(PartnerApiVO vo){
        setValue(vo);
    }

    private void setValue(PartnerApiVO vo) {
        //partnerCode = vo.getPartnerCode();
        partnerPrice = vo.getPartnerPrice();
        partnerFee = vo.getPartnerFee();
        feeType = vo.getFeeType();
        url = vo.getUrl();
    }

    @Override
    public void prePersist() {
        this.status = true;
        super.prePersist();
    }
}
