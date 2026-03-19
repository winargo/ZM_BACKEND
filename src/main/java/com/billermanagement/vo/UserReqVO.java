package com.billermanagement.vo;

import lombok.Data;

@Data
public class UserReqVO {
    private String id;
    private String username;
    private String email;
    private String role;
}
