package org.xlp.db.tableoption.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.xlp.db.tableoption.xlpenum.RelationType;

/**
 * 表示表关系，注解在实体属性上
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-1
 *         </p>
 * @version 1.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到  
@Target({ElementType.FIELD})//定义注解的作用目标**作用范围字段 
@Documented//说明该注解将被包含在javadoc中 
public @interface XLPRelation {
	/**
	 * 写法为{"f=f`", "f1=f2`"}，表示是：两个bean对象的相关属性, 主实体字段名在等号前，属性实体的 字段名在等号后，期间不能空字符
	 * <p>如： { "id=id" }
	 * @return
	 */
	public String[] relation();
	
	/**
	 * 关系类型
	 * 
	 * @return
	 */
	public RelationType type();
	
	/**
	 * 描述
	 * 
	 * @return
	 */
	public String descriptor() default "";
}
