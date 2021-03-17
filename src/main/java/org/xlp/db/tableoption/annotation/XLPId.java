package org.xlp.db.tableoption.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.xlp.db.tableoption.xlpenum.DataType;
import org.xlp.db.tableoption.xlpenum.PrimaryKeyType;
import org.xlp.utils.XLPStringUtil;

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
	//column的名称
	public String columnName() default "";//主键对应的列名称
	public PrimaryKeyType type() default PrimaryKeyType.AUTO;//默认主键值为自增长
	public String descriptor() default "";//描述
	
	//以下字段用来根据实体类创建相应的表
	/**
	 * 字段长度
	 */
	public int length() default -1;
	
	/**
	 * 小数部分长度
	 */
	public int decimalLength() default 0;
	
	/**
	 * 默认字符串值
	 */
	public String defaultValue() default XLPStringUtil.NULL_STRING;
	
	/**
	 * 数据类型
	 */
	public DataType dataType() default DataType.NONE;
	
	/**
	 * 是否可为空值，默认可以 
	 */
	public boolean isNull() default true;
	
	/**
	 * 默认值是否用0填充
	 */
	public boolean zeroFill() default false;
}
