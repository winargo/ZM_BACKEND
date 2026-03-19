package com.billermanagement.vo.InstamoneyVO.CreateCustomer;

import com.billermanagement.vo.InstamoneyVO.AccountDetail;
import com.billermanagement.vo.InstamoneyVO.Address;
import com.billermanagement.vo.InstamoneyVO.Identification;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IMCreateCustomerReqVO {

    @JsonProperty("external_id")
    private String externalId;

    @JsonProperty("customer_type")
    private String customerType;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("surname")
    private String surname;

//    @JsonProperty("date_of_birth")
//    private String dob;

//    @JsonProperty("email")
//    private String email;
//
//    @JsonProperty("phone_number")
//    private String phoneNumber;
//
//    @JsonProperty("mobile_number")
//    private String mobileNumber;

    @JsonProperty("address")
    private Address address;

//    @JsonProperty("identification")
//    private Identification identification;

    @JsonProperty("account_details")
    private AccountDetail accountDetail;

}
