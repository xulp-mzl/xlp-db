package org.xlp.db.tableoption.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.xlp.db.tableoption.xlpenum.PrimaryKeyDataType;
import org.xlp.db.tableoption.xlpenum.PrimaryKeyType;

/**
 * 用来判断是否数据库表中的主键的字段
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 1.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到  
@Target({ElementType.FIELD})//定义注解的作用目标**作用范围字段 
@Documented//说明该注解将被包含在javadoc中 
public @interface XLPId {
	/**
	 * 主键对应的列名称
	 * @return
	 */
	public String columnName() default "";
	
	/**
	 * 主键类型
	 */
	public PrimaryKeyType type() default PrimaryKeyType.NONE;
	
	/**
	 * 主键描述
	 * @return
	 */
	public String descriptor() default "";
	
	//以下字段用来根据实体类创建相应的表
	/**
	 * 字段长度, 默认值适用于字符串类
	 */
	public int length() default -1;

	
	/**
	 * 数据类型
	 */
	public PrimaryKeyDataType dataType() default PrimaryKeyDataType.NONE;
}
