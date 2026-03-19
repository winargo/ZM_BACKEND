package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.BillerApi;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface BillerApiRepository extends BaseRepository<BillerApi> {
    @Query(value="SELECT * FROM BILLER_API where API_ID=?1 order by BILLER_PRICE, PRIORITY", nativeQuery = true)
    List<BillerApi> findByApiId(int apiId);

    @Query(value="SELECT * FROM BILLER_API where API_ID=?1 order by PRIORITY", nativeQuery = true)
    List<BillerApi> findByPriority(int apiId);

    @Query(value="SELECT * FROM BILLER_API where API_ID=?1 order by BILLER_PRICE", nativeQuery = true)
    List<BillerApi> findByPrice(int apiId);

    @Query(value = "select a.ID as id, a.BILLER_ID as billerId, a.BILLER_CODE as billerCode, a.TRANSFORM_ID as transformId, " +
            "a.BILLER_PRICE as billerPrice, a.ADMIN_FEE as adminFee, b.PARTNER_ID as partnerId " +
            "from BILLER_API a, PARTNER_API b, PARTNER c " +
            "where a.API_ID=b.API_ID and b.PARTNER_ID=c.ID and a.ID=?1 and c.PARTNER_ALIAS=?2", nativeQuery = true)
    Optional<BillerResult> findResultByBillerId(int id, String partnerAlias);



    @Query(value="SELECT * FROM BILLER_API where BILLER_ID=?1", nativeQuery = true)
    List<BillerApi> findByBillerId(int id);

    @Query(value="SELECT * FROM BILLER_API where BILLER_ID=?1 and API_ID=?2 and STATUS=true", nativeQuery = true)
    Optional<BillerApi> findByBillerAndApiId(int id, int api_id);

    /*@Query(value="select a.BILLER_ID as billerId, a.BILLER_CODE as billerCode, a.TRANSFORM_ID as transformId, a.PRIORITY as priority, a.BILLER_PRICE as billerPrice, a.ADMIN_FEE as adminFee, c.PARTNER_ID as partnerId, c.API_ID as apiId " +
            "from BILLER_API a, BILLER_LIST b, PARTNER_API c " +
            "where a.API_ID=c.API_ID " +
            "and b.PARTNER_API_ID=c.ID " +
            "and a.BILLER_ID=b.BILLER_ID " +
            "and c.PARTNER_ID=?1 " +
            "and c.PARTNER_CODE=?2 " +
            "and a.STATUS=true " +
            "order by a.PRIORITY", nativeQuery = true)
    List<BillerResult> findByPriority(int id, String code);*/

    /*@Query(value="select a.BILLER_ID as billerId, a.BILLER_CODE as billerCode, a.TRANSFORM_ID as transformId, a.PRIORITY as priority, a.BILLER_PRICE as billerPrice, a.ADMIN_FEE as adminFee, c.PARTNER_ID as partnerId, c.API_ID as apiId " +
            "from BILLER_API a, BILLER_LIST b, PARTNER_API c " +
            "where a.API_ID=c.API_ID " +
            "and b.PARTNER_API_ID=c.ID " +
            "and a.BILLER_ID=b.BILLER_ID " +
            "and c.PARTNER_ID=?1 " +
            "and c.PARTNER_CODE=?2 " +
            "and a.STATUS=true " +
            "order by a.BILLER_PRICE", nativeQuery = true)
    List<BillerResult> findByPrice(int id, String code);*/

    String GET_BILLER = "select a.ID as id, a.BILLER_ID as billerId, a.BILLER_CODE as billerCode, a.TRANSFORM_ID as transformId, " +
            "b.PRIORITY as priority, a.BILLER_PRICE as billerPrice, a.ADMIN_FEE as adminFee, c.PARTNER_ID as partnerId, " +
            "c.API_ID as apiId from BILLER_API a, BILLER_LIST b, PARTNER_API c, API d, BILLER e " +
            "where a.API_ID=c.API_ID " +
            "and a.BILLER_ID=e.id " +
            "and c.API_ID=d.ID " +
            "and a.ID=b.BILLER_API_ID " +
            "and a.BILLER_ID=b.BILLER_ID " +
            "and b.PARTNER_API_ID=c.ID " +
            "and c.PARTNER_ID=?1 " +
            "and d.API_ID=?2 " +
            "and a.STATUS=true " +
            "and e.STATUS=true ";

    @Query(value = GET_BILLER + "order by b.PRIORITY", nativeQuery = true)
    List<BillerResult> findByPriority(int id, String code);

    @Query(value = GET_BILLER + "order by a.BILLER_PRICE, b.PRIORITY", nativeQuery = true)
    List<BillerResult> findByPrice(int id, String code);

    @Query(value="select distinct API_CATEGORY " +
        "from API a, BILLER_API ba " +
        "where ba.API_ID=a.id " +
        "and BILLER_ID = ?1", nativeQuery = true)
    List<String> findDistinctCategoryByBillerId(int id);

    @Query(value="select * FROM BILLER_API " +
        "where BILLER_ID =?1 and API_ID in " +
        "(select ID from API where lower(API_CATEGORY) = lower(?2))", nativeQuery = true)
    List<BillerApi> findByBillerIdAndCategory(int billerId, String category);

    @Query(value="select a.* FROM BILLER_API a, BILLER b " +
        "where a.BILLER_ID=b.id " +
            "and a.STATUS=true " +
            "and b.STATUS=true " +
            "and a.API_ID in (select ID from API where lower(API_CATEGORY) = lower(?1))", nativeQuery = true)
    List<BillerApi> findByCategory(String category);
}
