package com.toolset.dbutil.util;

import com.sun.istack.internal.NotNull;
import org.springframework.util.Assert;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用工具类
 * 一些工作中可能用到的小方法
 * @author yh
 */
public class CommonUtil {

    /**
     * 筛选 {@code list} 中 {@code beanClass} 对象的 {@code fieldName} 字段
     * 根据 {@code flag} 判断是仅包含筛选值的结果集还是不包含筛选值的结果集
     * @param list 需要筛选的集合
     * @param fieldName 需要筛选的字段
     * @param distinctValue 需要比较的值
     * @param beanClass 对象的类型
     * @param flag 返回结果是 仅包含/不包含 筛选值的结果集
     * @param <T> 泛型类型
     * @return 根据比较值筛选出集合中对象对应字段 仅包含/不包含 的结果集
     */
    public static <T> List<T> distinct(@NotNull List<T> list, @NotNull String fieldName, Object distinctValue,@NotNull Class beanClass, boolean flag) {
        Assert.notEmpty(list,"需要筛选的集合list不能为空！");
        Assert.notNull(fieldName,"需要筛选的字段fieldName不能为空！");
        Assert.notNull(beanClass,"需要筛选的对象类型beanClass不能为空！");
        List<T> justInclude = new ArrayList<>();
        List<T> notInclude = new ArrayList<>();
        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName,beanClass);
            if (pd != null) {
                for (T t : list) {
                    Object thisValue = pd.getReadMethod().invoke(t);
                    if (thisValue instanceof BigDecimal) {
                        if (((BigDecimal) thisValue).compareTo((BigDecimal) distinctValue) == 0) {
                            justInclude.add(t);
                        } else {
                            notInclude.add(t);
                        }
                    } else {
                        if (thisValue.equals(distinctValue)) {
                            justInclude.add(t);
                        } else {
                            notInclude.add(t);
                        }
                    }
                }
            }
        } catch (IntrospectionException introspectionException) {
            introspectionException.printStackTrace();
        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        } catch (InvocationTargetException invocationTargetException) {
            invocationTargetException.printStackTrace();
        }
        if (flag) {
            return justInclude;
        } else {
            return notInclude;
        }
    }
}
