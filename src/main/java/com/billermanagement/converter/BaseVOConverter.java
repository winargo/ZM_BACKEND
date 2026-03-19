package com.billermanagement.converter;


import com.billermanagement.util.ExtendedSpringBeanUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by yukibuwana on 1/24/17.
 */
public abstract class BaseVOConverter<Z, V, T> implements IBaseVoConverter<Z, V, T> {

    /**
     * transfer value from vo object to domain object
     * for enum value, please do manually using Enum.values()[ordinal]
     *
     * @param vo
     * @param model
     * @return
     */
    @Override
    public T transferVOToModel(Z vo, T model) {

        ExtendedSpringBeanUtil.copySpecificProperties(vo, model,
                new String[]{"id"},
                new String[]{"secureId"});

        return model;
    }

    /**
     * transfer value from list of domain object to list of vo object
     *
     * @param models
     * @param vos
     * @return
     */
    @Override
    public Collection<V> transferListOfModelToListOfVO(Collection<T> models, Collection<V> vos) {
        if (null == vos) vos = new ArrayList<>();
        for (T model : models) {
            V vo = transferModelToVO(model, null);
            vos.add(vo);
        }
        return vos;
    }

    /**
     * transfer value from domain object to vo object
     *
     * @param model
     * @param vo
     * @return
     */
    @Override
    public V transferModelToVO(T model, V vo) {
        ExtendedSpringBeanUtil.copySpecificProperties(model,vo,
                new String[]{"secureId", "version"},
                new String[]{"id", "version"});
        return vo;
    }
}
