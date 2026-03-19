package com.billermanagement.persistance.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "USER")
@DynamicUpdate
@Data
public class User extends Base{

    @Column(name = "USERNAME",nullable = false)
    private String username;

    //@Column(name = "PASSWORD",nullable = false)
    //private String password;

    @Column(name = "EMAIL",nullable = false)
    private String email;

    @Column(name = "ROLES",nullable = false)
    private String roles;
}
