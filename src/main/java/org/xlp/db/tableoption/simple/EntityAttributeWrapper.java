package org.xlp.db.tableoption.simple;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.ManyTablesQuerySQL;
import org.xlp.db.sql.QuerySQL;
import org.xlp.db.tableoption.annotation.XLPRelation;
import org.xlp.db.tableoption.handlers.BeanHandleWithAnnotation;
import org.xlp.db.tableoption.handlers.MapHandle;
import org.xlp.db.tableoption.handlers.MapListHandle;
import org.xlp.db.tableoption.handlers.result.DBMapBeanConverter;
import org.xlp.db.tableoption.key.CompoundPrimaryKey;
import org.xlp.db.tableoption.xlpenum.RelationType;
import org.xlp.db.utils.BeanUtil;
import org.xlp.db.utils.Constants;
import org.xlp.javabean.JavaBeanPropertiesDescriptor;
import org.xlp.javabean.PropertyDescriptor;

/**
 * 给实体自动封装实体属性
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-15
 *         </p>
 * @version 1.0
 * 
 */
public class EntityAttributeWrapper<T> {
	//日志记录器
	private final static Logger LOGGER = LoggerFactory.getLogger(EntityAttributeWrapper.class);
	//实体类型
	private Class<?> entityClass;
	//标记构造此对象是用实体对象还是实体类型构造
	private boolean constructWithEntity;
	//实体对象
	private T entity;
	//别名后缀
	private static int  ALIAS_SUFFIX = 0;
	
	/**
	 * @throws NullPointerException 假如参数为null，抛出该异常
	 * @param entity
	 * @throws EntityException 
	 */
	public EntityAttributeWrapper(T entity) throws EntityException{
		this(entity.getClass());
		this.constructWithEntity = true;
		this.entity = entity;
	}
	
	/**
	 * @throws NullPointerException 假如参数为null，抛出该异常
	 * @param entityClass
	 * @throws EntityException 
	 */
	protected EntityAttributeWrapper(Class<?> entityClass) throws EntityException{
		this.entityClass = entityClass;
		if(!BeanUtil.isEntity(entityClass)){
			LOGGER.error(entityClass.getName() + ": 没有XLPEntity实体注解");
			throw new EntityException("此对象不是实体");
		}
		
		this.constructWithEntity = false;
	}
	
	/**
	 * 创建新实体对象
	 * 
	 * @return
	 * @throws EntityException 
	 */
	@SuppressWarnings("unchecked")
	public T createNewEntity() throws EntityException{
		if (!constructWithEntity) 
			throw new EntityException("请用[EntityAttributeWrapper(T entity)]此构造函数构造此对象");
		
		//开始创建默认的单表查询SQL对象
		QuerySQL<T> qs = new QuerySQL<T>(entity);
		
		validate(qs);
		
		PropertyDescriptor<T>[] pds = new JavaBeanPropertiesDescriptor<T>((Class<T>) entityClass)
				.getPdsWithAnnotation(XLPRelation.class);
		int len = pds.length;
		if (len == 0)
			try {
				return Constants.BASE_DB_OPTION.query(qs.getParamSql(), 
						new BeanHandleWithAnnotation<T>((Class<T>) entityClass),
						qs.getParams());
			} catch (SQLException e) {
				throw new EntityException("实体创建失败",e);
			}
		qs = null;
		
		RelationType type = null;
		XLPRelation relation = null;
		T newEntity = null;
		for (int i = 0; i < len; i++) {
			ALIAS_SUFFIX++;
			
			relation = pds[i].getFieldAnnotation(XLPRelation.class);
			type = relation.type();
			if (type == RelationType.ONE) 
				newEntity = initEntityAttrOne(newEntity, pds[i], relation);
			else if(type == RelationType.LIST)
				newEntity = initEntityAttrListOrSet(newEntity, pds[i], relation
						, true);
			else 
				newEntity = initEntityAttrListOrSet(newEntity, pds[i], relation
						, false);
		}
		return newEntity;
			
	}

