package org.xlp.db.tableoption.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来判断是否数据库表中的字段
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 3.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到  
@Target({ElementType.FIELD})//定义注解的作用目标**作用范围字段 
@Documented//说明该注解将被包含在javadoc中 
public @interface XLPColumn {
	//column的名称
	public String columnName() default "";
	public String descriptor() default "";//描述
	/**
	 * 当字段类型为String时，该值起作用，该值小于0时，无长度限制，大于0时，该长度为最大长度，默认值为-1
	 */
	public int maxLength() default -1;
	/**
	 * 当字段类型为String时，该值起作用，该字段的值保存到数据库之前是否去空
	 */
	public boolean trim() default true;
	
}
