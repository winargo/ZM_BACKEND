package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.TransTmp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransTmpRepository extends JpaRepository<TransTmp, Integer>, JpaSpecificationExecutor<TransTmp> {

    //@Query(value="SELECT * FROM TRANS_TMP where PARTNER_TID=?1 and BILLER_TID=?2", nativeQuery = true)
    //Optional<TransTmp> findTrans(String partnerTid, String billerTid);

    @Query(value="SELECT * FROM TRANS_TMP where STATUS=?1", nativeQuery = true)
    List<TransTmp> findTrans(int status);

    @Query(value="SELECT * FROM TRANS_TMP where BM_TID=?1 and STATUS=?2", nativeQuery = true)
    TransTmp findTrans(String transId, int status);

    @Query(value="SELECT * FROM TRANS_TMP where REQUEST like CONCAT('%', :account, '%') and REQUEST like CONCAT('%', :method, '%') and STATUS=:status order by DATE_CREATED DESC LIMIT 1", nativeQuery = true)
    TransTmp findTransByAccount(String account, String method, int status);
    
    @Query(value="SELECT * FROM TRANS_TMP where BILLER_TID=?1", nativeQuery = true)
    TransTmp findTransByBillerTid(String billerTid);

    @Query(value = "SELECT DATE_ADD(DATE_CREATED,INTERVAL ?1 SECOND) AS SLA_TIME FROM TRANS_TMP WHERE id=?2",nativeQuery = true)
    Date getSLATime(int seconds, int id);
}
