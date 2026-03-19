package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.AuditTrail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditTrailRepository extends BaseRepository<AuditTrail> {
    @Query(value="SELECT * FROM AUDIT_TRAIL where owner_id=?1",
            nativeQuery = true)
    List<AuditTrail> findByOwnerId(int id);
}
