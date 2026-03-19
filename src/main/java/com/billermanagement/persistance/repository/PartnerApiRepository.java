package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.PartnerApi;
import com.billermanagement.persistance.domain.resultset.PartnerResult;
import com.billermanagement.persistance.domain.resultset.PartnerSelectionResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerApiRepository extends BaseRepository<PartnerApi> {

    //@Query(value="SELECT API_ID FROM PARTNER_API where PARTNER_ID=?1 and PARTNER_CODE=?2", nativeQuery = true)
    @Query(value="SELECT a.API_ID FROM PARTNER_API a, API b where a.API_ID=b.id and a.PARTNER_ID=?1 and b.API_ID=?2", nativeQuery = true)
    int findApiId(String partnerId, String partnerCode);

    @Query(value="SELECT * FROM PARTNER_API where PARTNER_ID=?1", nativeQuery = true)
    List<PartnerApi> findByPartnerId(int id);

    //@Query(value="SELECT * FROM PARTNER_API where PARTNER_CODE=?1", nativeQuery = true)
    @Query(value = "SELECT * FROM PARTNER_API a, API b where a.API_ID=b.ID and b.API_ID=?1", nativeQuery = true)
    PartnerApi findByPartnerCode(String code);

    @Query(value="select concat(a.PARTNER_ID,'.',b.API_ID) as apiId, b.API_SELECTION as apiSelection " +
            "from PARTNER_API a, API b where a.API_ID=b.id and a.STATUS=true", nativeQuery = true)
    List<PartnerSelectionResult> findPartnerIdAndSelection();

    @Query(value="select b.API_ID as apiId, b.API_SELECTION as apiSelection from PARTNER_API a, " +
            "API b where a.API_ID=b.id and a.PARTNER_ID=?1 and b.API_ID=?2 and a.STATUS=true", nativeQuery = true)
    PartnerSelectionResult findPartnerIdAndSelection(int partnerId, String partnerCode);

    @Query(value="SELECT * FROM PARTNER_API " +
        "where PARTNER_ID=?1 and API_ID in " +
        "(select ID from API where lower(API_CATEGORY) = lower(?2))", nativeQuery = true)
    List<PartnerApi> findByPartnerIdAndCategory(int id, String category);

    @Query(value = "select PARTNER_PRICE as partnerPrice, PARTNER_FEE as partnerFee, URL as partnerUrl from PARTNER_API " +
            "where PARTNER_ID=(select id from PARTNER where PARTNER_ALIAS=?1) " +
            "and API_ID=(select id from API where API_ID=?2)", nativeQuery = true)
    PartnerResult findPartnerInfo(String alias, String apiId);

    @Query(value = "select concat(a.PARTNER_ALIAS,'.',b.API_ID) as partner, c.PARTNER_PRICE as partnerPrice, c.PARTNER_FEE as partnerFee, " +
            "c.URL as partnerUrl from PARTNER a, API b, PARTNER_API c where c.PARTNER_ID=a.id and c.API_ID=b.id", nativeQuery = true)
    List<PartnerResult> findPartnerInfo();

}
