package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.Api;
import com.billermanagement.persistance.domain.resultset.PartnerProduct;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiRepository extends BaseRepository<Api> {
    @Query(value="SELECT * FROM API where API_ID=?1", nativeQuery = true)
    Optional<Api> findByApiId(String id);

    List<Api> findByApiCategory(String category);

    @Query(value="SELECT DISTINCT api_category FROM API", nativeQuery = true)
    List<String> findDistinctCategory();

    @Query(value = "select distinct(a.API_CATEGORY) from API a, PARTNER_API b, PARTNER c " +
            "where a.ID = b.API_ID and b.PARTNER_ID = c.id and c.PARTNER_ALIAS = ?1", nativeQuery = true)
    List<String> findPartnerCategory(String partner);

    @Query(value = "select a.API_CATEGORY as category, a.API_ID as productCode, a.API_NAME as productName, " +
            "a.API_DESCRIPTION as productDesc, a.NOMINAL as nominal, b.PARTNER_PRICE as partnerPrice, b.PARTNER_FEE as partnerFee " +
            "from API a, PARTNER_API b, PARTNER c " +
            "where a.ID = b.API_ID " +
            "and b.PARTNER_ID = c.id " +
            "and c.PARTNER_ALIAS = :partner " +
            "and (:category is null or UPPER(a.API_CATEGORY) = :category) " +
            "order by a.API_CATEGORY, a.API_ID", nativeQuery = true)
            //"and UPPER(a.API_CATEGORY) = 'AIRTIME'", nativeQuery = true)
    List<PartnerProduct> findPartnerProduct(@Param("partner") String partner, @Param("category") String category);

    @Query(value="select distinct API_CATEGORY from API where API_ID=?1",nativeQuery=true)
    String findCategoryByProductCode(String productCode);
}
