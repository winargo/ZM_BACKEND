package com.billermanagement.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Copy properties from an Object to other Object
 * Extended from spring BeansUtils
 *
 * Created by andri.khrisharyadi@gmail.com
 * on 3/26/14.
 *
 * Edit: Add option to avoid copy if source value is null (fani.h)
 */
public class ExtendedSpringBeanUtil extends BeanUtils {

    /**
     * copy properties from other object with same attributes name
     *
     * @param source
     * @param target
     * @param copiedProperties
     * @throws BeansException
     */
    public static void copySpecificProperties(Object source, Object target,
                                              String[] copiedProperties, boolean excludeSourceNullValue)
            throws BeansException {

        copySpecificProperties(source, target, copiedProperties, copiedProperties, excludeSourceNullValue);
    }

    public static void copySpecificProperties(Object source, Object target,
                                              String[] copiedProperties)
            throws BeansException {

        copySpecificProperties(source, target, copiedProperties, copiedProperties, true);
    }

    /**
     * copy properties from object to other object with same or different attributes name
     * count attributes must be same
     *
     * @param source
     * @param target
     * @param sourceCopiedProperties
     * @param targetCopiedProperties
     * @throws BeansException
     */
    public static void copySpecificProperties(Object source, Object target ,
                                              String[] sourceCopiedProperties , String[] targetCopiedProperties,
                                              boolean excludeSourceNullValue)
            throws BeansException {

        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        if(sourceCopiedProperties == null){
            copyProperties(source, target);
            return;
        }

        if(sourceCopiedProperties.length != targetCopiedProperties.length){
            throw new FatalBeanException("Count source copy properties must be equal with target copy properties");
        }

        List<String> sourceCopiedList = (sourceCopiedProperties != null) ? Arrays.asList(sourceCopiedProperties) : null;
        List<String> targetCopiedList = (targetCopiedProperties != null) ? Arrays.asList(targetCopiedProperties) : null;

        int n = 0;

        for (String sourceCopied : sourceCopiedList) {
            PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), sourceCopied);
            PropertyDescriptor targetPd = getPropertyDescriptor(target.getClass(), targetCopiedList.get(n));
            if( (sourcePd!=null && targetPd!=null)

                    ) {

                if (sourcePd != null && sourcePd.getReadMethod() != null
                        && targetPd !=null && targetPd.getReadMethod() != null
                        && sourcePd.getPropertyType().equals(targetPd.getPropertyType())
                        ) {
                    try {
                        Method readMethodSource = sourcePd.getReadMethod();
                        if (!Modifier.isPublic(readMethodSource.getDeclaringClass().getModifiers())) {
                            readMethodSource.setAccessible(true);
                        }

                        Object sourceValue = readMethodSource.invoke(source);

                        Method writeMethodTarget = targetPd.getWriteMethod();

                        if (!Modifier.isPublic(writeMethodTarget.getDeclaringClass().getModifiers())) {
                            writeMethodTarget.setAccessible(true);
                        }

                        if (sourceValue == null && !excludeSourceNullValue) {
                            writeMethodTarget.invoke(target, sourceValue);
                        } else if (sourceValue != null) {
                            writeMethodTarget.invoke(target, sourceValue);
                        }
                    }
                    catch (Throwable ex) {
                        throw new FatalBeanException("Could not copy properties from source to target", ex);
                    }
                }
            }

            n++;


        }
    }

    public static void copySpecificProperties(Object source, Object target ,
                                              String[] sourceCopiedProperties ,
                                              String[] targetCopiedProperties){
        copySpecificProperties(source, target, sourceCopiedProperties, targetCopiedProperties, true);
    }

}
