/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.Transform;
import com.billermanagement.vo.TransformDistinctVO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author sulaeman
 */
@Repository
public interface TransformRepository extends BaseRepository<Transform> {

    @Query(value = "SELECT * FROM TRANSFORM where transformId=?1 and method=?2 limit 1", nativeQuery = true)
    public Optional<Transform> findByTransformIdAndMethod(String transformId, String method);

    @Query(value = "select distinct transformId,name from TRANSFORM", nativeQuery = true)
    public List<TransformDistinctVO> getDistinct();

}