	/**
	 * 给实体的List集合实体属性赋值
	 * 
	 * @param newEntity 新实体对象 
	 * @param propertyDescriptor 属性描述器
	 * @param relation 关系注解对象
	 * @param isList 标记关系是否是list
	 * @return 新实体
	 * @throws EntityException 
	 */
	@SuppressWarnings("all")
	private T initEntityAttrListOrSet(T newEntity,
			PropertyDescriptor<T> propertyDescriptor, XLPRelation relation,
			boolean isList) throws EntityException {
		//获取关系字段数组
		String[] f_f1s = relation.relation();
		int len = f_f1s.length;
		//获取属性实体类型
		Class<?> attrEntityClass = getAttrEntityCls1(propertyDescriptor, len);
		
		dealNoRelation(len, attrEntityClass);
		//获取多表查询对象
		ManyTablesQuerySQL mqs = mqs(f_f1s, len, attrEntityClass);
		
		//把数据库中的数据处理成List<Map>集合
		List<Map<String, Object>> listMaps = null;
		try {
			listMaps = Constants.BASE_DB_OPTION.query(mqs.getParamSql(),
					new MapListHandle(), mqs.getParams());
		} catch (SQLException e) {
			throw new EntityException("数据处理失败", e);
		}
		
		List<?> list = null;
		if (newEntity == null){
			if (listMaps.size() == 0) 
				return newEntity;
			else
				newEntity = new DBMapBeanConverter<T>().mapToBean(listMaps.get(0), 
						(Class<T>) entityClass);
		}
		list = new DBMapBeanConverter().mapListToBeanList(listMaps, attrEntityClass);
		if (isList) {
			BeanUtil.callSetter(newEntity, propertyDescriptor, list);
		}else {
			Set<?> set = new HashSet(list);
			BeanUtil.callSetter(newEntity, propertyDescriptor, set);
		}
		
		return newEntity;
	}

	/**
	 * 获取属性实体类型
	 * 
	 * @param propertyDescriptor 属性描述器
	 * @param len 关系个数
	 * @return
	 * @throws EntityException 
	 */
	private Class<?> getAttrEntityCls1(
			PropertyDescriptor<T> propertyDescriptor, int len) 
			throws EntityException {
		Field field = propertyDescriptor.getField();
		Type type = field.getGenericType();
		if (type == null || !(type instanceof ParameterizedType)) 
			throw new EntityException(field.getGenericType() + "该字段不符合要求");
		ParameterizedType pType = (ParameterizedType) type;
		//获取属性实体类型
		Class<?> attrClass = (Class<?>) pType.getActualTypeArguments()[0];
		return attrClass;
	}

	/**
	 * 前提条件处理
	 * 
	 * @param qs
	 * @throws EntityException
	 */
	private void validate(QuerySQL<T> qs) throws EntityException {
		//获取主键信息
		CompoundPrimaryKey cpk = qs.getPrimaryKey();
		//判断是否有主键
		int keyCount = cpk.getCount();
		if (keyCount == 0) {
			LOGGER.error(entityClass.getSimpleName() + "该实体不存在主键属性描述");
			throw new EntityException(entityClass.getSimpleName() + "该实体不存在主键属性描述");
		}
		//判断主键属性是否为空
		Object[] values = cpk.getValues();
		for (int i = 0; i < keyCount; i++) {
			if(values[i] == null)
				throw new EntityException("主键属性值不能为null");
		}
		cpk = null;
	}

