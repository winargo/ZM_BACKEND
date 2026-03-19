package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.Biller;
import com.billermanagement.persistance.domain.BillerApi;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillerRepository extends BaseRepository<Biller> {
    /*@Query(value = "select a.ID as id, a.BILLER_ALIAS as billerAlias, a.BILLER_NAME as billerName, " +
            "b.BILLER_PRICE as billerPrice from BILLER a, BILLER_API b " +
            "where a.ID=b.BILLER_ID and a.ID=?1 and b.API_ID=?2", nativeQuery = true)
    Optional<BillerAndApiResult> findByBillerAndApiId(int billerId, int apiId);*/

    @Query(value = "select BILLER_ALIAS from BILLER where ID=?1", nativeQuery = true)
    String findBillerAlias(int id);

    @Query(value = "select * from BILLER where STATUS=true", nativeQuery = true)
    List<Biller> findActiveBiller();
}
