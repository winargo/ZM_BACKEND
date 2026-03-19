package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.BmLog;
import com.billermanagement.persistance.domain.TransTmp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



@Repository
public interface BmLogRepository extends JpaRepository<BmLog, Integer>, JpaSpecificationExecutor<BmLog> {

    @Query(value="SELECT * FROM BM_LOG where PARTNER_TID=?1 LIMIT 1", nativeQuery = true)
    BmLog findLogByPartnerTid(String partnerTid);

    @Query(value="SELECT * FROM BM_LOG where BM_TID=?1", nativeQuery = true)
    BmLog findLogByBmTid(String bmTid);

    @Query(value="SELECT * FROM BM_LOG where PARTNER_TID=?1 and BM_TID=?2", nativeQuery = true)
    BmLog findLogByPartnerTidAndBmTid(String partnerTid,String bmTid);
}
