package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.BillerPic;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillerPicRepository extends BaseRepository<BillerPic> {
    @Query(value="SELECT * FROM BILLER_PIC where BILLER_ID=?1", nativeQuery = true)
    List<BillerPic> findByBillerId(int id);
}
