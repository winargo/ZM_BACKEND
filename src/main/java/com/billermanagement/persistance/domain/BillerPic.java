package com.billermanagement.persistance.domain;

import com.billermanagement.vo.ui.BillerPicVO;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "BILLER_PIC")
@DynamicUpdate
@Data
public class BillerPic extends Base {
    @ManyToOne
    @JoinColumn(name = "BILLER_ID")
    private Biller biller;

    @Column(name = "TEAM", length = 16)
    private String team;

    @Column(name = "NAME", length = 32)
    private String name;

    @Column(name = "CONTACT", length = 16)
    private String contact;

    @Column(name = "EMAIL", length = 32)
    private String email;

    @Column(name = "STATUS", columnDefinition="BOOLEAN", nullable = false)
    private boolean status;

    public BillerPic() { }

    public BillerPic(BillerPicVO vo){
        setValue(vo);
    }

    public void update(BillerPicVO vo){
        setValue(vo);
    }

    private void setValue(BillerPicVO vo) {
        team = vo.getTeam();
        name = vo.getName();
        contact = vo.getContact();
        email = vo.getEmail();
    }

    @Override
    public void prePersist() {
        this.status = true;
        super.prePersist();
    }
}
