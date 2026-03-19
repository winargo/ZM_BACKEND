package com.billermanagement.vo;

import com.billermanagement.persistance.domain.Role;
import lombok.Data;

@Data
public class UserResVO {
    private String id;
    private String username;
    private String email;
    private Role role;
}
