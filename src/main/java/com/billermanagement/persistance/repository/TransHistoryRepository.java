package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.TransHistory;
import com.billermanagement.persistance.domain.resultset.TransHistoryResult;
import com.billermanagement.persistance.domain.resultset.TransStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransHistoryRepository extends PagingAndSortingRepository<TransHistory, Integer>, JpaSpecificationExecutor<TransHistory> {
    /*
    @Query(value="SELECT * FROM TRANS_HISTORY where (:partnerId is null or PARTNER_ID = :partnerId) " +
            "and (:billerId is null or BILLER_ID = :billerId) " +
            "and (:category is null or PARTNER_CODE in (select API_ID from API where API_CATEGORY = :category)) " +
            "and DATE_CREATED between :startDate and :endDate", nativeQuery = true)
    List<TransHistory> findOldRecord(@Param("partnerId") Integer partnerId, @Param("billerId") Integer billerId,
                                     @Param("category") String category, @Param("startDate") String startDate,
                                     @Param("endDate") String endDate);
    */
    @Query(value = "SELECT a.id as id, a.DATE_CREATED as creationDate, a.BM_TID as bmTid, a.STATUS as status, " +
            "a.PARTNER_TID as partnerTid, a.PARTNER_ID as partnerId, b.PARTNER_NAME as partnerName, a.PARTNER_CODE as partnerCode, " +
            "a.PARTNER_PRICE as partnerPrice, a.PARTNER_FEE as partnerFee, " +
            "a.BILLER_TID as billerTid, a.BILLER_ID as billerId, c.BILLER_NAME as billerName, a.BILLER_CODE as billerCode, " +
            "a.BILLER_PRICE as billerPrice, a.ADMIN_FEE as billerFee " +
            "FROM TRANS_HISTORY a, PARTNER b, BILLER c " +
            "where (:partnerId is null or a.PARTNER_ID = :partnerId) " +
            "and (:billerId is null or a.BILLER_ID = :billerId) " +
            "and (:category is null or a.PARTNER_CODE in (select API_ID from API where API_CATEGORY = :category)) " +
            "and a.DATE_CREATED between :startDate and :endDate " +
            "and a.PARTNER_ID = b.id " +
            "and a.BILLER_ID = c.id", nativeQuery = true)
    List<TransHistoryResult> findRecord(@Param("partnerId") Integer partnerId, @Param("billerId") Integer billerId,
                                        @Param("category") String category, @Param("startDate") String startDate,
                                        @Param("endDate") String endDate);

    @Query(value = "SELECT a.id as id, a.DATE_CREATED as creationDate, a.BM_TID as bmTid, a.STATUS as status, " +
            "a.PARTNER_TID as partnerTid, a.PARTNER_ID as partnerId, b.PARTNER_NAME as partnerName, a.PARTNER_CODE as partnerCode, " +
            "a.PARTNER_PRICE as partnerPrice, a.PARTNER_FEE as partnerFee, " +
            "a.BILLER_TID as billerTid, a.BILLER_ID as billerId, c.BILLER_NAME as billerName, a.BILLER_CODE as billerCode, " +
            "a.BILLER_PRICE as billerPrice, a.ADMIN_FEE as billerFee " +
            "FROM TRANS_HISTORY a, PARTNER b, BILLER c " +
            "where (:partnerId is null or a.PARTNER_ID = :partnerId) " +
            "and (:billerId is null or a.BILLER_ID = :billerId) " +
            "and (:category is null or a.PARTNER_CODE in (select API_ID from API where API_CATEGORY = :category)) " +
            "and a.DATE_CREATED between :startDate and :endDate " +
            "and ((:search is NULL or a.BM_TID=:search) or (:search is NULL or a.PARTNER_TID=:search) or (:search is null or a.STATUS like CONCAT('%',:search,'%'))) "+
            "and a.PARTNER_ID = b.id " +
            "and a.BILLER_ID = c.id", nativeQuery = true)
    List<TransHistoryResult> findRecord2(@Param("partnerId") Integer partnerId,
                                         @Param("billerId") Integer billerId,
                                         @Param("category") String category,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("search") String search,
                                         Pageable pageable);


    @Query(value = "SELECT count(a.id) AS total " +
            "FROM TRANS_HISTORY a, PARTNER b, BILLER c " +
            "where (:partnerId is null or a.PARTNER_ID = :partnerId) " +
            "and (:billerId is null or a.BILLER_ID = :billerId) " +
            "and (:category is null or a.PARTNER_CODE in (select API_ID from API where API_CATEGORY = :category)) " +
            "and a.DATE_CREATED between :startDate and :endDate " +
            "and ((:search is NULL or a.BM_TID=:search) or (:search is NULL or a.PARTNER_TID=:search) or (:search is null or a.STATUS like CONCAT('%',:search,'%'))) "+
            "and a.PARTNER_ID = b.id " +
            "and a.BILLER_ID = c.id", nativeQuery = true)
    int countFindRecord2(@Param("partnerId") Integer partnerId,
                         @Param("billerId") Integer billerId,
                         @Param("category") String category,
                         @Param("startDate") String startDate,
                         @Param("endDate") String endDate,
                         @Param("search") String search);

    @Query(value = "select BM_TID as transId, PARTNER_TID as requestId, STATUS as status, " +
            "SUBSTRING(DATE_FORMAT(DATE_CREATED,'%Y%m%d%H%i%s%f'), 1, 17) as datetime " +
            "from TRANS_HISTORY " +
            "where (:requestId is null or PARTNER_TID = :requestId) " +
            "and (:transId is null or BM_TID = :transId)", nativeQuery = true)
    Optional<TransStatus> findTransaction(@Param("requestId") String requestId, @Param("transId") String transId);

    @Modifying
    @Query(value = "update TRANS_HISTORY set BILLER_PRICE=?1, ADMIN_FEE=?2, BILLER_STATUS=?3, BM_STATUS=?4, STATUS=?5 " +
            "where BM_TID=?6", nativeQuery = true)
    void updateTransaction(int billerPrice, int adminFee, String billerStatus, String bmStatus, String status, String bmTid);

    @Modifying
    @Query(value = "update TRANS_HISTORY set BILLER_PRICE=?1, ADMIN_FEE=?2, BILLER_STATUS=?3, BM_STATUS=?4, STATUS=?5 " +
            "where BM_TID=?6 and PARTNER_TID=?7", nativeQuery = true)
    void updateTransaction2(int billerPrice, int adminFee, String billerStatus, String bmStatus, String status, String bmTid,String partnerTid);

}
