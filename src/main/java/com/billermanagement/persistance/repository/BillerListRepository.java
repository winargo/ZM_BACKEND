package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.BillerList;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BillerListRepository extends BaseRepository<BillerList> {
    @Modifying
    @Query(value = "DELETE FROM BILLER_LIST WHERE PARTNER_API_ID is NULL", nativeQuery = true)
    void deleteNullPartnerApiId();
}
