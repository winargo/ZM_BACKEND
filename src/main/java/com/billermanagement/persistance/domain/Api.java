package com.billermanagement.persistance.domain;

import com.billermanagement.vo.ui.ApiVO;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "API")
@DynamicUpdate
@Data
public class Api extends Base {
    @Column(name = "API_ID", length = 16, unique = true, nullable = false)
    private String apiId;

    @Column(name = "API_NAME", length = 100)
    private String apiName;

    @Column(name = "API_DESCRIPTION", length = 100)
    private String apiDescription;

    @Column(name = "API_CATEGORY", length = 100)
    private String apiCategory;

    @Column(name = "API_SELECTION", length = 12)
    private String apiSelection;

    @Column(name = "NOMINAL", columnDefinition="INT")
    private int nominal;

    public Api() { }

    public Api(ApiVO vo){
        setValue(vo);
    }

    public void update(ApiVO vo){
        setValue(vo);
    }

    private void setValue(ApiVO vo) {
        apiId = vo.getApiId();
        apiName = vo.getApiName();
        apiDescription = vo.getApiDescription();
        apiCategory = vo.getApiCategory();
        apiSelection = vo.getApiSelection();
        nominal = vo.getNominal();
    }

}
