package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.Partner;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerRepository extends BaseRepository<Partner> {
    @Query(value="SELECT id FROM PARTNER where PARTNER_ALIAS=?1", nativeQuery = true)
    int findIdByAlias(String alias);

    @Query(value="SELECT * FROM PARTNER where STATUS=true", nativeQuery = true)
    List<Partner> findActivePartner();
}
