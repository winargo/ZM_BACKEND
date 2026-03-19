package com.billermanagement.vo.InstamoneyVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Identification {

    @JsonProperty("ktp_number")
    private String ktp;

    @JsonProperty("npwp_number")
    private String npwp;

}
