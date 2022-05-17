package org.xlp.db.tableoption.annotation;

/**
 * <p>创建时间：2022年5月15日 下午5:09:16</p>
 * @author xlp
 * @version 1.0 
 * @Description 外键注解
*/
public @interface XLPForeign {
	/**
	 * 描述
	 * @return
	 */
	public String descriptor() default "";
	
	/**
	 * 外键指向主键所在的类
	 */
	public Class<?> value();
	
	/**
	 *  外键指向主键所在的类中主键字段名称，默认值与主键所在的类的主键相同
	 */
	public String[] to() default {};
}
