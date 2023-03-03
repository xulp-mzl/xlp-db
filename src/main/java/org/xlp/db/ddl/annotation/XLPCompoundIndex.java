package org.xlp.db.ddl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.xlp.db.ddl.type.IndexType;

/**
 * 索引注解，适用于复合索引
 * 
 * @author 徐龙平
 *         <p>
 *         2023-2-28
 *         </p>
 * @version 1.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到  
@Target({ElementType.TYPE})//定义注解的作用目标**作用范围字段 
@Documented//说明该注解将被包含在javadoc中 
public @interface XLPCompoundIndex {
	/**
	 * 索引类型
	 */
	public IndexType[] indexType() default {};
	
	/**
	 * 索引名称
	 */
	public String[] name() default {};
	
	/**
	 * 索引列名称，名称之间用逗号隔开
	 */
	public String[] column();
}
