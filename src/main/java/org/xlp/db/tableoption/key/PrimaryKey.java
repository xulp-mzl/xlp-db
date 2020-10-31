package org.xlp.db.tableoption.key;


import org.xlp.db.exception.EntityException;
import org.xlp.db.tableoption.annotation.XLPId;
import org.xlp.db.tableoption.xlpenum.PrimaryKeyType;
import org.xlp.db.utils.BeanUtil;
import org.xlp.javabean.PropertyDescriptor;
import org.xlp.utils.XLPStringUtil;

/**
 * 主键信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-24
 *         </p>
 * @version 1.0
 * 
 */
 public final class PrimaryKey extends KeyAbstract{
	//主键mame
	private String name;
	//主键类型
	private PrimaryKeyType type;
	//主键实际值
	private Object value;
	//主键当前可用值
	private Object currentValue;
	//标记主键对应的实体字段是否是基本类型
	private boolean isPrimitive;
	
	/**
	 * 获得主键名称
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 获得主键类型
	 * @return
	 */
	public PrimaryKeyType getType() {
		return type;
	}

	/**
	 * 获得主键实际值
	 * @return
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * 获得主键当前可用值
	 * @return
	 */
	public Object getCurrentValue() {
		return currentValue;
	}

	/**
	 * 用bean对象构建此对象
	 * @param beanClass
	 * @throws EntityException 
	 * @throws NullPointerException 假如参数为null，抛出该异常
	 */
	public <T> PrimaryKey(T bean) throws EntityException{
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
	public <T> PrimaryKey(T bean, boolean isToObtainCurrentKeyValue) 
		throws EntityException{
		super(bean, isToObtainCurrentKeyValue);
	}
	
	/**
	 * 初始化数据
	 * @param bean
	 * @throws EntityException 
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <T> void init(T bean) throws EntityException {
		PropertyDescriptor<T> pd = (PropertyDescriptor<T>) BeanUtil.getIsPrimaryKey(beanClass);
		if (pd != null) {
			XLPId xlpId = pd.getFieldAnnotation(XLPId.class);
			name = xlpId.columnName();
			try {
				value = pd.executeReadMethod(bean);
			} catch (Exception e) {
				LOGGER.warn("读取主键值时产生异常，对应的字段名：" + pd.getFieldName());
			}
			type = xlpId.type();
			isPrimitive = pd.getFiledClassType().isPrimitive();
		}else {
			LOGGER.warn("给定的bean对象实体没有相关的主键");
		}
		
		currentValue = getCurrentKeyValue();
	}
	
	/**
	 * 得到当前可用主键值(对于单个主键)
	 * 
	 * @param pd
	 * @return 返回当前可用的主键值
	 * @throws EntityException
	 */
	private <T> Object getCurrentKeyValue() throws EntityException {
		Object currentValue = null;
		if(isToObtainCurrentKeyValue){
			currentValue = value;
			if (type == PrimaryKeyType.UUID && value == null) {
				currentValue = XLPStringUtil.uuidL();
			}else if (type == PrimaryKeyType.AUTO && (value == null ||
					(isPrimitive && value.toString().equals("0")))) {
				currentValue = getCurrentKeyNumber(name);
			}
		}
		return currentValue;
	}
	
	/**
	 * 得到主键对应的实体字段是否是基本类型
	 * 
	 * @return 假如返回true，是基本类型，否则不是基本类型
	 */
	public boolean isPrimitive(){
		return isPrimitive;
	}
}
