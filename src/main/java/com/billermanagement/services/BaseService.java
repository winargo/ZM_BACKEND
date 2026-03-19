package com.billermanagement.services;

import com.billermanagement.config.Messages;
import com.billermanagement.exception.DataNotFoundException;
import com.billermanagement.persistance.domain.Base;
import com.billermanagement.vo.BaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Created by yukibuwana on 1/24/17.
 */
@Transactional(readOnly = true)
public abstract class BaseService<T extends Base, V extends BaseVO, Z> extends AbstractBaseService<T,V, Z> {

    @Autowired
    Messages messages;

    protected V add(T t) {
        T updated = getJpaRepository().save(t);

        if (null != updated.getId()) {
            V vo = getVoConverter().transferModelToVO(updated, null);

            if(null == updated.getSecureId() || updated.getSecureId().isEmpty()) {
                throw new DataNotFoundException(messages.get("error.not.found", new String[]{updated.getSecureId()}));
            }

            vo.setId(updated.getSecureId());
            return vo;
        }
        return null;
    }

    @Transactional
    @Override
    public V add(Z vo) {
        T t = getVoConverter().transferVOToModel(vo, null);

        return this.add(t);
    }

    @Transactional
    @Override
    public V update(String secureKey, Z vo) {
        Optional<T> t = getJpaRepository().findBySecureId(secureKey);
        return this.update(t.get(), vo);
    }

    @Transactional
    @Override
    public V update(Integer id, Z vo) {
        Optional<T> t = getJpaRepository().findById(id);
        return this.update(t.get(), vo);
    }

    @Transactional
    protected V update(T t, Z vo) {
        if (null == t) {
            throw new DataNotFoundException(messages.get("error.not.found", new String[]{t.getSecureId()}));
        }

        getVoConverter().transferVOToModel(vo, t);

        T updateObj = getJpaRepository().saveAndFlush(t);

        if (null != updateObj) {
            return getVoConverter().transferModelToVO(t, null);
        }
        return null;
    }

    @Override
    @Transactional
    public Boolean delete(String secureKey) {
        Optional<T> t = getJpaRepository().findBySecureId(secureKey);

        if (t.isPresent()) {
            Integer id = t.get().getId();
            getJpaRepository().deleteById(id);

            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public V findBySecureId(String secureKey) {
        Optional<T> t = getJpaRepository().findBySecureId(secureKey);

        if(t.isPresent())
            return getVoConverter().transferModelToVO(t.get(), null);
        else
            throw new DataNotFoundException(messages.get("error.not.found", new String[]{secureKey}));
    }

    @Override
    public V findById(Integer id) {

        Optional<T> t = getJpaRepository().findById(id);

        if(t.isPresent())
            return getVoConverter().transferModelToVO(t.get(), null);
        else
            throw new DataNotFoundException(messages.get("error.not.found", new String[]{id.toString()}));
    }
}
