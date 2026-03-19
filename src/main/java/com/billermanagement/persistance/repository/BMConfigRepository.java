package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.BMConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BMConfigRepository extends JpaRepository<BMConfig, Integer>, JpaSpecificationExecutor<BMConfig> {
    Optional<BMConfig> findById(int id);

    @Query(value = "select k.param_name FROM BM_CONFIG k WHERE k.param_name LIKE :prefix%", nativeQuery = true)
    List<String> selectNameByPrefix(String prefix);

    @Query(value = "select * FROM BM_CONFIG k WHERE k.param_name LIKE :prefix%", nativeQuery = true)
    List<BMConfig> selectByPrefix(String prefix);

    @Query(value = "select * FROM BM_CONFIG k WHERE k.param_name=?1", nativeQuery = true)
    BMConfig selectByParamName(String name);

    @Modifying
    @Query(value = "update BM_CONFIG set PARAM_VALUE=?1 where PARAM_NAME=?2", nativeQuery = true)
    void setInstanceStatus(String param_value, String param_name);

    @Modifying
    @Query(value = "update BM_CONFIG set PARAM_VALUE='1' where PARAM_NAME like 'instance.%'", nativeQuery = true)
    void updateInstanceStatus();
}
