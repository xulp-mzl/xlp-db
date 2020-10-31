package org.xlp.db.tableoption.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用：使用此注解标记的字段也可以与数据库表中的字段相对应。
 * 但是，此注解只在把查询出来的数据封装给指定的bean对象中起作用
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-26
 *         </p>
 * @version 1.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到  
@Target({ElementType.FIELD})//定义注解的作用目标**作用范围字段 
@Documented//说明该注解将被包含在javadoc中 
public @interface XLPIrrelevantColumn {
	//column的名称
	public String columnName() default "";
	public String descriptor() default "";//描述
}
