package com.billermanagement.persistance.domain;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ROLE")
@DynamicUpdate
@Data
public class Role extends Base{
    @Column(name = "NAME",nullable = false)
    private String name;

    @Column(name = "PAGES",nullable = false)
    private String pages;

}
