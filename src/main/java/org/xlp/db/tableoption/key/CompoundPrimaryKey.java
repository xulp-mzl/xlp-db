package org.xlp.db.tableoption.key;


import org.xlp.db.exception.EntityException;
import org.xlp.db.tableoption.annotation.XLPEntity;
import org.xlp.db.tableoption.annotation.XLPId;
import org.xlp.db.tableoption.xlpenum.PrimaryKeyType;
import org.xlp.javabean.JavaBeanPropertiesDescriptor;
import org.xlp.javabean.MethodException;
import org.xlp.javabean.PropertyDescriptor;
import org.xlp.utils.XLPOutputInfoUtil;
import org.xlp.utils.XLPStringUtil;

/**
 * 复合主键信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-24
 *         </p>
 * @version 1.0
 * 
 */
public final class CompoundPrimaryKey extends KeyAbstract{
	//主键mames
	private String[] names;
	//主键类型
	private PrimaryKeyType[] types;
	//主键值
	private Object[] values;
	//当前主键可用值
	private Object[] currentValues;
	//主键个数
	private int count;
	//标记主键对应的实体字段是否是基本类型
	private boolean[] isPrimitives;
	/**
	 * 获得主键名称
	 * @return
	 */
	public String[] getNames() {
		return names;
	}

	/**
	 * 获得主键类型
	 * @return
	 */
	public PrimaryKeyType[] getTypes() {
		return types;
	}

	/**
	 * 获得主键值
	 * @return
	 */
	public Object[] getValues() {
		return values;
	}

	/**
	 * 获得复合主键当前可用值
	 * @return
	 */
	public Object[] getCurrentValues() {
		return currentValues;
	}
	
	/**
	 * 获得复合主键个数
	 * 
	 * @return
	 */
	public int getCount() {
		return count;
	}

	/**
	 * 得到主键对应的实体字段是否是基本类型
	 * 
	 * @return 假如返回true，是基本类型，否则不是基本类型
	 */
	public boolean[] isPrimitives(){
		return isPrimitives;
	}
	
	/**
	 * 用bean对象构建此对象
	 * @param beanClass
	 * @throws EntityException 
	 * @throws NullPointerException 假如参数为null，抛出该异常
	 */
	public <T> CompoundPrimaryKey(T bean) throws EntityException{
		super(bean);
	}
	
	/**
	 * 用bean对象和isToObtainCurrentKeyValue构建此对象
	 * 
	 * @param bean
	 * @param isToObtainCurrentKeyValue 是否去数据库中获取当前可用值
	 * @throws EntityException
	 * @throws NullPointerException 假如参数为null，抛出该异常
	 */
	public <T> CompoundPrimaryKey(T bean, boolean isToObtainCurrentKeyValue) 
		throws EntityException{
		super(bean, isToObtainCurrentKeyValue);
	}
	
	/**
	 * 初始化数据
	 * @param bean
	 * @throws SQLException 
	 */
	@SuppressWarnings("all")
	@Override
	protected <T> void init(T bean) throws EntityException {
		//得到带指定注解的字段描述
		PropertyDescriptor<T>[] pds = new JavaBeanPropertiesDescriptor(beanClass).
				getPdsWithAnnotation(XLPId.class);
		int len = pds.length;
		values = new Object[len];
		types = new PrimaryKeyType[len];
		names = new String[len];
		currentValues = new Object[len];
		isPrimitives = new boolean[len];
		
		if(len == 0)
			LOGGER.warn("给定的bean对象实体没有相关的主键属性");
		
		XLPEntity entity = beanClass.getAnnotation(XLPEntity.class);
		
		XLPId xlpId;
		for (int i = 0; i <len; i++) {
			count ++;
			xlpId = pds[i].getFieldAnnotation(XLPId.class);
			names[i] = xlpId.columnName();
			names[i] = XLPStringUtil.isEmpty(names[i]) ? pds[i].getFieldName() : names[i];
			try {
				values[i] = pds[i].executeReadMethod(bean);
			} catch (MethodException e) {
				LOGGER.warn("读取主键值时产生异常，对应的字段名：" + pds[i].getFieldName());
				XLPOutputInfoUtil.println(e);
			}
			types[i] = xlpId.type() == PrimaryKeyType.NONE ? entity.primaryKeyType() : xlpId.type();
			isPrimitives[i] = pds[i].getFiledClassType().isPrimitive();
			
			if (types[i] == PrimaryKeyType.UUID && XLPStringUtil.isEmpty((String) values[i])) {
				currentValues[i] = XLPStringUtil.uuidL();
			} else if (isToObtainCurrentKeyValue && types[i] == PrimaryKeyType.AUTO
					&& (values[i] == null || (isPrimitives[i] && values[i].toString().equals("0")))) {
				currentValues[i] = getCurrentKeyNumber(names[i]);
			} else {
				currentValues[i] = values[i];
			}
		}
	}
}
