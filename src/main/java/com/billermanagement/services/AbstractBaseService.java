package com.billermanagement.services;

import com.billermanagement.converter.IBaseVoConverter;
import com.billermanagement.persistance.domain.Base;
import com.billermanagement.persistance.repository.BaseRepository;
import com.billermanagement.util.Constants;
import com.billermanagement.vo.BaseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * Created by yukibuwana on 1/24/17.
 */
public abstract class AbstractBaseService<T extends Base, V extends BaseVO, Z> {

    protected abstract BaseRepository<T> getJpaRepository();

    protected abstract JpaSpecificationExecutor<T> getSpecRepository();

    protected abstract IBaseVoConverter<Z, V, T> getVoConverter();

    public abstract V add(Z vo);

    public abstract V update(String secureKey, Z vo);

    public abstract V update(Integer id, Z vo);

    public abstract Boolean delete(String secureKey);

    public abstract V findBySecureId(String secureKey);

    public abstract V findById(Integer id);

    public static Sort getSortBy(String sortBy, String direction) {
        if (direction.equalsIgnoreCase("asc")) {
            return new Sort(Sort.Direction.ASC, sortBy);
        } else {
            return new Sort(Sort.Direction.DESC, sortBy);
        }
    }

    protected Map<String, Object> constructMapReturn(Page<T> page) {
        Collection<V> vos = getVoConverter().transferListOfModelToListOfVO(page.getContent(), null);
        return constructMapReturn(vos, page.getTotalElements(), page.getTotalPages());
    }

    public static Map<String, Object> constructMapReturn(Collection voList, long totalElements, int totalPages) {
        Map<String, Object> map = new HashMap<>();

        map.put(Constants.PageParameter.LIST_DATA, voList);
        map.put(Constants.PageParameter.TOTAL_ELEMENTS, totalElements);
        map.put(Constants.PageParameter.TOTAL_PAGES, totalPages);

        return map;
    }

    protected Map<String, Object> constructMapReturn(Collection voList, Page<T> resultPage) {
        Map<String, Object> map = new HashMap<>();

        map.put(Constants.PageParameter.LIST_DATA, voList);
        if (resultPage != null){
            map.put(Constants.PageParameter.TOTAL_ELEMENTS, resultPage.getTotalElements());
            map.put(Constants.PageParameter.TOTAL_PAGES, resultPage.getTotalPages());
        }
        return map;
    }

    @Transactional(readOnly = true)
    protected Map<String, Object> search(Integer page, Integer limit, String sortBy, String direction,
                                         Specifications specs) {

        sortBy = StringUtils.isEmpty(sortBy) ? "id" : sortBy;
        direction = StringUtils.isEmpty(direction) ? "asc" : direction;

        Pageable pageable = new PageRequest(page, limit, getSortBy(sortBy, direction));
        Page<T> resultPage;

        if(specs == null){
            resultPage = getJpaRepository().findAll(pageable);
        }else{
            resultPage = getSpecRepository().findAll(specs, pageable);
        }

        List<T> models = resultPage.getContent();
        Collection<V> vos = getVoConverter().transferListOfModelToListOfVO(models, new ArrayList<>());

        return constructMapReturn(vos, resultPage.getTotalElements(), resultPage.getTotalPages());
    }

    public Map<String, Object> search(Integer page, Integer limit, String sortBy, String direction, String searchBy,
                                      String searchVal) {
        if(searchBy == null){
            return search(page, limit, sortBy, direction, null);
        }else{
            return search(page, limit, sortBy, direction, Specifications.where(dynamicSearchParams(searchBy,
                    searchVal)));
        }
    }

    public Map<String, Object> search(Integer page, Integer limit, String sortBy, String direction, String searchBy,
                                      String searchVal, Specifications additionalSpecs) {
        if (searchBy != null) {
            additionalSpecs = additionalSpecs.and(dynamicSearchParams(searchBy, searchVal));
        }
        return search(page, limit, sortBy, direction, additionalSpecs);

    }

    public Map<String, Object> search(Integer page, Integer limit, String sortBy, String direction, String searchBy,
                                      String searchVal, Specification additionalSpec) {
        Specifications<T> specs = Specifications.where(additionalSpec);
        if (searchBy != null) {
            specs = specs.and(dynamicSearchParams(searchBy, searchVal));
        }
        return search(page, limit, sortBy, direction, specs);
    }

    @Transactional(readOnly = true)
    protected Collection<V> search(String sortBy, String direction, Specifications specs) {

        sortBy = StringUtils.isEmpty(sortBy) ? "id" : sortBy;
        direction = StringUtils.isEmpty(direction) ? "asc" : direction;

        Sort sort = getSortBy(sortBy, direction);
        Collection<T> models;

        if(specs == null){
            models = getJpaRepository().findAll(sort);
        }else{
            models = getSpecRepository().findAll(specs, sort);
        }

        Collection<V> vos = getVoConverter().transferListOfModelToListOfVO(models, new ArrayList<V>());

        return vos;
    }

    public Collection<V> search(String sortBy, String direction, String searchBy, String searchVal) {

        if(searchBy == null) {
            return search(sortBy, direction, null);
        } else {
            return search(sortBy, direction, Specifications.where(dynamicSearchParams(searchBy, searchVal)));
        }
    }

    public Collection<V> search(String sortBy, String direction, String searchBy, String searchVal, Specifications additionalSpecs) {
        if (searchBy != null) {
            additionalSpecs = additionalSpecs.and(dynamicSearchParams(searchBy, searchVal));
        }
        return search(sortBy, direction, additionalSpecs);

    }

    public Collection<V> search(String sortBy, String direction, String searchBy, String searchVal, Specification additionalSpec) {
        Specifications<T> specs = Specifications.where(additionalSpec);
        if (searchBy != null) {
            specs = specs.and(dynamicSearchParams(searchBy, searchVal));
        }
        return search(sortBy, direction, specs);
    }

    public Specification<T> dynamicSearchParams(final String searchBy, final String searchVal) {

        if(StringUtils.isEmpty(searchBy) || StringUtils.isEmpty(searchVal)){
            return new Specification<T>() {
                @Override
                public Predicate toPredicate(Root<T> tRoot, CriteriaQuery<?> criteriaQuery,
                                             CriteriaBuilder criteriaBuilder) {
                    return null;
                }
            };
        }

        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate[] predicates = getPredicates(root, cb, searchBy, searchVal);
                return cb.and(predicates);
            }

            private Predicate[] getPredicates(Root<T> root, CriteriaBuilder cb, String searchBy, String searchVal){
                String[] searchBys = searchBy.split("\\-");
                String[] searchVals = searchVal.split("\\-");
                Predicate[] predicates = new Predicate[searchBys.length];
                for(int i=0; i<searchBys.length; i++){
                    Predicate predicate;
                    if(root.get(searchBys[i]).getJavaType() == String.class){
                        predicate = cb.like(cb.upper(root.get(searchBys[i]).as(String.class)), "%" +
                                searchVals[i].toUpperCase() + "%");
                    }else{
                        predicate = cb.equal(root.get(searchBys[i]), searchVals[i]);
                    }
                    predicates[i] = predicate;
                }
                return predicates;
            }
        };
    }
}