	/***
	 * 给实体的单个实体属性赋值
	 * 
	 * @param newEntity 新实体对象 
	 * @param propertyDescriptor 属性描述器
	 * @param relation 关系注解对象
	 * @return 新实体
	 * @throws EntityException 
	 */
	@SuppressWarnings("all")
	private T initEntityAttrOne(T newEntity,
			PropertyDescriptor<T> propertyDescriptor, XLPRelation relation)
			throws EntityException {
		//获取关系字段数组
		String[] f_f1s = relation.relation();
		int len = f_f1s.length;
		LOGGER.debug("len =" + len);
		
		Class<?> attrEntityClass = getAttrEntityCls(propertyDescriptor);
		
		dealNoRelation(len, attrEntityClass);
		//获取多表查询对象
		ManyTablesQuerySQL mqs = mqs(f_f1s, len, attrEntityClass);
		
		//把数据库中的数据处理成map集合
		Map<String, Object> map = null;
		try {
			map = Constants.BASE_DB_OPTION.query(mqs.getParamSql(),
					new MapHandle(), mqs.getParams());
		} catch (SQLException e) {
			throw new EntityException("数据处理失败",e);
		}
		if (map.size() == 0) 
			return newEntity;
			
		if (newEntity == null) {
			newEntity = new DBMapBeanConverter<T>().mapToBean(map, (Class<T>) entityClass);
		}
		BeanUtil.callSetter(newEntity, propertyDescriptor,
				new DBMapBeanConverter().mapToBean(map, attrEntityClass)); 
		
		return newEntity;
	}

	/**
	 * 处理无关联时的情况
	 * 
	 * @param len
	 * @param attrEntityClass
	 * @throws EntityException
	 */
	private void dealNoRelation(int len, Class<?> attrEntityClass)
			throws EntityException {
		if (len == 0) {
			LOGGER.error(entityClass.getSimpleName() + 
					"与" + attrEntityClass.getSimpleName() + "没有属性关联");
			throw new EntityException(entityClass.getSimpleName() + 
					"与" + attrEntityClass.getSimpleName() + "没有属性关联");
		}
	}

	/**
	 * 返回属性实体类型
	 * 
	 * @param propertyDescriptor 属性描述器
	 * @param len 关系个数
	 * @return
	 * @throws EntityException
	 */
	private Class<?> getAttrEntityCls(PropertyDescriptor<T> propertyDescriptor)
			throws EntityException {
		//获取此实体属性类型
		Class<?> attrEntityClass = propertyDescriptor.getFiledClassType();
		return attrEntityClass;
	}

	/**
	 * 形成多表查询对象
	 * 
	 * @param fF1s 实体关系字段数组
	 * @param len 该数组的长度
	 * @param attrEntityClass 实体属性类型
	 * @return
	 * @throws EntityException 
	 */
	private ManyTablesQuerySQL mqs(String[] fF1s, int len, Class<?> attrEntityClass
			) throws EntityException {
		String[] f_f1 = null;
		
		String entN = entityClass.getSimpleName();
		String attrEntN = attrEntityClass.getSimpleName();
		
		ManyTablesQuerySQL mqs = new ManyTablesQuerySQL(entity);
		mqs.from(attrEntityClass, attrEntN + "_" + ALIAS_SUFFIX);
		
		for (int i = 0; i < len; i++) {
			//把"id=id"形式的字符串拆成id，id组成的数组
			f_f1 = splitString(fF1s[i]);
			if (BeanUtil.getFieldAlias(entityClass, f_f1[0]) == null
					|| BeanUtil.getFieldAlias(attrEntityClass, f_f1[1])  == null) {
				LOGGER.error("关系数据出错，出错数据是：[" + fF1s[i] + "]");
				throw new EntityException("关系数据出错，出错数据是：[" + fF1s[i] + "]");
			}
			mqs.andEqM(entN + "." + f_f1[0], attrEntN + "." + f_f1[1]);
		}
		
		return mqs;
	}

	/**
	 * 把"id=id"形式的字符串拆成id，id组成的数组
	 * 
	 * @param fF1s "id=id"形式的字符串
	 * @return "id=id"形式的字符串拆成id，id组成的数组
	 * @throws EntityException 
	 */
	private String[] splitString(String fF1) throws EntityException {
		String[] f_f1 = null;
		f_f1 = fF1.split("=");
		int len = f_f1.length;
		if (len != 2) {
			LOGGER.error(fF1 + "该关系格式错误");
			throw new EntityException(fF1 + "该关系格式错误");
		}
		return f_f1;
	}
}
