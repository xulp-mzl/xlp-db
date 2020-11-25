package org.xlp.db.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlp.db.tableoption.annotation.XLPColumn;
import org.xlp.db.tableoption.annotation.XLPEntity;
import org.xlp.db.tableoption.annotation.XLPId;
import org.xlp.javabean.JavaBeanPropertiesDescriptor;
import org.xlp.javabean.MethodException;
import org.xlp.javabean.PropertyDescriptor;
import org.xlp.javabean.convert.mapandbean.MapValueProcesser;
import org.xlp.javabean.processer.ValueProcesser;

/**
 * bean操作工具类
 * 
 * <p>
 * 2017-5-22
 * </p>
 * 
 * @version 2.0
 * 
 */
public class BeanUtil {
	// 日志记录
	private final static Logger LOGGER = LoggerFactory.getLogger(BeanUtil.class);

	/**
	 * 得到实体注解
	 * 
	 * @param beanClass
	 * @return
	 */
	public static <T> XLPEntity getXLPEntity(Class<T> beanClass) {
		if (beanClass == null) {
			return null;
		}

		XLPEntity xlpEntity = beanClass.getAnnotation(XLPEntity.class);
		if (xlpEntity == null) {
			return null;
		}
		return xlpEntity;
	}

	/**
	 * 判断是否是实体
	 * 
	 * @param beanClass
	 * @return
	 */
	public static <T> boolean isEntity(Class<T> beanClass) {
		if (beanClass == null) {
			return false;
		}

		XLPEntity xlpEntity = beanClass.getAnnotation(XLPEntity.class);
		if (xlpEntity == null) {
			return false;
		}
		return true;
	}

	/**
	 * 得到与主键相关的字段的描述（单个字段）
	 * 
	 * @param beanClass
	 * @return
	 */
	public static <T> PropertyDescriptor<T> getIsPrimaryKey(Class<T> beanClass) {
		if (!isEntity(beanClass)) {
			return null;
		}

		PropertyDescriptor<T>[] pds = new JavaBeanPropertiesDescriptor<T>(
				beanClass).getPds();

		int len = pds.length;
		XLPId xlpId = null;
		for (int i = 0; i < len; i++) {
			xlpId = pds[i].getFieldAnnotation(XLPId.class);
			if (xlpId != null) {
				return pds[i];
			}
		}
		return null;
	}

	/**
	 * 判断给定的实体对象字段中是否有XLPId注解
	 * 
	 * @param beanClass
	 * @return 假如有，返回true，否则返回FALSE
	 */
	public static boolean hasXLPId(Class<?> beanClass) {
		return getIsPrimaryKey(beanClass) != null;
	}

	/**
	 * 获取指定字段的别名，即所含注解描述名
	 * 
	 * @param beanClass
	 * @param fieldName
	 * @return
	 * @throws NullPointerException
	 *             假如参数为null，则抛出该异常
	 */
	public static <T> String getFieldAlias(Class<T> beanClass, String fieldName) {
		PropertyDescriptor<T>[] pds = new JavaBeanPropertiesDescriptor<T>(
				beanClass).getPds();

		int len = pds.length;
		XLPColumn xlpColumn;
		XLPId xlpId;
		// XLPIrrelevantColumn irrelevantColumn;
		String alias = null; // 别名
		for (int i = 0; i < len; i++) {
			if (pds[i].getFieldName().equals(fieldName)) {
				if ((xlpColumn = pds[i].getFieldAnnotation(XLPColumn.class)) != null) {
					alias = xlpColumn.columnName();
				} else if ((xlpId = pds[i].getFieldAnnotation(XLPId.class)) != null) {
					alias = xlpId.columnName();
				}
				break;
			}
		}
		return alias;
	}

	/**
	 * 给bean的主键填充值
	 * 
	 * @param bean
	 * @param value
	 */
	public static <T> void fillBeanKeys(T bean, Object[] values) {
		if (bean == null || values == null)
			return;
		@SuppressWarnings("unchecked")
		PropertyDescriptor<T>[] pds = new JavaBeanPropertiesDescriptor<T>(
				(Class<T>) bean.getClass()).getPdsWithAnnotation(XLPId.class);
		int len = pds.length;
		if(len != values.length){
			LOGGER.warn("传入的主键值个数与实际主键个数不相同");
			len = Math.min(len, values.length);
		}
		
		ValueProcesser processer = new MapValueProcesser();
		for (int i = 0; i < len; i++) {
			values[i] = processer.processValue(pds[i].getFiledClassType(), values[i]);
			callSetter(bean, pds[i], values[i]);
		}
	}

	/**
	 * 快速调用写方法
	 * 
	 * @param bean
	 * @param pd
	 * @param value
	 * @throws NullPointerException
	 *             假如参数为null,抛出该异常
	 */
	public static <T> void callSetter(T bean, PropertyDescriptor<T> pd,
			Object value) {
		try {
			pd.executeWriteMethod(bean, value);
		} catch (MethodException e) {
			LOGGER.warn("调用[" + pd.getFieldName() + "]该字段的写方法失败");
		}
	}

	/**
	 * 快速调用读方法
	 * 
	 * @param bean
	 * @param pd
	 * @return
	 * @throws NullPointerException
	 *             假如参数为null,抛出该异常
	 */
	public static <T> Object callGetter(T bean, PropertyDescriptor<T> pd) {
		try {
			return pd.executeReadMethod(bean);
		} catch (MethodException e) {
			LOGGER.warn("调用[" + pd.getFieldName() + "]该字段的读方法失败");
		}
		return null;
	}

	/**
	 * 用beanClass得到bean对象
	 * 
	 * @param beanClass
	 * @return
	 * @throws NullPointerException
	 *             假如参数为null,抛出该异常
	 * @throws RuntimeException
	 *             假如指定类实例化失败，抛出该异常
	 */
	public static <T> T newInstance(Class<T> beanClass) {
		try {
			return beanClass.newInstance();
		} catch (InstantiationException e) {
			LOGGER.error("[" + beanClass.getName() + "]该类对象实例化失败");
			throw new RuntimeException("bean对象实例化失败", e);
		} catch (IllegalAccessException e) {
			LOGGER.error("[" + beanClass.getName() + "]该类对象实例化失败");
			throw new RuntimeException("bean对象实例化失败", e);
		}
	}
}
