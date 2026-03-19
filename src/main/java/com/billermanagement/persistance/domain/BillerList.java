package com.billermanagement.persistance.domain;

import com.billermanagement.vo.ui.BillerListVO;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

@Entity
@Table(name = "BILLER_LIST")
@DynamicUpdate
@Data
public class BillerList extends Base {
    @Column(name = "PARTNER_API_ID")
    private int partnerApiId;

    @Column(name = "BILLER_ID", columnDefinition="INT")
    private int billerId;

    @Column(name = "PRIORITY", columnDefinition="TINYINT")
    private int priority;

    @ManyToOne
    @JoinColumn(name = "BILLER_API_ID")
    private BillerApi billerApiId;

    public BillerList() { }

    public BillerList(BillerListVO vo) { }

}
