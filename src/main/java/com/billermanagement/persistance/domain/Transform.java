/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.persistance.domain;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

/**
 *
 * @author sulaeman
 */
@Entity
@Table(name = "TRANSFORM")
@DynamicUpdate
@Data
public class Transform extends Base {

    private String transformId;
    private String method;
    private String name;
    private String type;
    private String url;
    private String flowType;
    @Lob
    private String fileRequest;
    @Lob
    private String fileResponse;
    @Lob
    private String fileCallback;
}
