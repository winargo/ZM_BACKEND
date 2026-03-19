package com.billermanagement.persistance.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "BM_CONFIG")
@AllArgsConstructor
@NoArgsConstructor
public class BMConfig{
    @Id
    @GeneratedValue
    private int id;

    private String param_name;

    private String param_value;
}
