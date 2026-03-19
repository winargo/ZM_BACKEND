package com.billermanagement.converter;

import java.util.Collection;

/**
 * Created by yukibuwana on 1/24/17.
 */

public interface IBaseVoConverter<Z, V, T> {

    /**
     * transfer value from vo object to domain object
     * for enum value, please do manually using Enum.values()[ordinal]
     * @param vo
     * @param model
     * @return
     */
    T transferVOToModel(Z vo, T model);

    /**
     * transfer value from list of domain object to list of vo object
     * @param models
     * @param vos
     * @return
     */
    Collection<V> transferListOfModelToListOfVO(Collection<T> models, Collection<V> vos);

    /**
     * transfer value from domain object to vo object
     * @param model
     * @param vo
     * @return
     */
    V transferModelToVO(T model, V vo);
}
