package com.billermanagement.persistance.repository;

import com.billermanagement.persistance.domain.Base;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Created by yukibuwana on 1/24/17.
 */

@NoRepositoryBean
public interface BaseRepository<T extends Base> extends JpaRepository<T, Integer>, JpaSpecificationExecutor<T> {

    Optional<T> findBySecureId(String secureId);
}
