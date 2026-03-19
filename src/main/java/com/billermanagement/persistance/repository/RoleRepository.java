package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.Role;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends BaseRepository<Role>{

    @Query(value="SELECT * FROM ROLE where id=?1", nativeQuery = true)
    Role findByRoleId(String id);
}
