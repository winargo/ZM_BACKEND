package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.ResultMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultMappingRepository extends JpaRepository<ResultMapping, Integer>, JpaSpecificationExecutor<ResultMapping> {
}
