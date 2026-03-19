package com.billermanagement.persistance.domain;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "RESULT_MAPPING")
@DynamicUpdate
@Data
public class ResultMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "config_generator")
    @SequenceGenerator(name="config_generator", sequenceName = "config_seq")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "BILLER_ID")
    private Biller biller;

    @Column(name = "BILLER_CODE", length = 8)
    private String billerCode;

    @Column(name = "BM_CODE", length = 8)
    private String bmCode;

    @Column(name = "DESCRIPTION")
    private String description;
}
